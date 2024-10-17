package org.metamart.service.secrets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.metamart.schema.api.services.CreateDatabaseService.DatabaseServiceType.Mysql;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.metamart.schema.api.services.DatabaseConnection;
import org.metamart.schema.auth.SSOAuthMechanism;
import org.metamart.schema.entity.automations.TestServiceConnectionRequest;
import org.metamart.schema.entity.automations.Workflow;
import org.metamart.schema.entity.services.ServiceType;
import org.metamart.schema.entity.teams.AuthenticationMechanism;
import org.metamart.schema.security.client.OktaSSOClientConfig;
import org.metamart.schema.security.client.MetaMartJWTClientConfig;
import org.metamart.schema.security.secrets.SecretsManagerConfiguration;
import org.metamart.schema.security.secrets.SecretsManagerProvider;
import org.metamart.schema.services.connections.database.MysqlConnection;
import org.metamart.schema.services.connections.database.common.basicAuth;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;
import org.metamart.service.exception.InvalidServiceConnectionException;
import org.metamart.service.util.JsonUtils;

public abstract class ExternalSecretsManagerTest {
  protected ExternalSecretsManager secretsManager;

  @Test
  void testEncryptDecryptDatabaseServiceConnectionConfig() {
    String password = "metamart-test";
    MysqlConnection expectedConnection =
        new MysqlConnection().withAuthType(new basicAuth().withPassword(password));
    Map<String, Map<String, String>> mysqlConnection =
        Map.of("authType", Map.of("password", password));

    // Ensure encrypted service connection config encrypts the password
    MysqlConnection actualConnection =
        (MysqlConnection)
            secretsManager.encryptServiceConnectionConfig(
                mysqlConnection, Mysql.value(), "test", ServiceType.DATABASE);
    assertNotEquals(
        password,
        JsonUtils.convertValue(actualConnection.getAuthType(), basicAuth.class).getPassword());

    // Decrypt the encrypted password and validate
    actualConnection =
        (MysqlConnection)
            secretsManager.decryptServiceConnectionConfig(
                mysqlConnection, Mysql.value(), ServiceType.DATABASE);
    assertEquals(
        password,
        JsonUtils.convertValue(actualConnection.getAuthType(), basicAuth.class).getPassword());
    assertEquals(expectedConnection, actualConnection);
  }

  @Test
  void testEncryptDecryptSSSOConfig() {
    String privateKey = "secret:/metamart/bot/bot/config/authconfig/privatekey";
    OktaSSOClientConfig config = new OktaSSOClientConfig().withPrivateKey(privateKey);
    AuthenticationMechanism expectedAuthMechanism =
        new AuthenticationMechanism()
            .withAuthType(AuthenticationMechanism.AuthType.SSO)
            .withConfig(
                new SSOAuthMechanism()
                    .withAuthConfig(config)
                    .withSsoServiceType(SSOAuthMechanism.SsoServiceType.OKTA));

    AuthenticationMechanism actualAuthMechanism =
        JsonUtils.convertValue(expectedAuthMechanism, AuthenticationMechanism.class);

    // Encrypt private key and ensure it is indeed encrypted
    secretsManager.encryptAuthenticationMechanism("bot", actualAuthMechanism);
    assertNotEquals(privateKey, getPrivateKey(actualAuthMechanism));

    // Decrypt private key and ensure it is decrypted
    secretsManager.decryptAuthenticationMechanism("bot", actualAuthMechanism);
    assertEquals(privateKey, getPrivateKey(actualAuthMechanism));
  }

  @Test
  void testEncryptDecryptWorkflow() {
    String password =
        "secret:/metamart/workflow/my-workflow/request/connection/config/password";
    String secretKey = "secret:/metamart/serverconnection/securityconfig/secretkey";
    MetaMartConnection connection =
        new MetaMartConnection()
            .withSecurityConfig(new MetaMartJWTClientConfig().withJwtToken(secretKey));
    DatabaseConnection dbConnection =
        new DatabaseConnection()
            .withConfig(new MysqlConnection().withAuthType(new basicAuth().withPassword(password)));
    TestServiceConnectionRequest testRequest =
        new TestServiceConnectionRequest()
            .withConnection(dbConnection)
            .withServiceType(ServiceType.DATABASE)
            .withConnectionType("Mysql");
    Workflow expectedWorkflow =
        new Workflow()
            .withName("my-workflow")
            .withMetaMartServerConnection(connection)
            .withRequest(testRequest);
    Workflow actualWorkflow = JsonUtils.convertValue(expectedWorkflow, Workflow.class);

    // Encrypt the workflow and ensure password and secrete key are encrypted
    actualWorkflow = secretsManager.encryptWorkflow(actualWorkflow);
    assertNotEquals(password, getPassword(actualWorkflow));
    assertNotEquals(
        secretKey,
        actualWorkflow.getMetaMartServerConnection().getSecurityConfig().getJwtToken());

    // Decrypt the workflow and ensure password and secrete key are decrypted
    actualWorkflow = secretsManager.decryptWorkflow(actualWorkflow);
    assertEquals(password, getPassword(actualWorkflow));
    assertEquals(
        secretKey,
        actualWorkflow.getMetaMartServerConnection().getSecurityConfig().getJwtToken());
    assertEquals(expectedWorkflow, actualWorkflow);
  }

  @Test
  void testExceptionConnection() {
    Map<String, Object> mysqlConnection =
        Map.of(
            "username1", "metamart-test", "authType", Map.of("password", "metamart-test"));
    InvalidServiceConnectionException thrown =
        Assertions.assertThrows(
            InvalidServiceConnectionException.class,
            () ->
                secretsManager.encryptServiceConnectionConfig(
                    mysqlConnection, Mysql.value(), "test", ServiceType.DATABASE));

    Assertions.assertEquals(
        "Failed to encrypt 'Mysql' connection stored in DB due to an unrecognized field: 'username1'",
        thrown.getMessage());
    thrown =
        Assertions.assertThrows(
            InvalidServiceConnectionException.class,
            () ->
                secretsManager.decryptServiceConnectionConfig(
                    mysqlConnection, Mysql.value(), ServiceType.DATABASE));

    Assertions.assertEquals(
        "Failed to decrypt 'Mysql' connection stored in DB due to an unrecognized field: 'username1'",
        thrown.getMessage());
  }

  @Test
  void testReturnsExpectedSecretManagerProvider() {
    assertEquals(expectedSecretManagerProvider(), secretsManager.getSecretsManagerProvider());
  }

  abstract void setUpSpecific(SecretsManagerConfiguration config);

  protected abstract SecretsManagerProvider expectedSecretManagerProvider();

  private String getPrivateKey(AuthenticationMechanism authMechanism) {
    return ((OktaSSOClientConfig) ((SSOAuthMechanism) authMechanism.getConfig()).getAuthConfig())
        .getPrivateKey();
  }

  private String getPassword(Workflow workflow) {
    return JsonUtils.convertValue(
            ((MysqlConnection)
                    ((DatabaseConnection)
                            ((TestServiceConnectionRequest) workflow.getRequest()).getConnection())
                        .getConfig())
                .getAuthType(),
            basicAuth.class)
        .getPassword();
  }
}
