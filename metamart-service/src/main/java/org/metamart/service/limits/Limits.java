package org.metamart.service.limits;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jdbi.v3.core.Jdbi;
import org.metamart.schema.system.LimitsConfig;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.security.policyevaluator.OperationContext;
import org.metamart.service.security.policyevaluator.ResourceContextInterface;

public interface Limits {
  void init(MetaMartApplicationConfig serverConfig, Jdbi jdbi);

  void enforceLimits(
      SecurityContext securityContext,
      ResourceContextInterface resourceContext,
      OperationContext operationContext);

  LimitsConfig getLimitsConfig();

  Response getLimitsForaFeature(String entityType, boolean cache);

  void invalidateCache(String entityType);
}
