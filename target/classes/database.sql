CREATE DATABASE test;

CREATE TABLE user(
    id int auto_increment primary key,
    name varchar(100) not null,
    email varchar(50) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp
);

INSERT user(name, email) values('Luis Espinel', 'lespinel@test.com');
INSERT user(name, email) values('Fake Name', 'fake@test.com');