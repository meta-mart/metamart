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

package org.metamart.service.apps.bundles.changeEvent.slack;

import static org.metamart.schema.entity.events.SubscriptionDestination.SubscriptionType.SLACK;
import static org.metamart.service.util.SubscriptionUtil.appendHeadersToTarget;
import static org.metamart.service.util.SubscriptionUtil.getClient;
import static org.metamart.service.util.SubscriptionUtil.getTargetsForWebhookAlert;
import static org.metamart.service.util.SubscriptionUtil.postWebhookMessage;

import java.util.List;
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
import org.metamart.service.formatter.decorators.MessageDecorator;
import org.metamart.service.formatter.decorators.SlackMessageDecorator;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.RestUtil;

@Slf4j
public class SlackEventPublisher implements Destination<ChangeEvent> {
  private final MessageDecorator<SlackMessage> slackMessageFormatter = new SlackMessageDecorator();
  private final Webhook webhook;
  private Invocation.Builder target;
  private final Client client;
  @Getter private final SubscriptionDestination subscriptionDestination;
  private final EventSubscription eventSubscription;

  public SlackEventPublisher(
      EventSubscription eventSubscription, SubscriptionDestination subscriptionDest) {
    if (subscriptionDest.getType() == SLACK) {
      this.eventSubscription = eventSubscription;
      this.subscriptionDestination = subscriptionDest;
      this.webhook = JsonUtils.convertValue(subscriptionDest.getConfig(), Webhook.class);

      // Build Client
      client = getClient(subscriptionDest.getTimeout(), subscriptionDest.getReadTimeout());

      // Build Target
      if (webhook != null && webhook.getEndpoint() != null) {
        String slackWebhookURL = webhook.getEndpoint().toString();
        if (!CommonUtil.nullOrEmpty(slackWebhookURL)) {
          target = appendHeadersToTarget(client, slackWebhookURL);
        }
      }
    } else {
      throw new IllegalArgumentException("Slack Alert Invoked with Illegal Type and Settings.");
    }
  }

  @Override
  public void sendMessage(ChangeEvent event) throws EventPublisherException {
    try {
      SlackMessage slackMessage =
          slackMessageFormatter.buildOutgoingMessage(
              eventSubscription.getFullyQualifiedName(), event);
      List<Invocation.Builder> targets =
          getTargetsForWebhookAlert(
              webhook, subscriptionDestination.getCategory(), SLACK, client, event);
      if (target != null) {
        targets.add(target);
      }
      for (Invocation.Builder actionTarget : targets) {
        if (webhook.getSecretKey() != null && !webhook.getSecretKey().isEmpty()) {
          String hmac =
              "sha256="
                  + CommonUtil.calculateHMAC(
                      webhook.getSecretKey(), JsonUtils.pojoToJson(slackMessage));
          postWebhookMessage(
              this, actionTarget.header(RestUtil.SIGNATURE_HEADER, hmac), slackMessage);
        } else {
          postWebhookMessage(this, actionTarget, slackMessage);
        }
      }
    } catch (Exception e) {
      String message =
          CatalogExceptionMessage.eventPublisherFailedToPublish(SLACK, event, e.getMessage());
      LOG.error(message);
      throw new EventPublisherException(message, Pair.of(subscriptionDestination.getId(), event));
    }
  }

  @Override
  public void sendTestMessage() throws EventPublisherException {
    try {
      SlackMessage slackMessage =
          slackMessageFormatter.buildOutgoingTestMessage(eventSubscription.getFullyQualifiedName());

      if (target != null) {
        if (webhook.getSecretKey() != null && !webhook.getSecretKey().isEmpty()) {
          String hmac =
              "sha256="
                  + CommonUtil.calculateHMAC(
                      webhook.getSecretKey(), JsonUtils.pojoToJson(slackMessage));
          postWebhookMessage(this, target.header(RestUtil.SIGNATURE_HEADER, hmac), slackMessage);
        } else {
          postWebhookMessage(this, target, slackMessage);
        }
      }
    } catch (Exception e) {
      String message = CatalogExceptionMessage.eventPublisherFailedToPublish(SLACK, e.getMessage());
      LOG.error(message);
      throw new EventPublisherException(message);
    }
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
      LOG.info("Closing Slack Client");
      client.close();
    }
  }
}
