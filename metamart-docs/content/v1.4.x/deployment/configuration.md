---
title: Server Configuration Reference
slug: /deployment/configuration
---

# Server Configuration Reference

This document describes MetaMart Server Configuration

```yaml
swagger:
  resourcePackage: org.metamart.service.resources


server:
  rootPath: '/api/*'
  applicationConnectors:
    - type: http
      port: 8585
  adminConnectors:
    - type: http
      port: 8586

# Logging settings.
# https://logback.qos.ch/manual/layouts.html#conversionWord
logging:
  level: INFO
  loggers:
    org.metamart.service.common: DEBUG
    io.swagger: ERROR
  appenders:
    - type: file
      threshold: TRACE
      logFormat: "%level [%d{HH:mm:ss.SSS}] [%t] %logger{5} - %msg %n"
      currentLogFilename: ./logs/metamart.log
      archivedLogFilenamePattern: ./logs/metamart-%d{yyyy-MM-dd}-%i.log.gz
      archivedFileCount: 7
      timeZone: UTC
      maxFileSize: 50MB

database:
  # the name of the JDBC driver, mysql in our case
  driverClass: com.mysql.cj.jdbc.Driver
  # the username and password
  user: metamart_user
  password: metamart_password
  # the JDBC URL; the database is called metamart_db
  url: jdbc:mysql://localhost/metamart_db?useSSL=false&serverTimezone=UTC

elasticsearch:
  host: localhost
  port: 9200

eventHandlerConfiguration:
  eventHandlerClassNames:
    - "org.metamart.service.events.AuditEventHandler"

health:
  delayedShutdownHandlerEnabled: true
  shutdownWaitPeriod: 1s
  healthCheckUrlPaths: ["/api/v1/health-check"]
  healthChecks:
    - name: UserDatabaseCheck
      critical: true
      schedule:
        checkInterval: 2500ms
        downtimeInterval: 10s
        failureAttempts: 2
        successAttempts: 1
```

## Server Port

```yaml
server:
  rootPath: '/api/*'
  applicationConnectors:
    - type: http
      port: 8585
  adminConnectors:
    - type: http
      port: 8586
```

By default, the MetaMart server runs on port 8585. It uses Jetty Server. The above config can be changed to make it
run on a different port. Once you have updated the port details in config restart the server.

## Database

MetaMart supports MySQL or Postgres as the database. The database configurations and connection strings must be as
specified below. 

The section below refers to the database connection details to MySQL database. We recommend you create
a MySQL user with a strong password and update this section accordingly.

```yaml
database:
  # the name of the JDBC driver, mysql in our case
  driverClass: com.mysql.cj.jdbc.Driver
  # the username and password
  user: metamart_user
  password: metamart_password
  # the JDBC URL; the database is called metamart_db
  url: jdbc:mysql://localhost/metamart_db?useSSL=false&serverTimezone=UTC
```

The section below refers to the database connection details to Postgres database. MetaMart uses stored generated
columns which is supported in Postgres 12. Ensure that you have Postgres 12 or a later version. We recommend you create
a Postgres user with a strong password and update this section accordingly.

```yaml
database:
  # the name of the JDBC driver, postgres in our case
  driverClass: org.postgresql.Driver
  # the username and password
  user: metamart_user
  password: metamart_password
  # the JDBC URL; the database is called metamart_db
  url: jdbc:postgresql://localhost:5432/metamart_db?useSSL=false&serverTimezone=UTC
```

## ElasticSearch

```yaml
elasticsearch:
  host: localhost
  port: 9200
```

ElasticSearch is one of the pre-requisites to run MetaMart. Default configuration expects a single instance of
ElasticSearch running on the local machine. Please make sure you update it with your production elastic search.

## Event Handlers

```yaml
eventHandlerConfiguration:
  eventHandlerClassNames:
    - "org.metamart.service.events.AuditEventHandler"
```

EventHandler configuration is optional. It will update the AuditLog in MySQL DB and also ElasticSearch indexes whenever
any entity is updated either through UI or API interactions. We recommend you leave it there as it enhances the user
experience.

## Healthcheck

```yaml
health:
  delayedShutdownHandlerEnabled: true
  shutdownWaitPeriod: 1s
  healthCheckUrlPaths: ["/api/v1/health-check"]
  healthChecks:
    - name: UserDatabaseCheck
      critical: true
      schedule:
        checkInterval: 2500ms
        downtimeInterval: 10s
        failureAttempts: 2
        successAttempts: 1
```

Healthcheck API provides an API endpoint to check the MetaMart server health. We recommend in production settings to
use this API to monitor the health of your MetaMart instance. Please tune the above configuration according to your
production needs.

## Security

Please follow our [Enable Security Guide](/deployment/security) to configure security for your MetaMart installation.
