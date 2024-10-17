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

import static org.metamart.common.utils.CommonUtil.listOrEmpty;
import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.security.SecurityUtil.findEmailFromClaims;
import static org.metamart.service.security.SecurityUtil.findUserNameFromClaims;
import static org.metamart.service.security.SecurityUtil.isBot;
import static org.metamart.service.security.SecurityUtil.validateDomainEnforcement;
import static org.metamart.service.security.SecurityUtil.validatePrincipalClaimsMapping;
import static org.metamart.service.security.jwt.JWTTokenGenerator.ROLES_CLAIM;
import static org.metamart.service.security.jwt.JWTTokenGenerator.TOKEN_TYPE;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.metamart.schema.api.security.AuthenticationConfiguration;
import org.metamart.schema.api.security.AuthorizerConfiguration;
import org.metamart.schema.auth.LogoutRequest;
import org.metamart.schema.auth.ServiceTokenType;
import org.metamart.schema.services.connections.metadata.AuthProvider;
import org.metamart.service.security.auth.BotTokenCache;
import org.metamart.service.security.auth.CatalogSecurityContext;
import org.metamart.service.security.auth.UserTokenCache;
import org.metamart.service.security.saml.JwtTokenCacheManager;

@Slf4j
@Provider
public class JwtFilter implements ContainerRequestFilter {
  public static final String EMAIL_CLAIM_KEY = "email";
  public static final String USERNAME_CLAIM_KEY = "username";
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String TOKEN_PREFIX = "Bearer";
  public static final String BOT_CLAIM = "isBot";
  @Getter private List<String> jwtPrincipalClaims;
  @Getter private Map<String, String> jwtPrincipalClaimsMapping;
  private JwkProvider jwkProvider;
  private String principalDomain;
  private boolean enforcePrincipalDomain;
  private AuthProvider providerType;
  private boolean useRolesFromProvider = false;

  private static final List<String> DEFAULT_PUBLIC_KEY_URLS =
      Arrays.asList(
          "http://localhost:8585/api/v1/system/config/jwks",
          "http://host.docker.internal:8585/api/v1/system/config/jwks");
  public static final List<String> EXCLUDED_ENDPOINTS =
      List.of(
          "v1/system/config/jwks",
          "v1/system/config/authorizer",
          "v1/system/config/customUiThemePreference",
          "v1/system/config/auth",
          "v1/users/signup",
          "v1/system/version",
          "v1/users/registrationConfirmation",
          "v1/users/resendRegistrationToken",
          "v1/users/generatePasswordResetLink",
          "v1/users/password/reset",
          "v1/users/checkEmailInUse",
          "v1/users/login",
          "v1/users/refresh");

  @SuppressWarnings("unused")
  private JwtFilter() {}

  @SneakyThrows
  public JwtFilter(
      AuthenticationConfiguration authenticationConfiguration,
      AuthorizerConfiguration authorizerConfiguration) {
    this.providerType = authenticationConfiguration.getProvider();
    // Cannot remove  Principal Claims listing since that is , breaking change for existing users
    this.jwtPrincipalClaims = authenticationConfiguration.getJwtPrincipalClaims();
    this.jwtPrincipalClaimsMapping =
        listOrEmpty(authenticationConfiguration.getJwtPrincipalClaimsMapping()).stream()
            .map(s -> s.split(":"))
            .collect(Collectors.toMap(s -> s[0], s -> s[1]));
    validatePrincipalClaimsMapping(jwtPrincipalClaimsMapping);

    ImmutableList.Builder<URL> publicKeyUrlsBuilder = ImmutableList.builder();
    for (String publicKeyUrlStr : authenticationConfiguration.getPublicKeyUrls()) {
      publicKeyUrlsBuilder.add(new URL(publicKeyUrlStr));
    }
    // avoid users misconfiguration and add default publicKeyUrls
    for (String publicKeyUrl : DEFAULT_PUBLIC_KEY_URLS) {
      if (!authenticationConfiguration.getPublicKeyUrls().contains(publicKeyUrl)) {
        publicKeyUrlsBuilder.add(new URL(publicKeyUrl));
      }
    }

    this.jwkProvider = new MultiUrlJwkProvider(publicKeyUrlsBuilder.build());
    this.principalDomain = authorizerConfiguration.getPrincipalDomain();
    this.enforcePrincipalDomain = authorizerConfiguration.getEnforcePrincipalDomain();
    this.useRolesFromProvider = authorizerConfiguration.getUseRolesFromProvider();
  }

  @VisibleForTesting
  JwtFilter(
      JwkProvider jwkProvider,
      List<String> jwtPrincipalClaims,
      String principalDomain,
      boolean enforcePrincipalDomain) {
    this.jwkProvider = jwkProvider;
    this.jwtPrincipalClaims = jwtPrincipalClaims;
    this.principalDomain = principalDomain;
    this.enforcePrincipalDomain = enforcePrincipalDomain;
  }

