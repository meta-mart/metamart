package org.metamart.schema;

import java.util.UUID;
import org.metamart.schema.auth.TokenType;

public interface TokenInterface {
  UUID getToken();

  UUID getUserId();

  TokenType getTokenType();

  Long getExpiryDate();

  void setToken(UUID id);

  void setUserId(UUID id);

  void setTokenType(TokenType type);

  void setExpiryDate(Long expiry);
}
