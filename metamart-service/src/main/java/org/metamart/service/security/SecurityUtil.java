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

package org.metamart.service.security;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.security.JwtFilter.BOT_CLAIM;
import static org.metamart.service.security.JwtFilter.EMAIL_CLAIM_KEY;
import static org.metamart.service.security.JwtFilter.USERNAME_CLAIM_KEY;

import com.auth0.jwt.interfaces.Claim;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.metamart.common.utils.CommonUtil;
import org.metamart.schema.api.configuration.LoginConfiguration;
import org.metamart.schema.settings.SettingsType;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.resources.settings.SettingsCache;

@Slf4j
public final class SecurityUtil {
  public static final String DEFAULT_PRINCIPAL_DOMAIN = "metamart.org";

  private SecurityUtil() {}

  public static String getUserName(SecurityContext securityContext) {
    Principal principal = securityContext.getUserPrincipal();
    return principal == null ? null : principal.getName().split("[/@]")[0];
  }

  public static LoginConfiguration getLoginConfiguration() {
    return SettingsCache.getSetting(SettingsType.LOGIN_CONFIGURATION, LoginConfiguration.class);
  }

  public static Map<String, String> authHeaders(String username) {
    Builder<String, String> builder = ImmutableMap.builder();
    if (username != null) {
      builder.put(CatalogOpenIdAuthorizationRequestFilter.X_AUTH_PARAMS_EMAIL_HEADER, username);
    }
    return builder.build();
  }

  public static String getPrincipalName(Map<String, String> authHeaders) {
    // Get username from the email address
    if (authHeaders == null) {
      return null;
    }
    String principal =
        authHeaders.get(CatalogOpenIdAuthorizationRequestFilter.X_AUTH_PARAMS_EMAIL_HEADER);
    return principal == null ? null : principal.split("@")[0];
  }

  public static String getDomain(MetaMartApplicationConfig config) {
    String principalDomain = config.getAuthorizerConfiguration().getPrincipalDomain();
    return CommonUtil.nullOrEmpty(principalDomain) ? DEFAULT_PRINCIPAL_DOMAIN : principalDomain;
  }

  public static Invocation.Builder addHeaders(WebTarget target, Map<String, String> headers) {
    if (headers != null) {
      return target
          .request()
          .header(
              CatalogOpenIdAuthorizationRequestFilter.X_AUTH_PARAMS_EMAIL_HEADER,
              headers.get(CatalogOpenIdAuthorizationRequestFilter.X_AUTH_PARAMS_EMAIL_HEADER));
    }
    return target.request();
  }

  public static String findUserNameFromClaims(
      Map<String, String> jwtPrincipalClaimsMapping,
      List<String> jwtPrincipalClaimsOrder,
      Map<String, ?> claims) {
    String userName;
    if (!nullOrEmpty(jwtPrincipalClaimsMapping)) {
      // We have a mapping available so we will use that
      String usernameClaim = jwtPrincipalClaimsMapping.get(USERNAME_CLAIM_KEY);
      String userNameClaimValue = getClaimOrObject(claims.get(usernameClaim));
      if (!nullOrEmpty(userNameClaimValue)) {
        userName = userNameClaimValue;
      } else {
        throw new AuthenticationException("Invalid JWT token, 'username' claim is not present");
      }
    } else {
      String jwtClaim = getFirstMatchJwtClaim(jwtPrincipalClaimsOrder, claims);
      userName = jwtClaim.contains("@") ? jwtClaim.split("@")[0] : jwtClaim;
    }
    return userName.toLowerCase();
  }

