package org.metamart.service.limits;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jdbi.v3.core.Jdbi;
import org.metamart.schema.configuration.LimitsConfiguration;
import org.metamart.schema.system.LimitsConfig;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.security.policyevaluator.OperationContext;
import org.metamart.service.security.policyevaluator.ResourceContextInterface;

public class DefaultLimits implements Limits {
  private MetaMartApplicationConfig serverConfig = null;
  private LimitsConfiguration limitsConfiguration = null;
  private Jdbi jdbi = null;

  @Override
  public void init(MetaMartApplicationConfig serverConfig, Jdbi jdbi) {
    this.serverConfig = serverConfig;
    this.limitsConfiguration = serverConfig.getLimitsConfiguration();
    this.jdbi = jdbi;
  }

  @Override
  public void enforceLimits(
      SecurityContext securityContext,
      ResourceContextInterface resourceContext,
      OperationContext operationContext) {
    // do not enforce limits
  }

  @Override
  public LimitsConfig getLimitsConfig() {
    LimitsConfig limitsConfig = new LimitsConfig();
    limitsConfig.setEnable(limitsConfiguration.getEnable());
    return limitsConfig;
  }

  @Override
  public Response getLimitsForaFeature(String name, boolean cache) {
    return Response.ok().build();
  }

  @Override
  public void invalidateCache(String entityType) {}
}
