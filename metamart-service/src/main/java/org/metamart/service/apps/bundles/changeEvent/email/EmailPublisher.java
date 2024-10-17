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

package org.metamart.service.apps.bundles.changeEvent.email;

import static org.metamart.schema.entity.events.SubscriptionDestination.SubscriptionType.EMAIL;
import static org.metamart.service.util.SubscriptionUtil.getTargetsForAlert;

import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.metamart.schema.alert.type.EmailAlertConfig;
import org.metamart.schema.entity.events.EventSubscription;
import org.metamart.schema.entity.events.SubscriptionDestination;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.service.Entity;
import org.metamart.service.apps.bundles.changeEvent.Destination;
import org.metamart.service.events.errors.EventPublisherException;
import org.metamart.service.exception.CatalogExceptionMessage;
import org.metamart.service.formatter.decorators.EmailMessageDecorator;
import org.metamart.service.formatter.decorators.MessageDecorator;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.email.EmailUtil;

@Slf4j
public class EmailPublisher implements Destination<ChangeEvent> {
  private final MessageDecorator<EmailMessage> emailDecorator = new EmailMessageDecorator();
  private final EmailAlertConfig emailAlertConfig;
  private final CollectionDAO daoCollection;

  @Getter private final SubscriptionDestination subscriptionDestination;
  private final EventSubscription eventSubscription;

  public EmailPublisher(
      EventSubscription eventSubscription, SubscriptionDestination subscriptionDestination) {
    if (subscriptionDestination.getType() == EMAIL) {
      this.eventSubscription = eventSubscription;
      this.subscriptionDestination = subscriptionDestination;
      this.emailAlertConfig =
          JsonUtils.convertValue(subscriptionDestination.getConfig(), EmailAlertConfig.class);
      this.daoCollection = Entity.getCollectionDAO();
    } else {
      throw new IllegalArgumentException("Email Alert Invoked with Illegal Type and Settings.");
    }
  }

  @Override
  public void sendMessage(ChangeEvent event) throws EventPublisherException {
    try {
      Set<String> receivers =
          getTargetsForAlert(emailAlertConfig, subscriptionDestination.getCategory(), EMAIL, event);
      EmailMessage emailMessage =
          emailDecorator.buildOutgoingMessage(eventSubscription.getFullyQualifiedName(), event);
      for (String email : receivers) {
        EmailUtil.sendChangeEventMail(
            eventSubscription.getFullyQualifiedName(), email, emailMessage);
      }
      setSuccessStatus(System.currentTimeMillis());
    } catch (Exception e) {
      setErrorStatus(System.currentTimeMillis(), 500, e.getMessage());
      String message =
          CatalogExceptionMessage.eventPublisherFailedToPublish(EMAIL, event, e.getMessage());
      LOG.error(message);
      throw new EventPublisherException(message, Pair.of(subscriptionDestination.getId(), event));
    }
  }

  @Override
  public void sendTestMessage() throws EventPublisherException {
    try {
      Set<String> receivers = emailAlertConfig.getReceivers();
      EmailMessage emailMessage =
          emailDecorator.buildOutgoingTestMessage(eventSubscription.getFullyQualifiedName());
      for (String email : receivers) {
        EmailUtil.sendChangeEventMail(
            eventSubscription.getFullyQualifiedName(), email, emailMessage);
      }
      setSuccessStatus(System.currentTimeMillis());
    } catch (Exception e) {
      setErrorStatus(System.currentTimeMillis(), 500, e.getMessage());
      String message = CatalogExceptionMessage.eventPublisherFailedToPublish(EMAIL, e.getMessage());
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
    LOG.debug("Email Publisher Stopped");
  }
}
