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

package org.metamart.service.formatter.factory;

import static org.metamart.service.Entity.FIELD_ASSETS;
import static org.metamart.service.Entity.FIELD_DESCRIPTION;
import static org.metamart.service.Entity.FIELD_DOMAIN;
import static org.metamart.service.Entity.FIELD_EXTENSION;
import static org.metamart.service.Entity.FIELD_FOLLOWERS;
import static org.metamart.service.Entity.FIELD_OWNERS;
import static org.metamart.service.Entity.FIELD_TAGS;
import static org.metamart.service.formatter.field.TestCaseResultFormatter.TEST_RESULT_FIELD;

import org.metamart.schema.entity.feed.Thread;
import org.metamart.schema.type.FieldChange;
import org.metamart.service.Entity;
import org.metamart.service.formatter.decorators.MessageDecorator;
import org.metamart.service.formatter.entity.DefaultEntityFormatter;
import org.metamart.service.formatter.entity.EntityFormatter;
import org.metamart.service.formatter.entity.IngestionPipelineFormatter;
import org.metamart.service.formatter.entity.KpiFormatter;
import org.metamart.service.formatter.entity.PipelineFormatter;
import org.metamart.service.formatter.entity.QueryFormatter;
import org.metamart.service.formatter.field.AssetsFieldFormatter;
import org.metamart.service.formatter.field.CustomPropertiesFormatter;
import org.metamart.service.formatter.field.DefaultFieldFormatter;
import org.metamart.service.formatter.field.DescriptionFormatter;
import org.metamart.service.formatter.field.DomainFormatter;
import org.metamart.service.formatter.field.FollowersFormatter;
import org.metamart.service.formatter.field.OwnerFormatter;
import org.metamart.service.formatter.field.TagFormatter;
import org.metamart.service.formatter.field.TestCaseResultFormatter;

public final class ParserFactory {
  private ParserFactory() {}

  public static EntityFormatter getEntityParser(String entityType) {
    // Handle Thread entity separately
    if (entityType.equals(Entity.THREAD)) {
      throw new IllegalArgumentException("Thread entity cannot be handled by Entity Parser.");
    }
    return switch (entityType) {
      case Entity.QUERY -> new QueryFormatter();
      case Entity.KPI -> new KpiFormatter();
      case Entity.INGESTION_PIPELINE -> new IngestionPipelineFormatter();
      case Entity.PIPELINE -> new PipelineFormatter();
      default -> new DefaultEntityFormatter();
    };
  }

  public static DefaultFieldFormatter getFieldParserObject(
      MessageDecorator<?> decorator,
      Thread thread,
      FieldChange fieldChange,
      String fieldChangeName) {
    return switch (fieldChangeName) {
      case FIELD_TAGS -> new TagFormatter(decorator, thread, fieldChange);
      case FIELD_FOLLOWERS -> new FollowersFormatter(decorator, thread, fieldChange);
      case FIELD_OWNERS -> new OwnerFormatter(decorator, thread, fieldChange);
      case FIELD_DESCRIPTION -> new DescriptionFormatter(decorator, thread, fieldChange);
      case FIELD_DOMAIN -> new DomainFormatter(decorator, thread, fieldChange);
      case FIELD_EXTENSION -> new CustomPropertiesFormatter(decorator, thread, fieldChange);
      case TEST_RESULT_FIELD -> new TestCaseResultFormatter(decorator, thread, fieldChange);
      case FIELD_ASSETS -> new AssetsFieldFormatter(decorator, thread, fieldChange);
      default -> new DefaultFieldFormatter(decorator, thread, fieldChange);
    };
  }
}
