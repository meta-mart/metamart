```yaml {% srNumber=300 %}
workflowConfig:
  loggerLevel: INFO  # DEBUG, INFO, WARNING or ERROR
  metaMartServerConfig:
    hostPort: "http://localhost:8585/api"
    authProvider: metamart
    securityConfig:
      jwtToken: "{bot_jwt_token}"
    ## Store the service Connection information
    storeServiceConnection: true  # false
    ## Secrets Manager Configuration
    # secretsManagerProvider: aws, azure or noop
    # secretsManagerLoader: airflow or env
    ## If SSL, fill the following
    # verifySSL: validate  # or ignore
    # sslConfig:
    #   caCertificate: /local/path/to/certificate
```