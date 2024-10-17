import unittest
from unittest.mock import patch

from azure.core.credentials import AccessToken
from azure.identity import ClientSecretCredential

from metadata.generated.schema.entity.services.connections.database.azureSQLConnection import (
    Authentication,
    AuthenticationMode,
    AzureSQLConnection,
)
from metadata.generated.schema.entity.services.connections.database.common.azureConfig import (
    AzureConfigurationSource,
)
from metadata.generated.schema.entity.services.connections.database.common.basicAuth import (
    BasicAuth,
)
from metadata.generated.schema.entity.services.connections.database.mysqlConnection import (
    MysqlConnection,
)
from metadata.generated.schema.entity.services.connections.database.postgresConnection import (
    PostgresConnection,
)
from metadata.generated.schema.security.credentials.azureCredentials import (
    AzureCredentials,
)
from metadata.ingestion.source.database.azuresql.connection import get_connection_url
from metadata.ingestion.source.database.mysql.connection import (
    get_connection as mysql_get_connection,
)
from metadata.ingestion.source.database.postgres.connection import (
    get_connection as postgres_get_connection,
)


class TestGetConnectionURL(unittest.TestCase):
    def test_get_connection_url_wo_active_directory_password(self):
        connection = AzureSQLConnection(
            driver="SQL Server",
            hostPort="myserver.database.windows.net",
            database="mydb",
            username="myuser",
            password="mypassword",
            authenticationMode=AuthenticationMode(
                authentication=Authentication.ActiveDirectoryPassword,
                encrypt=True,
                trustServerCertificate=False,
                connectionTimeout=45,
            ),
        )
        expected_url = "mssql+pyodbc://?odbc_connect=Driver%3DSQL+Server%3BServer%3Dmyserver.database.windows.net%3BDatabase%3Dmydb%3BUid%3Dmyuser%3BPwd%3Dmypassword%3BEncrypt%3Dyes%3BTrustServerCertificate%3Dno%3BConnection+Timeout%3D45%3BAuthentication%3DActiveDirectoryPassword%3B"
        self.assertEqual(str(get_connection_url(connection)), expected_url)

        connection = AzureSQLConnection(
            driver="SQL Server",
            hostPort="myserver.database.windows.net",
            database="mydb",
            username="myuser",
            password="mypassword",
            authenticationMode=AuthenticationMode(
                authentication=Authentication.ActiveDirectoryPassword,
            ),
        )

        expected_url = "mssql+pyodbc://?odbc_connect=Driver%3DSQL+Server%3BServer%3Dmyserver.database.windows.net%3BDatabase%3Dmydb%3BUid%3Dmyuser%3BPwd%3Dmypassword%3BEncrypt%3Dno%3BTrustServerCertificate%3Dno%3BConnection+Timeout%3D30%3BAuthentication%3DActiveDirectoryPassword%3B"
        self.assertEqual(str(get_connection_url(connection)), expected_url)

    def test_get_connection_url_mysql(self):
        connection = MysqlConnection(
            username="metamart_user",
            authType=BasicAuth(password="metamart_password"),
            hostPort="localhost:3306",
            databaseSchema="metamart_db",
        )
        engine_connection = mysql_get_connection(connection)
        self.assertEqual(
            str(engine_connection.url),
            "mysql+pymysql://metamart_user:metamart_password@localhost:3306/metamart_db",
        )
        connection = MysqlConnection(
            username="metamart_user",
            authType=AzureConfigurationSource(
                azureConfig=AzureCredentials(
                    clientId="clientid",
                    tenantId="tenantid",
                    clientSecret="clientsecret",
                    scopes="scope1,scope2",
                )
            ),
            hostPort="localhost:3306",
            databaseSchema="metamart_db",
        )
        with patch.object(
            ClientSecretCredential,
            "get_token",
            return_value=AccessToken(token="mocked_token", expires_on=100),
        ):
            engine_connection = mysql_get_connection(connection)
            self.assertEqual(
                str(engine_connection.url),
                "mysql+pymysql://metamart_user:mocked_token@localhost:3306/metamart_db",
            )

    def test_get_connection_url_postgres(self):
        connection = PostgresConnection(
            username="metamart_user",
            authType=BasicAuth(password="metamart_password"),
            hostPort="localhost:3306",
            database="metamart_db",
        )
        engine_connection = postgres_get_connection(connection)
        self.assertEqual(
            str(engine_connection.url),
            "postgresql+psycopg2://metamart_user:metamart_password@localhost:3306/metamart_db",
        )
        connection = PostgresConnection(
            username="metamart_user",
            authType=AzureConfigurationSource(
                azureConfig=AzureCredentials(
                    clientId="clientid",
                    tenantId="tenantid",
                    clientSecret="clientsecret",
                    scopes="scope1,scope2",
                )
            ),
            hostPort="localhost:3306",
            database="metamart_db",
        )
        with patch.object(
            ClientSecretCredential,
            "get_token",
            return_value=AccessToken(token="mocked_token", expires_on=100),
        ):
            engine_connection = postgres_get_connection(connection)
            self.assertEqual(
                str(engine_connection.url),
                "postgresql+psycopg2://metamart_user:mocked_token@localhost:3306/metamart_db",
            )
