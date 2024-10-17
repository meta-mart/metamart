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
import { Button, Col, Row, Table, Typography } from 'antd';
import { AxiosError } from 'axios';
import { isEmpty, noop } from 'lodash';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useHistory } from 'react-router-dom';
import ErrorPlaceHolder from '../../../components/common/ErrorWithPlaceholder/ErrorPlaceHolder';
import Loader from '../../../components/common/Loader/Loader';
import NextPrevious from '../../../components/common/NextPrevious/NextPrevious';
import { PagingHandlerParams } from '../../../components/common/NextPrevious/NextPrevious.interface';
import { OwnerLabel } from '../../../components/common/OwnerLabel/OwnerLabel.component';
import TableTags from '../../../components/Database/TableTags/TableTags.component';
import PageHeader from '../../../components/PageHeader/PageHeader.component';
import PageLayoutV1 from '../../../components/PageLayoutV1/PageLayoutV1';
import { getEntityDetailsPath, ROUTES } from '../../../constants/constants';
import { usePermissionProvider } from '../../../context/PermissionProvider/PermissionProvider';
import {
  OperationPermission,
  ResourceEntity,
} from '../../../context/PermissionProvider/PermissionProvider.interface';
import { ERROR_PLACEHOLDER_TYPE } from '../../../enums/common.enum';
import { EntityType, TabSpecificField } from '../../../enums/entity.enum';
import { Metric } from '../../../generated/entity/data/metric';
import { EntityReference } from '../../../generated/type/entityReference';
import { Include } from '../../../generated/type/include';
import { Paging } from '../../../generated/type/paging';
import { TagLabel, TagSource } from '../../../generated/type/tagLabel';
import LimitWrapper from '../../../hoc/LimitWrapper';
import { usePaging } from '../../../hooks/paging/usePaging';
import { getMetrics } from '../../../rest/metricsAPI';
import { getEntityName } from '../../../utils/EntityUtils';
import { DEFAULT_ENTITY_PERMISSION } from '../../../utils/PermissionsUtils';
import { getErrorText } from '../../../utils/StringsUtils';
import { showErrorToast } from '../../../utils/ToastUtils';

