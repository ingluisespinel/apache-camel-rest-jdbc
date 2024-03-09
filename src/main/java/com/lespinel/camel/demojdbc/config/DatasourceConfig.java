package com.lespinel.camel.demojdbc.config;

import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.PropertyInject;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @BindToRegistry
    public DataSource mysqlDatasource(@PropertyInject("database.host") String host,
                                      @PropertyInject(value = "database.port", defaultValue = "3306") Integer port,
                                      @PropertyInject("database.name") String databaseName,
                                      @PropertyInject("database.user") String user,
                                      @PropertyInject("database.pass") String password)
    {
        org.apache.commons.dbcp2.BasicDataSource dataSource = new org.apache.commons.dbcp2.BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://"+host +":"+port+"/"+databaseName+"?useSSL=true&serverTimezone=UTC");
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

}
