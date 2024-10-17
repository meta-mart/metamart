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
import java.util.List;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.service.apps.bundles.changeEvent.gchat.GChatMessage;
import org.metamart.service.exception.UnhandledServerException;

public class GChatMessageDecorator implements MessageDecorator<GChatMessage> {

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
        "<%s/%s/%s%s|%s>",
        getSmtpSettings().getMetaMartUrl(),
        prefix,
        fqn.trim().replace(" ", "%20"),
        nullOrEmpty(additionalParams) ? "" : String.format("/%s", additionalParams),
        fqn.trim());
  }

  @Override
  public GChatMessage buildEntityMessage(String publisherName, ChangeEvent event) {
    return getGChatMessage(createEntityMessage(publisherName, event));
  }

  @Override
  public GChatMessage buildTestMessage(String publisherName) {
    return getGChatTestMessage(publisherName);
  }

  @Override
  public GChatMessage buildThreadMessage(String publisherName, ChangeEvent event) {
    return getGChatMessage(createThreadMessage(publisherName, event));
  }

  private GChatMessage getGChatMessage(OutgoingMessage outgoingMessage) {
    if (!outgoingMessage.getMessages().isEmpty()) {
      GChatMessage gChatMessage = new GChatMessage();
      GChatMessage.CardsV2 cardsV2 = new GChatMessage.CardsV2();
      GChatMessage.Card card = new GChatMessage.Card();
      GChatMessage.Section section = new GChatMessage.Section();

      // Header
      gChatMessage.setText("Change Event from MetaMart");
      GChatMessage.CardHeader cardHeader = new GChatMessage.CardHeader();
      cardHeader.setTitle(outgoingMessage.getHeader());
      card.setHeader(cardHeader);

      // Attachments
      List<GChatMessage.Widget> widgets = new ArrayList<>();
      outgoingMessage.getMessages().forEach(m -> widgets.add(getGChatWidget(m)));
      section.setWidgets(widgets);
      card.setSections(List.of(section));
      cardsV2.setCard(card);
      gChatMessage.setCardsV2(List.of(cardsV2));

      return gChatMessage;
    }
    throw new UnhandledServerException("No messages found for the event");
  }

  private GChatMessage getGChatTestMessage(String publisherName) {
    if (!publisherName.isEmpty()) {
      GChatMessage gChatMessage = new GChatMessage();
      GChatMessage.CardsV2 cardsV2 = new GChatMessage.CardsV2();
      GChatMessage.Card card = new GChatMessage.Card();
      GChatMessage.Section section = new GChatMessage.Section();

      // Header
      gChatMessage.setText(
          "This is a test message from MetaMart to confirm your GChat destination is configured correctly.");
      GChatMessage.CardHeader cardHeader = new GChatMessage.CardHeader();
      cardHeader.setTitle("Alert: " + publisherName);
      cardHeader.setSubtitle("GChat destination test successful.");

      card.setHeader(cardHeader);
      card.setSections(List.of(section));
      cardsV2.setCard(card);
      gChatMessage.setCardsV2(List.of(cardsV2));

      return gChatMessage;
    }
    throw new UnhandledServerException("Publisher name not found.");
  }

  private GChatMessage.Widget getGChatWidget(String message) {
    GChatMessage.Widget widget = new GChatMessage.Widget();
    widget.setTextParagraph(new GChatMessage.TextParagraph(message));
    return widget;
  }
}
