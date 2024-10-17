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
import org.metamart.service.apps.bundles.changeEvent.msteams.TeamsMessage;
import org.metamart.service.exception.UnhandledServerException;

public class MSTeamsMessageDecorator implements MessageDecorator<TeamsMessage> {

  @Override
  public String getBold() {
    return "**%s**";
  }

  @Override
  public String getLineBreak() {
    return " <br/> ";
  }

  @Override
  public String getAddMarker() {
    return "**";
  }

  @Override
  public String getAddMarkerClose() {
    return "** ";
  }

  @Override
  public String getRemoveMarker() {
    return "~~";
  }

  @Override
  public String getRemoveMarkerClose() {
    return "~~ ";
  }

  @Override
  public String getEntityUrl(String prefix, String fqn, String additionalParams) {
    return String.format(
        "[%s](/%s/%s%s)",
        fqn.trim(),
        getSmtpSettings().getMetaMartUrl(),
        prefix,
        nullOrEmpty(additionalParams) ? "" : String.format("/%s", additionalParams));
  }

  @Override
  public TeamsMessage buildEntityMessage(String publisherName, ChangeEvent event) {
    return getTeamMessage(createEntityMessage(publisherName, event));
  }

  @Override
  public TeamsMessage buildTestMessage(String publisherName) {
    return getTeamTestMessage(publisherName);
  }

  @Override
  public TeamsMessage buildThreadMessage(String publisherName, ChangeEvent event) {
    return getTeamMessage(createThreadMessage(publisherName, event));
  }

  private TeamsMessage getTeamMessage(OutgoingMessage outgoingMessage) {
    if (!outgoingMessage.getMessages().isEmpty()) {
      TeamsMessage teamsMessage = new TeamsMessage();
      teamsMessage.setSummary("Change Event From MetaMart");

      // Sections
      TeamsMessage.Section teamsSections = new TeamsMessage.Section();
      teamsSections.setActivityTitle(outgoingMessage.getHeader());
      List<TeamsMessage.Section> attachmentList = new ArrayList<>();
      outgoingMessage
          .getMessages()
          .forEach(m -> attachmentList.add(getTeamsSection(teamsSections.getActivityTitle(), m)));

      teamsMessage.setSections(attachmentList);
      return teamsMessage;
    }
    throw new UnhandledServerException("No messages found for the event");
  }

  private TeamsMessage getTeamTestMessage(String publisherName) {
    if (!publisherName.isEmpty()) {
      TeamsMessage teamsMessage = new TeamsMessage();
      teamsMessage.setSummary(
          "This is a test message from MetaMart to confirm your Microsoft Teams destination is configured correctly.");

      // Sections
      TeamsMessage.Section teamsSection = new TeamsMessage.Section();
      teamsSection.setActivityTitle("Alert: " + publisherName);

      List<TeamsMessage.Section> sectionList = new ArrayList<>();
      sectionList.add(teamsSection);

      teamsMessage.setSections(sectionList);
      return teamsMessage;
    }
    throw new UnhandledServerException("Publisher name not found.");
  }

  private TeamsMessage.Section getTeamsSection(String activityTitle, String message) {
    TeamsMessage.Section section = new TeamsMessage.Section();
    section.setActivityTitle(activityTitle);
    section.setActivityText(message);
    return section;
  }
}
