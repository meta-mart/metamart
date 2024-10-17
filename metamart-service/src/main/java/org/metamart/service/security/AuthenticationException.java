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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.Getter;

public class AuthenticationException extends RuntimeException {
  @Getter private final transient Response response;

  public AuthenticationException(String msg) {
    super(msg);
    response =
        Response.status(Response.Status.UNAUTHORIZED)
            .entity(convertToErrorResponseMessage(msg))
            .type(MediaType.APPLICATION_JSON_TYPE)
            .build();
  }

  public AuthenticationException(String msg, Throwable cause) {
    super(msg, cause);
    response =
        Response.status(Response.Status.UNAUTHORIZED)
            .entity(convertToErrorResponseMessage(msg))
            .type(MediaType.APPLICATION_JSON_TYPE)
            .build();
  }

  public static AuthenticationException getTokenNotPresentException() {
    String msg = "Not Authorized! Token not present";
    return new AuthenticationException(msg);
  }

  public static AuthenticationException getInvalidTokenException(String reason) {
    String msg = String.format("Not Authorized! %s", reason);
    return new AuthenticationException(msg);
  }

  public static AuthenticationException getInvalidTokenException(String reason, Exception e) {
    String msg = String.format("Not Authorized! %s due to %s", reason, e);
    return new AuthenticationException(msg);
  }

  public static AuthenticationException getExpiredTokenException() {
    String msg = "Expired token!";
    return new AuthenticationException(msg);
  }

  private static ErrorResponse convertToErrorResponseMessage(String msg) {
    return new ErrorResponse(msg);
  }

  private record ErrorResponse(String responseMessage) {}
}
