/*
 *  Copyright 2022 DigiTrans.
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

import { Col, Row, Tag, Typography } from 'antd';
import cronstrue from 'cronstrue';
import { t } from 'i18next';
import { capitalize, isUndefined } from 'lodash';
import React from 'react';
import { ReactComponent as TimeDateIcon } from '../assets/svg/time-date.svg';
import { NO_DATA_PLACEHOLDER } from '../constants/constants';
import { PIPELINE_INGESTION_RUN_STATUS } from '../constants/pipeline.constants';
import { IngestionPipeline } from '../generated/entity/services/ingestionPipelines/ingestionPipeline';
import { getEntityName } from './EntityUtils';

export const renderNameField = (_: string, record: IngestionPipeline) => (
  <Typography.Text
    className="m-b-0 d-block break-word"
    data-testid="pipeline-name">
    {getEntityName(record)}
  </Typography.Text>
);

export const renderTypeField = (_: string, record: IngestionPipeline) => (
  <Typography.Text
    className="m-b-0 d-block break-word"
    data-testid="pipeline-type">
    {record.pipelineType}
  </Typography.Text>
);

export const renderStatusField = (_: string, record: IngestionPipeline) => (
  <Tag
    className="ingestion-run-badge latest"
    color={PIPELINE_INGESTION_RUN_STATUS[record.enabled ? 'success' : 'paused']}
    data-testid="pipeline-active-status">
    {record.enabled ? t('label.active-uppercase') : t('label.paused-uppercase')}
  </Tag>
);

export const renderScheduleField = (_: string, record: IngestionPipeline) => {
  if (isUndefined(record.airflowConfig?.scheduleInterval)) {
    return (
      <Typography.Text data-testid="scheduler-no-data">
        {NO_DATA_PLACEHOLDER}
      </Typography.Text>
    );
  }
  const scheduleDescription = cronstrue.toString(
    record.airflowConfig.scheduleInterval,
    {
      use24HourTimeFormat: false,
      verbose: true,
    }
  );

  const firstSentenceEndIndex = scheduleDescription.indexOf(',');

  const descriptionFirstPart = scheduleDescription
    .slice(0, firstSentenceEndIndex)
    .trim();

  const descriptionSecondPart = capitalize(
    scheduleDescription.slice(firstSentenceEndIndex + 1).trim()
  );

  return (
    <Row gutter={[8, 8]} wrap={false}>
      <Col>
        <TimeDateIcon className="m-t-xss" height={20} width={20} />
      </Col>
      <Col>
        <Row className="line-height-16">
          <Col span={24}>
            <Typography.Text
              className="font-medium"
              data-testid="schedule-primary-details">
              {descriptionFirstPart}
            </Typography.Text>
          </Col>
          <Col span={24}>
            <Typography.Text
              className="text-xs text-grey-muted"
              data-testid="schedule-secondary-details">
              {descriptionSecondPart}
            </Typography.Text>
          </Col>
        </Row>
      </Col>
    </Row>
  );
};