const MetricListPage = () => {
  const { t } = useTranslation();
  const history = useHistory();

  const {
    pageSize,
    currentPage,
    handlePageChange,
    handlePageSizeChange,
    handlePagingChange,
    showPagination,
    paging,
  } = usePaging();

  const { getResourcePermission } = usePermissionProvider();
  const [permission, setPermission] = useState<OperationPermission>(
    DEFAULT_ENTITY_PERMISSION
  );
  const [error, setError] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [metrics, setMetrics] = useState<Metric[]>([]);

  const init = async () => {
    try {
      setLoading(true);
      const permission = await getResourcePermission(ResourceEntity.METRIC);
      setPermission(permission);
      if (permission.ViewAll || permission.ViewBasic) {
        const metricResponse = await getMetrics({
          fields: [TabSpecificField.OWNERS, TabSpecificField.TAGS],
          limit: pageSize,
          include: Include.All,
        });
        setMetrics(metricResponse.data);
        handlePagingChange(metricResponse.paging);
      }
    } catch (error) {
      const errorMessage = getErrorText(
        error as AxiosError,
        t('server.entity-fetch-error', {
          entity: t('label.metric-plural'),
        })
      );
      showErrorToast(errorMessage);
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const fetchMetrics = async (params?: Partial<Paging>) => {
    try {
      setLoadingMore(true);
      const metricResponse = await getMetrics({
        ...params,
        fields: [TabSpecificField.OWNERS, TabSpecificField.TAGS],
        limit: pageSize,
        include: Include.All,
      });
      setMetrics(metricResponse.data);
      handlePagingChange(metricResponse.paging);
    } catch (error) {
      const errorMessage = getErrorText(
        error as AxiosError,
        t('server.entity-fetch-error', {
          entity: t('label.metric-plural'),
        })
      );
      showErrorToast(errorMessage);
      setError(errorMessage);
    } finally {
      setLoadingMore(false);
    }
  };

  const onPageChange = useCallback(
    ({ cursorType, currentPage }: PagingHandlerParams) => {
      if (cursorType) {
        fetchMetrics({ [cursorType]: paging[cursorType] });
        handlePageChange(currentPage);
      }
    },
    [paging, pageSize]
  );

  const noopWithPromise = async () => {
    noop();
  };

  const columns = useMemo(
    () => [
      {
        title: t('label.name'),
        dataIndex: 'name',
        width: '200px',
        key: 'name',
        render: (_: string, record: Metric) => {
          return (
            <Link
              data-testid="metric-name"
              to={getEntityDetailsPath(
                EntityType.METRIC,
                record.fullyQualifiedName ?? ''
              )}>
              {getEntityName(record)}
            </Link>
          );
        },
      },
      {
        title: t('label.description'),
        dataIndex: 'description',
        flex: true,
        key: 'description',
        render: (description: string) =>
          isEmpty(description) ? (
            <Typography.Text className="text-grey-muted">
              {t('label.no-entity', {
                entity: t('label.description'),
              })}
            </Typography.Text>
          ) : (
            description
          ),
      },
      {
        title: t('label.tag-plural'),
        dataIndex: 'tags',
        key: 'tags',
        accessor: 'tags',
        width: 300,
        render: (tags: TagLabel[], record: Metric, index: number) => (
          <TableTags<Metric>
            isReadOnly
            entityFqn={record.fullyQualifiedName ?? ''}
            entityType={EntityType.METRIC}
            handleTagSelection={noopWithPromise}
            hasTagEditAccess={false}
            index={index}
            record={record}
            tags={tags}
            type={TagSource.Classification}
            onThreadLinkSelect={noop}
          />
        ),
      },
      {
        title: t('label.glossary-term-plural'),
        dataIndex: 'tags',
        key: 'glossary',
        accessor: 'tags',
        width: 300,
        render: (tags: TagLabel[], record: Metric, index: number) => (
          <TableTags<Metric>
            isReadOnly
            entityFqn={record.fullyQualifiedName ?? ''}
            entityType={EntityType.METRIC}
            handleTagSelection={noopWithPromise}
            hasTagEditAccess={false}
            index={index}
            record={record}
            tags={tags}
            type={TagSource.Glossary}
            onThreadLinkSelect={noop}
          />
        ),
      },
      {
        title: t('label.owner-plural'),
        dataIndex: 'owners',
        key: 'owners',
        width: 200,
        render: (owners: EntityReference[]) => <OwnerLabel owners={owners} />,
      },
    ],
    []
  );

  useEffect(() => {
    init();
  }, [pageSize]);

  if (loading) {
    return <Loader />;
  }

  if (error && !loading) {
    return (
      <ErrorPlaceHolder>
        <Typography.Paragraph className="text-center m-auto">
          {error}
        </Typography.Paragraph>
      </ErrorPlaceHolder>
    );
  }

  return (
    <PageLayoutV1 pageTitle={t('label.metric-plural')}>
      <Row className="p-x-lg p-t-md p-b-md" gutter={[0, 16]}>
        <Col span={24}>
          <div className="d-flex justify-between">
            <PageHeader
              data={{
                header: t('label.metric-plural'),
                subHeader: t('message.metric-description'),
              }}
            />
            {permission.Create && (
              <LimitWrapper resource="metric">
                <Button
                  data-testid="create-metric"
                  type="primary"
                  onClick={() => history.push(ROUTES.ADD_METRIC)}>
                  {t('label.add-entity', { entity: t('label.metric') })}
                </Button>
              </LimitWrapper>
            )}
          </div>
        </Col>
        <Col span={24}>
          <Table
            bordered
            columns={columns}
            dataSource={metrics}
            loading={loadingMore}
            locale={{
              emptyText: (
                <ErrorPlaceHolder
                  className="p-y-md"
                  heading={t('label.metric')}
                  permission={permission.Create}
                  type={ERROR_PLACEHOLDER_TYPE.CREATE}
                  onClick={() => history.push(ROUTES.ADD_METRIC)}
                />
              ),
            }}
            pagination={false}
            rowKey="id"
            size="small"
          />
        </Col>
        <Col span={24}>
          {showPagination && (
            <NextPrevious
              currentPage={currentPage}
              pageSize={pageSize}
              paging={paging}
              pagingHandler={onPageChange}
              onShowSizeChange={handlePageSizeChange}
            />
          )}
        </Col>
      </Row>
    </PageLayoutV1>
  );
};

export default MetricListPage;
