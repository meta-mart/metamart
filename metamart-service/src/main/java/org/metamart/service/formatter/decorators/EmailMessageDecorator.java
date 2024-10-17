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

package org.metamart.service.formatter.decorators;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.util.email.EmailUtil.getSmtpSettings;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.service.apps.bundles.changeEvent.email.EmailMessage;
import org.metamart.service.exception.UnhandledServerException;

public class EmailMessageDecorator implements MessageDecorator<EmailMessage> {
  @Override
  public String getBold() {
    return "<b>%s</b>";
  }

  @Override
  public String getLineBreak() {
    return " <br/> ";
  }

  @Override
  public String getAddMarker() {
    return "<b>";
  }

  @Override
  public String getAddMarkerClose() {
    return "</b>";
  }

  @Override
  public String getRemoveMarker() {
    return "<s>";
  }

  @Override
  public String getRemoveMarkerClose() {
    return "</s>";
  }

  @Override
  public String getEntityUrl(String prefix, String fqn, String additionalParams) {
    return String.format(
        "<a href = '%s/%s/%s%s'>%s</a>",
        getSmtpSettings().getMetaMartUrl(),
        prefix,
        fqn.trim(),
        nullOrEmpty(additionalParams) ? "" : String.format("/%s", additionalParams),
        fqn.trim());
  }

  @Override
  public EmailMessage buildEntityMessage(String publisherName, ChangeEvent event) {
    return getEmailMessage(createEntityMessage(publisherName, event));
  }

  @Override
  public EmailMessage buildTestMessage(String publisherName) {
    return getEmailTestMessage(publisherName);
  }

  @Override
  public EmailMessage buildThreadMessage(String publisherName, ChangeEvent event) {
    return getEmailMessage(createThreadMessage(publisherName, event));
  }

  public EmailMessage getEmailMessage(OutgoingMessage outgoingMessage) {
    if (!outgoingMessage.getMessages().isEmpty()) {
      EmailMessage emailMessage = new EmailMessage();
      emailMessage.setUserName(outgoingMessage.getUserName());
      emailMessage.setEntityUrl(outgoingMessage.getEntityUrl());
      emailMessage.setUpdatedBy(outgoingMessage.getUserName());
      emailMessage.setChangeMessage(new ArrayList<>(outgoingMessage.getMessages()));
      return emailMessage;
    }
    throw new UnhandledServerException("No messages found for the event");
  }

  public EmailMessage getEmailTestMessage(String publisherName) {
    if (!publisherName.isEmpty()) {
      EmailMessage emailMessage = new EmailMessage();
      emailMessage.setUserName("test_user");
      emailMessage.setUpdatedBy("system");
      emailMessage.setEntityUrl(StringUtils.EMPTY);
      emailMessage.setChangeMessage(
          new ArrayList<>(
              Collections.singleton(
                  "This is a test alert to verify the destination configuration for alerts. "
                      + "Publisher: "
                      + publisherName
                      + ". If you received this message, your alert "
                      + "configuration is correct.")));
      return emailMessage;
    }
    throw new UnhandledServerException("Publisher name not found.");
  }
}
