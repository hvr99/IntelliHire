import os

def create_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

base_dir = 'user-service'

pom_xml = """
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
        <relativePath/>
    </parent>
    <groupId>com.jobrec</groupId>
    <artifactId>user-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>user-service</name>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-oauth2-resource-server</artifactId></dependency>
        <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
        </plugins>
    </build>
</project>
"""
create_file(f'{base_dir}/pom.xml', pom_xml)

app_yml = """
server:
  port: ${USER_SERVICE_PORT:8082}
spring:
  application:
    name: user-service
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/jobrec}
    username: ${DB_USER:user_svc}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: users
  flyway:
    enabled: true
    baseline-on-migrate: true
    schemas: users
    default-schema: users
jwt:
  secret: ${JWT_SECRET:very_secret_default_key_that_is_at_least_256_bits_long_placeholder}
internal:
  api-key: ${INTERNAL_API_KEY:internal_key_placeholder}
"""
create_file(f'{base_dir}/src/main/resources/application.yml', app_yml)

app_test_yml = """
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        default_schema: public
  flyway:
    enabled: false
jwt:
  secret: test_secret_key_that_is_at_least_256_bits_long_for_hs256_testing
internal:
  api-key: test_internal_key
"""
create_file(f'{base_dir}/src/main/resources/application-test.yml', app_test_yml)

flyway_v1 = """
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    headline VARCHAR(255),
    summary TEXT,
    location VARCHAR(255),
    phone VARCHAR(50)
);

CREATE TABLE skills (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    level VARCHAR(50),
    years INTEGER,
    source VARCHAR(50) NOT NULL,
    UNIQUE (user_id, name)
);

CREATE TABLE educations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    degree VARCHAR(255) NOT NULL,
    institution VARCHAR(255) NOT NULL,
    field VARCHAR(255),
    start_date DATE,
    end_date DATE
);

CREATE TABLE experiences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    company VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE
);

CREATE TABLE certifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    issuer VARCHAR(255),
    date DATE,
    verified BOOLEAN DEFAULT FALSE
);

CREATE TABLE preferences (
    user_id UUID PRIMARY KEY,
    minimum_salary DECIMAL(12,2)
);

CREATE TABLE preference_roles (
    preference_user_id UUID NOT NULL,
    role VARCHAR(255) NOT NULL,
    FOREIGN KEY (preference_user_id) REFERENCES preferences(user_id) ON DELETE CASCADE
);

CREATE TABLE preference_locations (
    preference_user_id UUID NOT NULL,
    location VARCHAR(255) NOT NULL,
    FOREIGN KEY (preference_user_id) REFERENCES preferences(user_id) ON DELETE CASCADE
);

CREATE TABLE preference_work_modes (
    preference_user_id UUID NOT NULL,
    work_mode VARCHAR(50) NOT NULL,
    FOREIGN KEY (preference_user_id) REFERENCES preferences(user_id) ON DELETE CASCADE
);
"""
create_file(f'{base_dir}/src/main/resources/db/migration/V1__init_users_schema.sql', flyway_v1)

app_java = """
package com.jobrec.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
"""
create_file(f'{base_dir}/src/main/java/com/jobrec/user/UserServiceApplication.java', app_java)
