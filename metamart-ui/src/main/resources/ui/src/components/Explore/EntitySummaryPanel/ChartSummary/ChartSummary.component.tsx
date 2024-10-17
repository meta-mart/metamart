/*
 *  Copyright 2024 DigiTrans.
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
import { Col, Divider, Row, Typography } from 'antd';
import { get } from 'lodash';
import React, { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { SummaryEntityType } from '../../../../enums/EntitySummary.enum';
import { ExplorePageTabs } from '../../../../enums/Explore.enum';
import { Chart } from '../../../../generated/entity/data/chart';
import {
  getFormattedEntityData,
  getSortedTagsWithHighlight,
} from '../../../../utils/EntitySummaryPanelUtils';
import {
  DRAWER_NAVIGATION_OPTIONS,
  getEntityOverview,
} from '../../../../utils/EntityUtils';
import SummaryTagsDescription from '../../../common/SummaryTagsDescription/SummaryTagsDescription.component';
import { SearchedDataProps } from '../../../SearchedData/SearchedData.interface';
import CommonEntitySummaryInfo from '../CommonEntitySummaryInfo/CommonEntitySummaryInfo';
import SummaryList from '../SummaryList/SummaryList.component';
import { BasicEntityInfo } from '../SummaryList/SummaryList.interface';

interface ChartsSummaryProps {
  entityDetails: Chart;
  highlights?: SearchedDataProps['data'][number]['highlight'];
}

const ChartSummary = ({ entityDetails, highlights }: ChartsSummaryProps) => {
  const { t } = useTranslation();
  const entityInfo = useMemo(
    () => getEntityOverview(ExplorePageTabs.CHARTS, entityDetails),
    [entityDetails]
  );
  const formattedDashboardData: BasicEntityInfo[] = useMemo(
    () =>
      getFormattedEntityData(
        SummaryEntityType.DASHBOARD,
        entityDetails.dashboards
      ),
    [entityDetails.dashboards]
  );

  return (
    <>
      <Row className="m-md m-t-0" gutter={[0, 4]}>
        <Col span={24}>
          <CommonEntitySummaryInfo
            componentType={DRAWER_NAVIGATION_OPTIONS.explore}
            entityInfo={entityInfo}
          />
        </Col>
      </Row>
      <Divider className="m-y-xs" />

      <SummaryTagsDescription
        entityDetail={entityDetails}
        tags={getSortedTagsWithHighlight(
          entityDetails.tags,
          get(highlights, 'tag.name')
        )}
      />

      <Divider className="m-y-xs" />

      <Row className="m-md" gutter={[0, 8]}>
        <Col span={24}>
          <Typography.Text
            className="summary-panel-section-title"
            data-testid="charts-header">
            {t('label.dashboard-plural')}
          </Typography.Text>
        </Col>
        <Col span={24}>
          <SummaryList formattedEntityData={formattedDashboardData} />
        </Col>
      </Row>
    </>
  );
};

export default ChartSummary;
