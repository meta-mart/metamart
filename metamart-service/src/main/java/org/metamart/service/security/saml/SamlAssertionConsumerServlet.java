/*
 *  Copyright 2021 DigiTrans
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metamart.service.security.saml;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.security.AuthenticationCodeFlowHandler.SESSION_REDIRECT_URI;
import static org.metamart.service.util.UserUtil.getRoleListFromUser;

import com.onelogin.saml2.Auth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.api.security.AuthorizerConfiguration;
import org.metamart.schema.auth.JWTAuthMechanism;
import org.metamart.schema.auth.RefreshToken;
import org.metamart.schema.auth.ServiceTokenType;
import org.metamart.schema.entity.teams.User;
import org.metamart.schema.type.Include;
import org.metamart.service.Entity;
import org.metamart.service.auth.JwtResponse;
import org.metamart.service.security.jwt.JWTTokenGenerator;
import org.metamart.service.util.TokenUtil;
import org.metamart.service.util.UserUtil;

/**
 * This Servlet also known as Assertion Consumer Service URL handles the SamlResponse the IDP send in response to the
 * SamlRequest. After a successful processing it redirects user to the relayState which is the callback setup in the
 * config.
 */
@WebServlet("/api/v1/saml/acs")
@Slf4j
public class SamlAssertionConsumerServlet extends HttpServlet {
  private final Set<String> admins;

  public SamlAssertionConsumerServlet(AuthorizerConfiguration configuration) {
    admins = configuration.getAdminPrincipals();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    try {
      handleResponse(req, resp);
    } catch (Exception e) {
      LOG.error("[SamlAssertionConsumerServlet] Exception :" + e.getMessage());
    }
  }

  private void handleResponse(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    Auth auth = new Auth(SamlSettingsHolder.getInstance().getSaml2Settings(), req, resp);
    auth.processResponse();
    if (!auth.isAuthenticated()) {
      LOG.error("[SAML ACS] Not Authenticated");
      resp.sendError(403, "UnAuthenticated");
    }

    List<String> errors = auth.getErrors();

    if (!errors.isEmpty()) {
      String errorReason = auth.getLastErrorReason();
      if (errorReason != null && !errorReason.isEmpty()) {
        LOG.error("[SAML ACS]" + errorReason);
        resp.sendError(500, errorReason);
      }
    } else {
      String username;
      String nameId = auth.getNameId();
      String email = nameId;
      if (nameId.contains("@")) {
        username = nameId.split("@")[0];
      } else {
        username = nameId;
        email = String.format("%s@%s", username, SamlSettingsHolder.getInstance().getDomain());
      }

      JWTAuthMechanism jwtAuthMechanism;
      User user;
      try {
        user = Entity.getEntityByName(Entity.USER, username, "id,roles", Include.NON_DELETED);
        jwtAuthMechanism =
            JWTTokenGenerator.getInstance()
                .generateJWTToken(
                    username,
                    getRoleListFromUser(user),
                    !nullOrEmpty(user.getIsAdmin()) && user.getIsAdmin(),
                    user.getEmail(),
                    SamlSettingsHolder.getInstance().getTokenValidity(),
                    false,
                    ServiceTokenType.OM_USER);
      } catch (Exception e) {
        LOG.error("[SAML ACS] User not found: " + username);
        // Create the user
        user = UserUtil.addOrUpdateUser(UserUtil.user(username, email.split("@")[1], username));
        jwtAuthMechanism =
            JWTTokenGenerator.getInstance()
                .generateJWTToken(
                    username,
                    new HashSet<>(),
                    admins.contains(username),
                    email,
                    SamlSettingsHolder.getInstance().getTokenValidity(),
                    false,
                    ServiceTokenType.OM_USER);
      }

      // Add to json response cookie
      JwtResponse jwtResponse = getJwtResponseWithRefresh(user, jwtAuthMechanism);
      Cookie refreshTokenCookie = new Cookie("refreshToken", jwtResponse.getRefreshToken());
      refreshTokenCookie.setMaxAge(60 * 60); // 1hr
      refreshTokenCookie.setPath("/"); // 30 days
      resp.addCookie(refreshTokenCookie);

      // Redirect with JWT Token
      String redirectUri = (String) req.getSession().getAttribute(SESSION_REDIRECT_URI);
      String url =
          redirectUri
              + "?id_token="
              + jwtAuthMechanism.getJWTToken()
              + "&email="
              + nameId
              + "&name="
              + username;
      resp.sendRedirect(url);
    }
  }

  private JwtResponse getJwtResponseWithRefresh(
      User storedUser, JWTAuthMechanism jwtAuthMechanism) {
    RefreshToken newRefreshToken = TokenUtil.getRefreshToken(storedUser.getId(), UUID.randomUUID());
    // save Refresh Token in Database
    Entity.getTokenRepository().insertToken(newRefreshToken);

    JwtResponse response = new JwtResponse();
    response.setTokenType("Bearer");
    response.setAccessToken(jwtAuthMechanism.getJWTToken());
    response.setRefreshToken(newRefreshToken.getToken().toString());
    response.setExpiryDuration(jwtAuthMechanism.getJWTTokenExpiresAt());
    return response;
  }
}
