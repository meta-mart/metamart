package org.metamart.service.security.auth;

import static org.metamart.service.exception.CatalogExceptionMessage.AUTHENTICATOR_OPERATION_NOT_SUPPORTED;
import static org.metamart.service.exception.CatalogExceptionMessage.FORBIDDEN_AUTHENTICATOR_OP;

import javax.ws.rs.core.Response;
import org.metamart.schema.auth.LoginRequest;
import org.metamart.schema.entity.teams.User;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.auth.JwtResponse;
import org.metamart.service.exception.CustomExceptionMessage;

public class NoopAuthenticator implements AuthenticatorHandler {
  @Override
  public void init(MetaMartApplicationConfig config) {
    /* deprecated unused */
  }

  @Override
  public JwtResponse loginUser(LoginRequest loginRequest) {
    throw new CustomExceptionMessage(
        Response.Status.FORBIDDEN,
        AUTHENTICATOR_OPERATION_NOT_SUPPORTED,
        FORBIDDEN_AUTHENTICATOR_OP);
  }

  @Override
  public void checkIfLoginBlocked(String userName) {
    throw new CustomExceptionMessage(
        Response.Status.FORBIDDEN,
        AUTHENTICATOR_OPERATION_NOT_SUPPORTED,
        FORBIDDEN_AUTHENTICATOR_OP);
  }

  @Override
  public void recordFailedLoginAttempt(String providedIdentity, String userName) {
    throw new CustomExceptionMessage(
        Response.Status.FORBIDDEN,
        AUTHENTICATOR_OPERATION_NOT_SUPPORTED,
        FORBIDDEN_AUTHENTICATOR_OP);
  }

  @Override
  public void validatePassword(String providedIdentity, String reqPassword, User storedUser) {
    throw new CustomExceptionMessage(
        Response.Status.FORBIDDEN,
        AUTHENTICATOR_OPERATION_NOT_SUPPORTED,
        FORBIDDEN_AUTHENTICATOR_OP);
  }

  @Override
  public User lookUserInProvider(String email, String pwd) {
    throw new CustomExceptionMessage(
        Response.Status.FORBIDDEN,
        AUTHENTICATOR_OPERATION_NOT_SUPPORTED,
        FORBIDDEN_AUTHENTICATOR_OP);
  }
}
