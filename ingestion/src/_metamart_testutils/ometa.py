from metadata.generated.schema.entity.services.connections.metadata.metaMartConnection import (
    AuthProvider,
    MetaMartConnection,
)
from metadata.generated.schema.security.client.metaMartJWTClientConfig import (
    MetaMartJWTClientConfig,
)
from metadata.ingestion.models.custom_pydantic import CustomSecretStr
from metadata.ingestion.ometa.ometa_api import MetaMart

OM_JWT = "eyJraWQiOiJHYjM4OWEtOWY3Ni1nZGpzLWE5MmotMDI0MmJrOTQzNTYiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlzQm90IjpmYWxzZSwiaXNzIjoib3Blbi1tZXRhZGF0YS5vcmciLCJpYXQiOjE2NjM5Mzg0NjIsImVtYWlsIjoiYWRtaW5Ab3Blbm1ldGFkYXRhLm9yZyJ9.tS8um_5DKu7HgzGBzS1VTA5uUjKWOCU0B_j08WXBiEC0mr0zNREkqVfwFDD-d24HlNEbrqioLsBuFRiwIWKc1m_ZlVQbG7P36RUxhuv2vbSp80FKyNM-Tj93FDzq91jsyNmsQhyNv_fNr3TXfzzSPjHt8Go0FMMP66weoKMgW2PbXlhVKwEuXUHyakLLzewm9UMeQaEiRzhiTMU3UkLXcKbYEJJvfNFcLwSl9W8JCO_l0Yj3ud-qt_nQYEZwqW6u5nfdQllN133iikV4fM5QZsMCnm8Rq1mvLR0y9bmJiD7fwM1tmJ791TUWqmKaTnP49U493VanKpUAfzIiOiIbhg"


def int_admin_ometa(
    url: str = "http://localhost:8585/api", jwt: str = OM_JWT
) -> MetaMart:
    """Initialize the ometa connection with default admin:admin creds"""
    server_config = MetaMartConnection(
        hostPort=url,
        authProvider=AuthProvider.metamart,
        securityConfig=MetaMartJWTClientConfig(jwtToken=CustomSecretStr(jwt)),
    )
    metadata = MetaMart(server_config)
    assert metadata.health_check()
    return metadata
