package org.metamart.service.security.auth;

import static org.metamart.schema.type.Include.NON_DELETED;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.auth.JWTAuthMechanism;
import org.metamart.schema.entity.teams.AuthenticationMechanism;
import org.metamart.schema.entity.teams.User;
import org.metamart.service.Entity;
import org.metamart.service.jdbi3.UserRepository;
import org.metamart.service.resources.teams.UserResource;
import org.metamart.service.secrets.SecretsManager;
import org.metamart.service.secrets.SecretsManagerFactory;
import org.metamart.service.util.EntityUtil.Fields;
import org.metamart.service.util.JsonUtils;

@Slf4j
public class BotTokenCache {
  public static final String EMPTY_STRING = "";
  private static final LoadingCache<String, String> BOTS_TOKEN_CACHE =
      CacheBuilder.newBuilder()
          .maximumSize(1000)
          .expireAfterWrite(2, TimeUnit.MINUTES)
          .build(new BotTokenLoader());

  private BotTokenCache() {
    // Private constructor for utility class
  }

  public static String getToken(String botName) {
    try {
      if (BOTS_TOKEN_CACHE.get(botName).equals(EMPTY_STRING)) {
        BOTS_TOKEN_CACHE.invalidate(botName);
      }
      return BOTS_TOKEN_CACHE.get(botName);
    } catch (ExecutionException | UncheckedExecutionException ex) {
      return null;
    }
  }

  public static void invalidateToken(String botName) {
    try {
      BOTS_TOKEN_CACHE.invalidate(botName);
    } catch (Exception ex) {
      LOG.error("Failed to invalidate Bot token cache for Bot {}", botName, ex);
    }
  }

  static class BotTokenLoader extends CacheLoader<String, String> {
    @Override
    public String load(@CheckForNull String botName) {
      UserRepository userRepository = (UserRepository) Entity.getEntityRepository(Entity.USER);
      User user =
          userRepository.getByName(
              null,
              botName,
              new Fields(Set.of(UserResource.USER_PROTECTED_FIELDS)),
              NON_DELETED,
              true);
      AuthenticationMechanism authenticationMechanism = user.getAuthenticationMechanism();
      SecretsManager secretsManager = SecretsManagerFactory.getSecretsManager();
      secretsManager.decryptAuthenticationMechanism(user.getName(), authenticationMechanism);
      if (authenticationMechanism != null) {
        JWTAuthMechanism jwtAuthMechanism =
            JsonUtils.convertValue(authenticationMechanism.getConfig(), JWTAuthMechanism.class);
        return jwtAuthMechanism.getJWTToken();
      }
      return null;
    }
  }
}
