/*
 *  Copyright 2023 DigiTrans.
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

import { Space, Tooltip } from 'antd';
import { isEmpty } from 'lodash';
import React, { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { useHistory } from 'react-router-dom';
import { ReactComponent as IconRequest } from '../../../assets/svg/request-icon.svg';
import { FQN_SEPARATOR_CHAR } from '../../../constants/char.constants';
import { DE_ACTIVE_COLOR } from '../../../constants/constants';
import { ENTITY_TASKS_TOOLTIP } from '../../../constants/entity.constants';
import { EntityField } from '../../../constants/Feeds.constants';
import { TagSource } from '../../../generated/type/tagLabel';
import { getPartialNameFromTableFQN } from '../../../utils/CommonUtils';
import { getFieldThreadElement } from '../../../utils/FeedElementUtils';
import {
  getEntityTaskDetails,
  getRequestDescriptionPath,
  getRequestTagsPath,
  getUpdateDescriptionPath,
  getUpdateTagsPath,
} from '../../../utils/TasksUtils';
import { EntityTasksProps } from './EntityTasks.interface';

const EntityTasks = ({
  data,
  tagSource,
  entityFqn,
  entityType,
  entityTaskType,
  onThreadLinkSelect,
}: EntityTasksProps) => {
  const { t } = useTranslation();
  const history = useHistory();

  const { fqnPart, entityField } = useMemo(
    () => getEntityTaskDetails(entityType),
    [entityType]
  );

  const columnName = useMemo(() => {
    const columnName = getPartialNameFromTableFQN(data.fqn ?? '', fqnPart);

    return columnName.includes(FQN_SEPARATOR_CHAR)
      ? `"${columnName}"`
      : columnName;
  }, [data.fqn]);

  const handleTask = (hasData: boolean) => {
    if (entityTaskType === EntityField.DESCRIPTION) {
      history.push(
        (hasData ? getUpdateDescriptionPath : getRequestDescriptionPath)(
          entityType,
          entityFqn,
          entityField,
          columnName
        )
      );
    } else {
      history.push(
        (hasData ? getUpdateTagsPath : getRequestTagsPath)(
          entityType,
          entityFqn,
          entityField,
          columnName
        )
      );
    }
  };

  const taskElement = useMemo(() => {
    const hasData = !isEmpty(data.field);

    return (
      <Tooltip
        destroyTooltipOnHide
        overlayClassName="ant-popover-request-description"
        title={
          hasData
            ? ENTITY_TASKS_TOOLTIP[entityTaskType].update
            : ENTITY_TASKS_TOOLTIP[entityTaskType].request
        }>
        <IconRequest
          className="hover-cell-icon cursor-pointer"
          data-testid="task-element"
          height={14}
          name={t('label.request-tag-plural')}
          style={{ color: DE_ACTIVE_COLOR }}
          width={14}
          onClick={() => handleTask(hasData)}
        />
      </Tooltip>
    );
  }, [data.field]);

  return (
    <Space data-testid="entity-task" size="middle">
      {/*  Request and Update Tasks */}
      {tagSource !== TagSource.Glossary && taskElement}

      {/*  List Conversation */}
      {getFieldThreadElement(
        onThreadLinkSelect,
        entityType,
        entityFqn,
        columnName,
        entityField,
        entityTaskType
      )}
    </Space>
  );
};

export default EntityTasks;
