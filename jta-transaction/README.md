## JTA transaction model
JTA—is an API for managing transactions. It’s based on the distributed transaction processing (DTP) model from the Open Group. Doing programmatic transaction or bean-managed persistence, then you’re using JTA interfaces.
JTS—Java Transaction Service is a specification for building a transaction service. JTA may rest upon JTS. As an EJB developer, you’ll practically never touch JTS. JTS is defined in CORBA and is part of Object Services.


### Prerequisites
- Java 8
- mysql 8
- maven 3

### create database
    CREATE SCHEMA `transaction` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci ;
    
    CREATE TABLE jta_transaction_test (
        id INT PRIMARY KEY,
        code VARCHAR(255) UNIQUE,
        name VARCHAR(255) UNIQUE
    );
    insert into jta_transaction_test (id, code, name) values(1, 'code_1', 'name_1');
    insert into jta_transaction_test (id, code, name) values(2, 'code_2', 'name_2');
    insert into jta_transaction_test (id, code, name) values(3, 'code_3', 'name_3');
    commit;

### Build
`mvn clean install`

### Test
`mvn clean test`
