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

package org.metamart.service.formatter.entity;

import static org.metamart.service.formatter.util.FormatterUtil.transformMessage;

import org.metamart.schema.entity.feed.Thread;
import org.metamart.schema.type.FieldChange;
import org.metamart.service.formatter.decorators.MessageDecorator;
import org.metamart.service.formatter.util.FormatterUtil;

public class DefaultEntityFormatter implements EntityFormatter {
  @Override
  public String format(
      MessageDecorator<?> messageFormatter,
      Thread thread,
      FieldChange fieldChange,
      FormatterUtil.CHANGE_TYPE changeType) {
    return transformMessage(messageFormatter, thread, fieldChange, changeType);
  }
}
