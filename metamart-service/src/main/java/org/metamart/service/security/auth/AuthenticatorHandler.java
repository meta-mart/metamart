package org.metamart.service.security.auth;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.exception.CatalogExceptionMessage.NOT_IMPLEMENTED_METHOD;
import static org.metamart.service.util.UserUtil.getRoleListFromUser;

import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.UUID;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.metamart.schema.api.teams.CreateUser;
import org.metamart.schema.auth.ChangePasswordRequest;
import org.metamart.schema.auth.JWTAuthMechanism;
import org.metamart.schema.auth.LoginRequest;
import org.metamart.schema.auth.PasswordResetRequest;
import org.metamart.schema.auth.RefreshToken;
import org.metamart.schema.auth.RegistrationRequest;
import org.metamart.schema.auth.ServiceTokenType;
import org.metamart.schema.auth.TokenRefreshRequest;
import org.metamart.schema.entity.teams.User;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.auth.JwtResponse;
import org.metamart.service.exception.CustomExceptionMessage;
import org.metamart.service.security.jwt.JWTTokenGenerator;

public interface AuthenticatorHandler {
  String NOT_IMPLEMENTED_ERROR_TYPE = "NOT_IMPLEMENTED";

  void init(MetaMartApplicationConfig config);

  JwtResponse loginUser(LoginRequest loginRequest) throws IOException, TemplateException;

  void checkIfLoginBlocked(String userName);

  void recordFailedLoginAttempt(String email, String userName)
      throws TemplateException, IOException;

  void validatePassword(String providedIdentity, String reqPassword, User omUser)
      throws TemplateException, IOException;

  User lookUserInProvider(String email, String pwd) throws TemplateException, IOException;

  default User registerUser(RegistrationRequest registrationRequest) {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default void sendEmailVerification(UriInfo uriInfo, User user) throws IOException {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default void confirmEmailRegistration(UriInfo uriInfo, String emailToken) {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default void resendRegistrationToken(UriInfo uriInfo, User registeredUser) throws IOException {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default void sendPasswordResetLink(
      UriInfo uriInfo, User user, String subject, String templateFilePath) throws IOException {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default void resetUserPasswordWithToken(UriInfo uriInfo, PasswordResetRequest req)
      throws IOException {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default void changeUserPwdWithOldPwd(UriInfo uriInfo, String userName, ChangePasswordRequest req)
      throws IOException {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default RefreshToken createRefreshTokenForLogin(UUID currentUserId) {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default JwtResponse getNewAccessToken(TokenRefreshRequest request) {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default void sendInviteMailToUser(
      UriInfo uriInfo,
      User user,
      String subject,
      CreateUser.CreatePasswordType requestType,
      String pwd)
      throws IOException {
    throw new CustomExceptionMessage(
        Response.Status.NOT_IMPLEMENTED, NOT_IMPLEMENTED_ERROR_TYPE, NOT_IMPLEMENTED_METHOD);
  }

  default JwtResponse getJwtResponse(User storedUser, long expireInSeconds) {
    RefreshToken refreshToken = createRefreshTokenForLogin(storedUser.getId());
    JWTAuthMechanism jwtAuthMechanism =
        JWTTokenGenerator.getInstance()
            .generateJWTToken(
                storedUser.getName(),
                getRoleListFromUser(storedUser),
                !nullOrEmpty(storedUser.getIsAdmin()) && storedUser.getIsAdmin(),
                storedUser.getEmail(),
                expireInSeconds,
                false,
                ServiceTokenType.OM_USER);

    JwtResponse response = new JwtResponse();
    response.setTokenType("Bearer");
    response.setAccessToken(jwtAuthMechanism.getJWTToken());
    response.setRefreshToken(refreshToken.getToken().toString());
    response.setExpiryDuration(jwtAuthMechanism.getJWTTokenExpiresAt());
    return response;
  }
}
