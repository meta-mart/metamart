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

package org.metamart.service.apps.bundles.changeEvent.generic;

import static org.metamart.schema.entity.events.SubscriptionDestination.SubscriptionType.WEBHOOK;
import static org.metamart.service.util.SubscriptionUtil.getClient;
import static org.metamart.service.util.SubscriptionUtil.getTargetsForWebhookAlert;
import static org.metamart.service.util.SubscriptionUtil.postWebhookMessage;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.metamart.common.utils.CommonUtil;
import org.metamart.schema.entity.events.EventSubscription;
import org.metamart.schema.entity.events.SubscriptionDestination;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.schema.type.Webhook;
import org.metamart.service.apps.bundles.changeEvent.Destination;
import org.metamart.service.events.errors.EventPublisherException;
import org.metamart.service.exception.CatalogExceptionMessage;
import org.metamart.service.fernet.Fernet;
import org.metamart.service.security.SecurityUtil;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.RestUtil;

@Slf4j
public class GenericPublisher implements Destination<ChangeEvent> {
  private final Client client;
  private final Webhook webhook;
  private static final String TEST_MESSAGE_JSON =
      "This is a test message from MetaMart to confirm your webhook destination is configured correctly.";

  @Getter private final SubscriptionDestination subscriptionDestination;
  private final EventSubscription eventSubscription;

  public GenericPublisher(
      EventSubscription eventSubscription, SubscriptionDestination subscriptionDestination) {
    if (subscriptionDestination.getType() == WEBHOOK) {
      this.eventSubscription = eventSubscription;
      this.subscriptionDestination = subscriptionDestination;
      this.webhook = JsonUtils.convertValue(subscriptionDestination.getConfig(), Webhook.class);

      // Build Client
      this.client =
          getClient(subscriptionDestination.getTimeout(), subscriptionDestination.getReadTimeout());
    } else {
      throw new IllegalArgumentException(
          "GenericWebhook Alert Invoked with Illegal Type and Settings.");
    }
  }

  @Override
  public void sendMessage(ChangeEvent event) throws EventPublisherException {
    long attemptTime = System.currentTimeMillis();
    try {
      String json = JsonUtils.pojoToJson(event);

      prepareAndSendMessage(json, getTarget());

      // Post to Generic Webhook with Actions
      List<Invocation.Builder> targets =
          getTargetsForWebhookAlert(
              webhook, subscriptionDestination.getCategory(), WEBHOOK, client, event);
      String eventJson = JsonUtils.pojoToJson(event);
      for (Invocation.Builder actionTarget : targets) {
        postWebhookMessage(this, actionTarget, eventJson);
      }
    } catch (Exception ex) {
      handleException(attemptTime, event, ex);
    }
  }

  @Override
  public void sendTestMessage() throws EventPublisherException {
    long attemptTime = System.currentTimeMillis();
    try {
      prepareAndSendMessage(TEST_MESSAGE_JSON, getTarget());
    } catch (Exception ex) {
      handleException(attemptTime, ex);
    }
  }

  private void prepareAndSendMessage(String json, Invocation.Builder target) {
    if (!CommonUtil.nullOrEmpty(webhook.getEndpoint())) {

      // Add HMAC signature header if secret key is present
      if (!CommonUtil.nullOrEmpty(webhook.getSecretKey())) {
        String hmac =
            "sha256="
                + CommonUtil.calculateHMAC(decryptWebhookSecretKey(webhook.getSecretKey()), json);
        target.header(RestUtil.SIGNATURE_HEADER, hmac);
      }

      // Add custom headers if they exist
      Map<String, String> headers = webhook.getHeaders();
      if (!CommonUtil.nullOrEmpty(headers)) {
        headers.forEach(target::header);
      }

      Webhook.HttpMethod httpOperation = webhook.getHttpMethod();
      postWebhookMessage(this, target, json, httpOperation);
    }
  }

  private void handleException(long attemptTime, ChangeEvent event, Exception ex)
      throws EventPublisherException {
    handleCommonException(attemptTime, ex);

    String message =
        CatalogExceptionMessage.eventPublisherFailedToPublish(WEBHOOK, event, ex.getMessage());
    LOG.error(message);
    throw new EventPublisherException(message, Pair.of(subscriptionDestination.getId(), event));
  }

  private void handleException(long attemptTime, Exception ex) throws EventPublisherException {
    handleCommonException(attemptTime, ex);

    String message =
        CatalogExceptionMessage.eventPublisherFailedToPublish(WEBHOOK, ex.getMessage());
    LOG.error(message);
    throw new EventPublisherException(message);
  }

  private void handleCommonException(long attemptTime, Exception ex)
      throws EventPublisherException {
    Throwable cause = ex.getCause();

    if (cause.getClass() == UnknownHostException.class) {
      String message =
          String.format(
              "Unknown Host Exception for Generic Publisher : %s , WebhookEndpoint : %s",
              subscriptionDestination.getId(), webhook.getEndpoint());

      LOG.warn(message);
      setErrorStatus(attemptTime, 400, "UnknownHostException");
      throw new EventPublisherException(message);
    }
  }

  private Invocation.Builder getTarget() {
    Map<String, String> authHeaders = SecurityUtil.authHeaders("admin@meta-mart.org");
    return SecurityUtil.addHeaders(client.target(webhook.getEndpoint()), authHeaders);
  }

  @Override
  public EventSubscription getEventSubscriptionForDestination() {
    return eventSubscription;
  }

  @Override
  public boolean getEnabled() {
    return subscriptionDestination.getEnabled();
  }

  public void close() {
    if (null != client) {
      client.close();
    }
  }

  public static String decryptWebhookSecretKey(String encryptedSecretkey) {
    if (Fernet.getInstance().isKeyDefined()) {
      encryptedSecretkey = Fernet.getInstance().decryptIfApplies(encryptedSecretkey);
    }
    return encryptedSecretkey;
  }
}
