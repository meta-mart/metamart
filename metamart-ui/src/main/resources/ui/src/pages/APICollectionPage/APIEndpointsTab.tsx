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

import { Col, Row, Switch, Typography } from 'antd';
import { ColumnsType } from 'antd/lib/table';
import { isEmpty } from 'lodash';
import { PagingResponse } from 'Models';
import React, { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import DescriptionV1 from '../../components/common/EntityDescription/DescriptionV1';
import ErrorPlaceHolder from '../../components/common/ErrorWithPlaceholder/ErrorPlaceHolder';
import NextPrevious from '../../components/common/NextPrevious/NextPrevious';
import { NextPreviousProps } from '../../components/common/NextPrevious/NextPrevious.interface';
import RichTextEditorPreviewer from '../../components/common/RichTextEditor/RichTextEditorPreviewer';
import TableAntd from '../../components/common/Table/Table';
import { NO_DATA, PAGE_SIZE } from '../../constants/constants';
import { ERROR_PLACEHOLDER_TYPE } from '../../enums/common.enum';
import { EntityType } from '../../enums/entity.enum';
import { APICollection } from '../../generated/entity/data/apiCollection';
import { APIEndpoint } from '../../generated/entity/data/apiEndpoint';
import entityUtilClassBase from '../../utils/EntityUtilClassBase';
import { getEntityName } from '../../utils/EntityUtils';

interface APIEndpointsTabProps {
  apiCollectionDetails: APICollection;
  apiEndpointsLoading: boolean;
  description: string;
  editDescriptionPermission?: boolean;
  isEdit?: boolean;
  showDeletedEndpoints?: boolean;
  apiEndpoints: PagingResponse<APIEndpoint[]>;
  currentEndpointsPage: number;
  endpointPaginationHandler: NextPreviousProps['pagingHandler'];
  onCancel?: () => void;
  onDescriptionEdit?: () => void;
  onDescriptionUpdate?: (updatedHTML: string) => Promise<void>;
  onThreadLinkSelect?: (link: string) => void;
  onShowDeletedEndpointsChange?: (value: boolean) => void;
  isVersionView?: boolean;
}

function APIEndpointsTab({
  apiCollectionDetails,
  apiEndpointsLoading,
  description,
  editDescriptionPermission = false,
  isEdit = false,
  apiEndpoints,
  currentEndpointsPage,
  endpointPaginationHandler,
  onCancel,
  onDescriptionEdit,
  onDescriptionUpdate,
  onThreadLinkSelect,
  showDeletedEndpoints = false,
  onShowDeletedEndpointsChange,
  isVersionView = false,
}: Readonly<APIEndpointsTabProps>) {
  const { t } = useTranslation();

  const tableColumn: ColumnsType<APIEndpoint> = useMemo(
    () => [
      {
        title: t('label.name'),
        dataIndex: 'name',
        key: 'name',
        width: 400,
        render: (_, record: APIEndpoint) => {
          return (
            <div className="d-inline-flex w-max-90">
              <Link
                className="break-word"
                data-testid={record.name}
                to={entityUtilClassBase.getEntityLink(
                  EntityType.API_ENDPOINT,
                  record.fullyQualifiedName as string
                )}>
                {getEntityName(record)}
              </Link>
            </div>
          );
        },
      },
      {
        title: t('label.request-method'),
        dataIndex: 'requestMethod',
        key: 'requestMethod',

        render: (requestMethod: APIEndpoint['requestMethod']) => {
          return <Typography.Text>{requestMethod || NO_DATA}</Typography.Text>;
        },
      },
      {
        title: t('label.description'),
        dataIndex: 'description',
        key: 'description',
        render: (text: string) =>
          text?.trim() ? (
            <RichTextEditorPreviewer markdown={text} />
          ) : (
            <span className="text-grey-muted">{t('label.no-description')}</span>
          ),
      },
    ],
    []
  );

  return (
    <Row gutter={[16, 16]}>
      <Col data-testid="description-container" span={24}>
        {isVersionView ? (
          <DescriptionV1
            description={description}
            entityFqn={apiCollectionDetails.fullyQualifiedName}
            entityType={EntityType.API_COLLECTION}
            isDescriptionExpanded={isEmpty(apiEndpoints.data)}
            showActions={false}
          />
        ) : (
          <DescriptionV1
            description={description}
            entityFqn={apiCollectionDetails.fullyQualifiedName}
            entityName={getEntityName(apiCollectionDetails)}
            entityType={EntityType.API_COLLECTION}
            hasEditAccess={editDescriptionPermission}
            isDescriptionExpanded={isEmpty(apiEndpoints.data)}
            isEdit={isEdit}
            showActions={!apiCollectionDetails.deleted}
            onCancel={onCancel}
            onDescriptionEdit={onDescriptionEdit}
            onDescriptionUpdate={onDescriptionUpdate}
            onThreadLinkSelect={onThreadLinkSelect}
          />
        )}
      </Col>
      {!isVersionView && (
        <Col span={24}>
          <Row justify="end">
            <Col>
              <Switch
                checked={showDeletedEndpoints}
                data-testid="show-deleted"
                onClick={onShowDeletedEndpointsChange}
              />
              <Typography.Text className="m-l-xs">
                {t('label.deleted')}
              </Typography.Text>{' '}
            </Col>
          </Row>
        </Col>
      )}

      <Col span={24}>
        <TableAntd
          bordered
          columns={tableColumn}
          data-testid="databaseSchema-tables"
          dataSource={apiEndpoints.data}
          loading={apiEndpointsLoading}
          locale={{
            emptyText: (
              <ErrorPlaceHolder
                className="mt-0-important"
                type={ERROR_PLACEHOLDER_TYPE.NO_DATA}
              />
            ),
          }}
          pagination={false}
          rowKey="id"
          size="small"
        />
      </Col>
      {apiEndpoints.paging.total > PAGE_SIZE && apiEndpoints.data.length > 0 && (
        <Col span={24}>
          <NextPrevious
            currentPage={currentEndpointsPage}
            pageSize={PAGE_SIZE}
            paging={apiEndpoints.paging}
            pagingHandler={endpointPaginationHandler}
          />
        </Col>
      )}
    </Row>
  );
}

export default APIEndpointsTab;
