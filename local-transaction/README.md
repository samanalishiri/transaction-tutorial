## Local transaction model
The developer manages connection, not transaction.it is the DBMS or JMS Provider that is actually managing the local transaction.


### Prerequisites
- Java 8
- mysql 8
- maven 3

### create database
    CREATE SCHEMA `transaction` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci ;
    
    CREATE TABLE local_transaction_test (
        id INT AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(255) unique,
        name VARCHAR(255) unique
    );
    insert into local_transaction_test (code, name) values('code_1', 'name_1');
    insert into local_transaction_test (code, name) values('code_2', 'name_2');
    insert into local_transaction_test (code, name) values('code_3', 'name_3');
    commit;

### Build
`mvn clean install`

### Test
`mvn clean test`
