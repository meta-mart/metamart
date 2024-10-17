package org.metamart.service.security;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@WebServlet("/api/v1/auth/login")
@Slf4j
public class AuthLoginServlet extends HttpServlet {
  private final AuthenticationCodeFlowHandler authenticationCodeFlowHandler;

  public AuthLoginServlet(AuthenticationCodeFlowHandler authenticationCodeFlowHandler) {
    this.authenticationCodeFlowHandler = authenticationCodeFlowHandler;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    authenticationCodeFlowHandler.handleLogin(req, resp);
  }
}
