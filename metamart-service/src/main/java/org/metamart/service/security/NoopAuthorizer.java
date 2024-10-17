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

import java.util.List;
import javax.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.api.teams.CreateUser;
import org.metamart.schema.entity.teams.User;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.Permission.Access;
import org.metamart.schema.type.ResourcePermission;
import org.metamart.service.Entity;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.exception.EntityNotFoundException;
import org.metamart.service.jdbi3.UserRepository;
import org.metamart.service.security.policyevaluator.OperationContext;
import org.metamart.service.security.policyevaluator.PolicyEvaluator;
import org.metamart.service.security.policyevaluator.ResourceContextInterface;
import org.metamart.service.util.RestUtil.PutResponse;
import org.metamart.service.util.UserUtil;

@Slf4j
public class NoopAuthorizer implements Authorizer {
  @Override
  public void init(MetaMartApplicationConfig metaMartApplicationConfig) {
    addAnonymousUser();
  }

  @Override
  public List<ResourcePermission> listPermissions(SecurityContext securityContext, String user) {
    // Return all operations.
    return PolicyEvaluator.getResourcePermissions(Access.ALLOW);
  }

  @Override
  public ResourcePermission getPermission(
      SecurityContext securityContext, String user, String resource) {
    return PolicyEvaluator.getResourcePermission(resource, Access.ALLOW);
  }

  @Override
  public ResourcePermission getPermission(
      SecurityContext securityContext, String user, ResourceContextInterface resourceContext) {
    return PolicyEvaluator.getResourcePermission(resourceContext.getResource(), Access.ALLOW);
  }

  @Override
  public void authorize(
      SecurityContext securityContext,
      OperationContext operationContext,
      ResourceContextInterface resourceContext) {
    /* Always authorize */
  }

  private void addAnonymousUser() {
    String username = "anonymous";
    try {
      Entity.getEntityByName(Entity.USER, username, "", Include.NON_DELETED);
    } catch (EntityNotFoundException ex) {
      User user =
          UserUtil.getUser(
              username, new CreateUser().withName(username).withEmail(username + "@domain.com"));
      addOrUpdateUser(user);
    } catch (Exception e) {
      LOG.error("Failed to create anonymous user {}", username, e);
    }
  }

  private void addOrUpdateUser(User user) {
    try {
      UserRepository userRepository = (UserRepository) Entity.getEntityRepository(Entity.USER);
      PutResponse<User> addedUser = userRepository.createOrUpdate(null, user);
      LOG.debug("Added anonymous user entry: {}", addedUser);
    } catch (Exception exception) {
      // In HA set up the other server may have already added the user.
      LOG.debug("Caught exception ", exception);
      LOG.debug("Anonymous user entry: {} already exists.", user);
    }
  }

  @Override
  public void authorizeAdmin(SecurityContext securityContext) {
    /* Always authorize */
  }

  @Override
  public void authorizeAdminOrBot(SecurityContext securityContext) {
    /* Always authorize */
  }

  @Override
  public boolean shouldMaskPasswords(SecurityContext securityContext) {
    return false; // Always show passwords
  }

  @Override
  public boolean authorizePII(SecurityContext securityContext, List<EntityReference> owners) {
    return true; // Always show PII Sensitive data
  }
}
