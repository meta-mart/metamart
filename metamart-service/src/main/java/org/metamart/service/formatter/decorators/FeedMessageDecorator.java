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

import org.metamart.schema.type.ChangeEvent;
import org.metamart.service.formatter.util.FeedMessage;

public class FeedMessageDecorator implements MessageDecorator<FeedMessage> {

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
    return "<span class=\"diff-added\">";
  }

  @Override
  public String getAddMarkerClose() {
    return "</span>";
  }

  @Override
  public String getRemoveMarker() {
    return "<span class=\"diff-removed\">";
  }

  @Override
  public String getRemoveMarkerClose() {
    return "</span>";
  }

  @Override
  public String getEntityUrl(String prefix, String fqn, String additionalParams) {
    return String.format(
        "[%s](/%s/%s%s)",
        fqn,
        prefix,
        fqn.trim(),
        nullOrEmpty(additionalParams) ? "" : String.format("/%s", additionalParams));
  }

  @Override
  public FeedMessage buildEntityMessage(String publisherName, ChangeEvent event) {
    return null;
  }

  @Override
  public FeedMessage buildTestMessage(String publisherName) {
    return null;
  }

  @Override
  public FeedMessage buildThreadMessage(String publisherName, ChangeEvent event) {
    return null;
  }
}
