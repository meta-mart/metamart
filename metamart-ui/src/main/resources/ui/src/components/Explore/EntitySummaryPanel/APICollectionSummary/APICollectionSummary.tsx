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

import { Col, Divider, Row } from 'antd';
import { get } from 'lodash';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { TabSpecificField } from '../../../../enums/entity.enum';
import { ExplorePageTabs } from '../../../../enums/Explore.enum';
import { APICollection } from '../../../../generated/entity/data/apiCollection';
import { getApiCollectionByFQN } from '../../../../rest/apiCollectionsAPI';
import { getSortedTagsWithHighlight } from '../../../../utils/EntitySummaryPanelUtils';
import {
  DRAWER_NAVIGATION_OPTIONS,
  getEntityOverview,
} from '../../../../utils/EntityUtils';
import { OwnerLabel } from '../../../common/OwnerLabel/OwnerLabel.component';
import SummaryPanelSkeleton from '../../../common/Skeleton/SummaryPanelSkeleton/SummaryPanelSkeleton.component';
import SummaryTagsDescription from '../../../common/SummaryTagsDescription/SummaryTagsDescription.component';
import { SearchedDataProps } from '../../../SearchedData/SearchedData.interface';
import CommonEntitySummaryInfo from '../CommonEntitySummaryInfo/CommonEntitySummaryInfo';

interface APICollectionSummaryProps {
  entityDetails: APICollection;
  componentType?: DRAWER_NAVIGATION_OPTIONS;
  isLoading?: boolean;
  highlights?: SearchedDataProps['data'][number]['highlight'];
}

const APICollectionSummary = ({
  entityDetails,
  componentType = DRAWER_NAVIGATION_OPTIONS.explore,
  isLoading,
  highlights,
}: APICollectionSummaryProps) => {
  const entityInfo = useMemo(
    () => getEntityOverview(ExplorePageTabs.API_COLLECTION, entityDetails),
    [entityDetails]
  );
  const [apiCollectionDetails, setApiCollectionDetails] =
    useState<APICollection>(entityDetails);

  const isExplore = useMemo(
    () => componentType === DRAWER_NAVIGATION_OPTIONS.explore,
    [componentType]
  );

  const ownerDetails = useMemo(() => {
    const owners = entityDetails.owners;

    return {
      value: <OwnerLabel hasPermission={false} owners={owners} />,
    };
  }, [entityDetails, apiCollectionDetails]);

  const fetchApiCollectionDetails = useCallback(async () => {
    try {
      const res = await getApiCollectionByFQN(
        entityDetails.fullyQualifiedName ?? '',
        {
          fields: [TabSpecificField.TAGS, TabSpecificField.OWNERS],
        }
      );

      setApiCollectionDetails({ ...res });
    } catch (error) {
      // Error
    }
  }, [entityDetails]);

  useEffect(() => {
    if (entityDetails.service?.type === 'apiService') {
      fetchApiCollectionDetails();
    }
  }, [entityDetails, componentType]);

  return (
    <SummaryPanelSkeleton loading={Boolean(isLoading)}>
      <>
        <Row className="m-md m-t-0" gutter={[0, 4]}>
          {!isExplore ? (
            <Col className="p-b-md" span={24}>
              {ownerDetails.value}
            </Col>
          ) : null}
          <Col span={24}>
            <CommonEntitySummaryInfo
              componentType={componentType}
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
      </>
    </SummaryPanelSkeleton>
  );
};

export default APICollectionSummary;