  @SneakyThrows
  @Override
  public void filter(ContainerRequestContext requestContext) {
    UriInfo uriInfo = requestContext.getUriInfo();
    if (EXCLUDED_ENDPOINTS.stream()
        .anyMatch(endpoint -> uriInfo.getPath().equalsIgnoreCase(endpoint))) {
      return;
    }

    // Extract token from the header
    String tokenFromHeader = extractToken(requestContext.getHeaders());
    LOG.debug("Token from header:{}", tokenFromHeader);

    Map<String, Claim> claims = validateJwtAndGetClaims(tokenFromHeader);
    String userName = findUserNameFromClaims(jwtPrincipalClaimsMapping, jwtPrincipalClaims, claims);
    String email =
        findEmailFromClaims(jwtPrincipalClaimsMapping, jwtPrincipalClaims, claims, principalDomain);

    // Check Validations
    checkValidationsForToken(claims, tokenFromHeader, userName);

    // Setting Security Context
    CatalogPrincipal catalogPrincipal = new CatalogPrincipal(userName, email);
    String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
    CatalogSecurityContext catalogSecurityContext =
        new CatalogSecurityContext(
            catalogPrincipal,
            scheme,
            SecurityContext.DIGEST_AUTH,
            getUserRolesFromClaims(claims, isBot(claims)));
    LOG.debug("SecurityContext {}", catalogSecurityContext);
    requestContext.setSecurityContext(catalogSecurityContext);
  }

  public void checkValidationsForToken(
      Map<String, Claim> claims, String tokenFromHeader, String userName) {
    // the case where OMD generated the Token for the Client in case OM generated Token
    validateTokenIsNotUsedAfterLogout(tokenFromHeader);

    // Validate Domain
    validateDomainEnforcement(
        jwtPrincipalClaimsMapping,
        jwtPrincipalClaims,
        claims,
        principalDomain,
        enforcePrincipalDomain);

    // Validate Bot token matches what was created in OM
    if (isBot(claims)) {
      validateBotToken(tokenFromHeader, userName);
    }

    // validate personal access token
    validatePersonalAccessToken(claims, tokenFromHeader, userName);
  }

  private Set<String> getUserRolesFromClaims(Map<String, Claim> claims, boolean isBot) {
    Set<String> userRoles = new HashSet<>();
    // Re-sync user roles from token
    if (useRolesFromProvider && !isBot && claims.containsKey(ROLES_CLAIM)) {
      List<String> roles = claims.get(ROLES_CLAIM).asList(String.class);
      if (!nullOrEmpty(roles)) {
        userRoles = new HashSet<>(claims.get(ROLES_CLAIM).asList(String.class));
      }
    }
    return userRoles;
  }

  @SneakyThrows
  public Map<String, Claim> validateJwtAndGetClaims(String token) {
    // Decode JWT Token
    DecodedJWT jwt;
    try {
      jwt = JWT.decode(token);
    } catch (JWTDecodeException e) {
      throw AuthenticationException.getInvalidTokenException("Unable to decode the token.");
    }

    // Check if expired
    // If expiresAt is set to null, treat it as never expiring token
    if (jwt.getExpiresAt() != null
        && jwt.getExpiresAt().before(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime())) {
      throw AuthenticationException.getExpiredTokenException();
    }

    // Validate JWT with public key
    Jwk jwk = jwkProvider.get(jwt.getKeyId());
    Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
    try {
      algorithm.verify(jwt);
    } catch (RuntimeException runtimeException) {
      throw AuthenticationException.getInvalidTokenException(
          "Token verification failed. Public key mismatch.", runtimeException);
    }

    Map<String, Claim> claims = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    claims.putAll(jwt.getClaims());

    return claims;
  }

  protected static String extractToken(MultivaluedMap<String, String> headers) {
    LOG.debug("Request Headers:{}", headers);
    String source = headers.getFirst(AUTHORIZATION_HEADER);
    if (nullOrEmpty(source)) {
      throw AuthenticationException.getTokenNotPresentException();
    }
    // Extract the bearer token
    if (source.startsWith(TOKEN_PREFIX)) {
      return source.substring(TOKEN_PREFIX.length() + 1);
    }
    throw AuthenticationException.getTokenNotPresentException();
  }

  public static String extractToken(String tokenFromHeader) {
    LOG.debug("Request Token:{}", tokenFromHeader);
    if (nullOrEmpty(tokenFromHeader)) {
      throw AuthenticationException.getTokenNotPresentException();
    }
    // Extract the bearer token
    if (tokenFromHeader.startsWith(TOKEN_PREFIX)) {
      return tokenFromHeader.substring(TOKEN_PREFIX.length() + 1);
    }
    throw AuthenticationException.getTokenNotPresentException();
  }

  private void validateBotToken(String tokenFromHeader, String userName) {
    if (tokenFromHeader.equals(BotTokenCache.getToken(userName))) {
      return;
    }
    throw AuthenticationException.getInvalidTokenException(
        "The given token does not match the current bot's token!");
  }

  private void validatePersonalAccessToken(
      Map<String, Claim> claims, String tokenFromHeader, String userName) {
    if (claims.containsKey(TOKEN_TYPE)
        && ServiceTokenType.PERSONAL_ACCESS
            .value()
            .equals(
                claims.get(TOKEN_TYPE) != null
                    ? StringUtils.EMPTY
                    : claims.get(TOKEN_TYPE).asString())) {
      Set<String> userTokens = UserTokenCache.getToken(userName);
      if (userTokens != null && userTokens.contains(tokenFromHeader)) {
        return;
      }
      throw AuthenticationException.getInvalidTokenException("Invalid personal access token!");
    }
  }

  private void validateTokenIsNotUsedAfterLogout(String authToken) {
    // Only OMD generated Tokens
    if (AuthProvider.BASIC.equals(providerType) || AuthProvider.SAML.equals(providerType)) {
      LogoutRequest previouslyLoggedOutEvent =
          JwtTokenCacheManager.getInstance().getLogoutEventForToken(authToken);
      if (previouslyLoggedOutEvent != null) {
        throw AuthenticationException.getExpiredTokenException();
      }
    }
  }
}