  public static String findEmailFromClaims(
      Map<String, String> jwtPrincipalClaimsMapping,
      List<String> jwtPrincipalClaimsOrder,
      Map<String, ?> claims,
      String defaulPrincipalClaim) {
    String email;
    if (!nullOrEmpty(jwtPrincipalClaimsMapping)) {
      // We have a mapping available so we will use that
      String emailClaim = jwtPrincipalClaimsMapping.get(EMAIL_CLAIM_KEY);
      String emailClaimValue = getClaimOrObject(claims.get(emailClaim));
      if (!nullOrEmpty(emailClaimValue) && emailClaimValue.contains("@")) {
        email = emailClaimValue;
      } else {
        throw new AuthenticationException(
            String.format(
                "Invalid JWT token, 'email' claim is not present or invalid : %s",
                emailClaimValue));
      }
    } else {
      String jwtClaim = getFirstMatchJwtClaim(jwtPrincipalClaimsOrder, claims);
      email =
          jwtClaim.contains("@")
              ? jwtClaim
              : String.format("%s@%s", jwtClaim, defaulPrincipalClaim);
    }
    return email.toLowerCase();
  }

  public static String getClaimOrObject(Object obj) {
    if (obj == null) {
      return "";
    }

    if (obj instanceof Claim c) {
      return c.asString();
    } else if (obj instanceof String s) {
      return s;
    }

    return StringUtils.EMPTY;
  }

  public static String getFirstMatchJwtClaim(
      List<String> jwtPrincipalClaimsOrder, Map<String, ?> claims) {
    return jwtPrincipalClaimsOrder.stream()
        .filter(claims::containsKey)
        .findFirst()
        .map(claims::get)
        .map(SecurityUtil::getClaimOrObject)
        .orElseThrow(
            () ->
                new AuthenticationException(
                    "Invalid JWT token, none of the following claims are present "
                        + jwtPrincipalClaimsOrder));
  }

  public static void validatePrincipalClaimsMapping(Map<String, String> mapping) {
    if (!nullOrEmpty(mapping)) {
      String username = mapping.get(USERNAME_CLAIM_KEY);
      String email = mapping.get(EMAIL_CLAIM_KEY);
      if (nullOrEmpty(username) || nullOrEmpty(email)) {
        throw new IllegalArgumentException(
            "Invalid JWT Principal Claims Mapping. Both username and email should be present");
      }
    }
    // If emtpy, jwtPrincipalClaims will be used so no need to validate
  }

  public static void validateDomainEnforcement(
      Map<String, String> jwtPrincipalClaimsMapping,
      List<String> jwtPrincipalClaimsOrder,
      Map<String, Claim> claims,
      String principalDomain,
      boolean enforcePrincipalDomain) {
    String domain = StringUtils.EMPTY;
    if (!nullOrEmpty(jwtPrincipalClaimsMapping)) {
      // We have a mapping available so we will use that
      String emailClaim = jwtPrincipalClaimsMapping.get(EMAIL_CLAIM_KEY);
      String emailClaimValue = getClaimOrObject(claims.get(emailClaim));
      if (!nullOrEmpty(emailClaimValue)) {
        if (emailClaimValue.contains("@")) {
          domain = emailClaimValue.split("@")[1];
        }
      } else {
        throw new AuthenticationException("Invalid JWT token, 'email' claim is not present");
      }
    } else {
      String jwtClaim = getFirstMatchJwtClaim(jwtPrincipalClaimsOrder, claims);
      if (jwtClaim.contains("@")) {
        domain = jwtClaim.split("@")[1];
      }
    }

    // Validate
    if (!isBot(claims) && (enforcePrincipalDomain && !domain.equals(principalDomain))) {
      throw new AuthenticationException(
          String.format(
              "Not Authorized! Email does not match the principal domain %s", principalDomain));
    }
  }

  public static void writeJsonResponse(HttpServletResponse response, String message)
      throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getOutputStream().print(message);
    response.getOutputStream().flush();
    response.setStatus(HttpServletResponse.SC_OK);
  }

  public static boolean isBot(Map<String, Claim> claims) {
    return claims.containsKey(BOT_CLAIM) && Boolean.TRUE.equals(claims.get(BOT_CLAIM).asBoolean());
  }
}
