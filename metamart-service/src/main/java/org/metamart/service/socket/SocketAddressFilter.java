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

package org.metamart.service.socket;

import com.auth0.jwt.interfaces.Claim;
import io.socket.engineio.server.utils.ParseQS;
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.api.security.AuthenticationConfiguration;
import org.metamart.schema.api.security.AuthorizerConfiguration;
import org.metamart.service.security.JwtFilter;
import org.metamart.service.security.SecurityUtil;

@Slf4j
public class SocketAddressFilter implements Filter {
  private JwtFilter jwtFilter;
  private final boolean enableSecureSocketConnection;

  public SocketAddressFilter(
      AuthenticationConfiguration authenticationConfiguration,
      AuthorizerConfiguration authorizerConf) {
    enableSecureSocketConnection = authorizerConf.getEnableSecureSocketConnection();
    if (enableSecureSocketConnection) {
      jwtFilter = new JwtFilter(authenticationConfiguration, authorizerConf);
    }
  }

  public SocketAddressFilter() {
    enableSecureSocketConnection = false;
  }

  @Override
  public void destroy() {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException {
    try {
      HttpServletRequest httpServletRequest = (HttpServletRequest) request;
      Map<String, String> query = ParseQS.decode(httpServletRequest.getQueryString());

      HeaderRequestWrapper requestWrapper = new HeaderRequestWrapper(httpServletRequest);
      requestWrapper.addHeader("RemoteAddress", httpServletRequest.getRemoteAddr());
      requestWrapper.addHeader("UserId", query.get("userId"));

      if (enableSecureSocketConnection) {
        String tokenWithType = httpServletRequest.getHeader("Authorization");
        requestWrapper.addHeader("Authorization", tokenWithType);
        validatePrefixedTokenRequest(jwtFilter, tokenWithType);
      }
      // Goes to default servlet.
      chain.doFilter(requestWrapper, response);
    } catch (Exception ex) {
      LOG.error("[SAFilter] Failed in filtering request: {}", ex.getMessage());
      response
          .getWriter()
          .println(String.format("[SAFilter] Failed in filtering request: %s", ex.getMessage()));
    }
  }

  @Override
  public void init(FilterConfig filterConfig) {}

  public static void validatePrefixedTokenRequest(JwtFilter jwtFilter, String prefixedToken) {
    String token = JwtFilter.extractToken(prefixedToken);
    Map<String, Claim> claims = jwtFilter.validateJwtAndGetClaims(token);
    String userName =
        SecurityUtil.findUserNameFromClaims(
            jwtFilter.getJwtPrincipalClaimsMapping(), jwtFilter.getJwtPrincipalClaims(), claims);
    jwtFilter.checkValidationsForToken(claims, token, userName);
  }
}
