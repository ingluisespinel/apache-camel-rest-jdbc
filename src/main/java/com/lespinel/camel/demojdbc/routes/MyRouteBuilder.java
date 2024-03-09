package com.lespinel.camel.demojdbc.routes;

import com.lespinel.camel.demojdbc.models.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jdbc.JdbcConstants;
import org.apache.camel.model.rest.RestBindingMode;

import java.util.Arrays;
import java.util.Map;

@Slf4j
public class MyRouteBuilder extends RouteBuilder {
    @PropertyInject("server.host")
    private String serverHost;
    @PropertyInject("server.port")
    private Integer serverPort;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .contextPath("/api/v1/camel-demo")
                .component("jetty")
                .host(serverHost).port(serverPort)
                .enableCORS(true)
                .bindingMode(RestBindingMode.json);

        rest("/users")
                .get()
                    .outType(User[].class)
                    .to("direct:getAllUsers")
                .get("/{userId}")
                    .outType(User.class)
                    .to("direct:getUserById")
                .post()
                    .produces("application/json")
                    .to("direct:insertUser");

        /*
         * Route that return the list of users from the MySQL Database
         * Support pagination with 'from' and 'to' headers
         * By default the pagination values are 'from'=0 and 'to'=10
         */
        from("direct:getAllUsers")
                // Validate Pagination Headers
                .process(exchange -> {
                    Integer from = exchange.getIn().getHeader("from", Integer.class);
                    Integer to = exchange.getIn().getHeader("to", Integer.class);
                    // Set the pagination headers
                    exchange.getIn().setHeader("from", from != null ? from : 0);
                    exchange.getIn().setHeader("to", to != null ? to : 10);
                })
                .setBody(simple("SELECT * FROM user LIMIT ${headers.from}, ${headers.to} "))
                .to("jdbc:mysqlDatasource?outputClass="+User.class.getName())
                .log("Total results founds: ${headers.CamelJdbcRowCount}");

        /*
         * Route that return the user information depending on userId
         */
        from("direct:getUserById")
                .setBody(simple("SELECT * FROM user WHERE id = :?userId"))
                .to("jdbc:mysqlDatasource?outputType=SelectOne&useHeadersAsParameters=true&outputClass="+User.class.getName())
                .log("Total results founds: ${headers.CamelJdbcRowCount}");

        /*
         * Route for User creation over MySQL database
         */
        from("direct:insertUser")
                .setHeader("CamelRetrieveGeneratedKeys", constant(true))
                .setBody(simple("INSERT INTO user(name, email) VALUES ('${body[name]}', '${body[email]}')"))
                .to("jdbc:mysqlDatasource")
                // Set in the body Message the new ID created after insertion
                .process(exchange -> {
                    var generatedKeyData = exchange.getIn().getHeader(JdbcConstants.JDBC_GENERATED_KEYS_DATA, Map[].class);
                    if(generatedKeyData != null && generatedKeyData.length > 0){
                        exchange.getIn().setBody(Map.of("newUserId",generatedKeyData[0].get("GENERATED_KEY")));
                        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
                    }
                })
                .log("New user Created with key ${headers.CamelGeneratedKeysRows}");

        /*
         * Route that receipt a User list and show how to split the list
         * and process each User individually
         */
        from("direct:processUserList")
                .log("Items in body: ${body}")
                .split(body()).streaming()
                    .log("In log body data: ${body}")
                    .process(exchange -> {
                        User user = exchange.getIn().getBody(User.class);
                        log.info("Processing user Id {} In process", user.getId());
                    })
                .end()
                .log("processUserList finished")
                .log("---------------------------");


    }
}
