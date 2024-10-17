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

import { PlusOutlined } from '@ant-design/icons';
import { Button, Col, Row } from 'antd';
import { AxiosError } from 'axios';
import { sortBy } from 'lodash';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useHistory } from 'react-router-dom';
import { usePermissionProvider } from '../../../../context/PermissionProvider/PermissionProvider';
import { ResourceEntity } from '../../../../context/PermissionProvider/PermissionProvider.interface';
import { ERROR_PLACEHOLDER_TYPE } from '../../../../enums/common.enum';
import { TabSpecificField } from '../../../../enums/entity.enum';
import { ServiceCategory } from '../../../../enums/service.enum';
import { PipelineType } from '../../../../generated/api/services/ingestionPipelines/createIngestionPipeline';
import { Table as TableType } from '../../../../generated/entity/data/table';
import { Operation } from '../../../../generated/entity/policies/policy';
import { IngestionPipeline } from '../../../../generated/entity/services/ingestionPipelines/ingestionPipeline';
import { useAirflowStatus } from '../../../../hooks/useAirflowStatus';
import {
  deployIngestionPipelineById,
  enableDisableIngestionPipelineById,
  getIngestionPipelines,
  triggerIngestionPipelineById,
} from '../../../../rest/ingestionPipelineAPI';
import { checkPermission } from '../../../../utils/PermissionsUtils';
import { getTestSuiteIngestionPath } from '../../../../utils/RouterUtils';
import { getServiceFromTestSuiteFQN } from '../../../../utils/TestSuiteUtils';
import { showErrorToast, showSuccessToast } from '../../../../utils/ToastUtils';
import ErrorPlaceHolder from '../../../common/ErrorWithPlaceholder/ErrorPlaceHolder';
import ErrorPlaceHolderIngestion from '../../../common/ErrorWithPlaceholder/ErrorPlaceHolderIngestion';
import IngestionListTable from '../../../Settings/Services/Ingestion/IngestionListTable/IngestionListTable';

interface Props {
  testSuite: TableType['testSuite'];
}

const TestSuitePipelineTab = ({ testSuite }: Props) => {
  const airflowInformation = useAirflowStatus();
  const { t } = useTranslation();
  const testSuiteFQN = testSuite?.fullyQualifiedName ?? testSuite?.name ?? '';

  const { permissions } = usePermissionProvider();
  const history = useHistory();

  const [isLoading, setIsLoading] = useState(true);
  const [testSuitePipelines, setTestSuitePipelines] = useState<
    IngestionPipeline[]
  >([]);
  const [pipelineIdToFetchStatus, setPipelineIdToFetchStatus] =
    useState<string>();

  const handlePipelineIdToFetchStatus = useCallback((pipelineId?: string) => {
    setPipelineIdToFetchStatus(pipelineId);
  }, []);

  const { isAirflowAvailable, isFetchingStatus } = airflowInformation;

  const handlePipelineListUpdate = useCallback(
    (pipelineList: React.SetStateAction<IngestionPipeline[]>) => {
      setTestSuitePipelines(pipelineList);
    },
    []
  );
  const createPermission = useMemo(
    () =>
      checkPermission(
        Operation.Create,
        ResourceEntity.INGESTION_PIPELINE,
        permissions
      ),
    [permissions]
  );

  const getAllIngestionWorkflows = useCallback(async () => {
    try {
      setIsLoading(true);
      const response = await getIngestionPipelines({
        arrQueryFields: [
          TabSpecificField.OWNERS,
          TabSpecificField.PIPELINE_STATUSES,
        ],
        testSuite: testSuiteFQN,
        pipelineType: [PipelineType.TestSuite],
      });
      setTestSuitePipelines(response.data);
    } catch (error) {
      showErrorToast(error as AxiosError);
    } finally {
      setIsLoading(false);
    }
  }, [testSuiteFQN]);

  const handleEnableDisableIngestion = useCallback(
    async (id: string) => {
      try {
        const { data } = await enableDisableIngestionPipelineById(id);
        if (data.id) {
          setTestSuitePipelines((list) =>
            list.map((row) =>
              row.id === id ? { ...row, enabled: data.enabled } : row
            )
          );
        }
      } catch (error) {
        showErrorToast(error as AxiosError, t('server.unexpected-response'));
      }
    },
    [getAllIngestionWorkflows]
  );

  const handleTriggerIngestion = useCallback(
    async (id: string, displayName: string) => {
      try {
        await triggerIngestionPipelineById(id);
        showSuccessToast('Pipeline triggered successfully');

        setPipelineIdToFetchStatus(id);
      } catch (error) {
        showErrorToast(
          t('server.ingestion-workflow-operation-error', {
            operation: 'triggering',
            displayName,
          })
        );
      }
    },
    []
  );

  const handleDeployIngestion = useCallback(
    async (id: string, displayName: string) => {
      try {
        await deployIngestionPipelineById(id);
        showSuccessToast(
          t('message.pipeline-action-success-message', {
            action: t('label.deployed-lowercase'),
          })
        );

        setPipelineIdToFetchStatus(id);
      } catch (error) {
        showErrorToast(
          error as AxiosError,
          t('server.ingestion-workflow-operation-error', {
            operation: 'updating',
            displayName,
          })
        );
      }
    },
    []
  );

  const dataSource = useMemo(() => {
    const sortedByTestCaseLength = sortBy(testSuitePipelines, (pipeline) => {
      const length = pipeline?.sourceConfig?.config?.testCases?.length;
      if (!length) {
        return -Infinity; // Use -Infinity to ensure these come first
      }

      return length;
    });

    return sortedByTestCaseLength.map((test) => ({
      ...test,
      key: test.name,
    }));
  }, [testSuitePipelines]);

  useEffect(() => {
    getAllIngestionWorkflows();
  }, []);

  const emptyPlaceholder = useMemo(
    () =>
      testSuite ? (
        <ErrorPlaceHolder
          button={
            <Button
              ghost
              className="p-x-lg"
              data-testid="add-placeholder-button"
              icon={<PlusOutlined />}
              type="primary"
              onClick={() => {
                history.push(getTestSuiteIngestionPath(testSuiteFQN));
              }}>
              {t('label.add')}
            </Button>
          }
          heading={t('label.pipeline')}
          permission={createPermission}
          type={ERROR_PLACEHOLDER_TYPE.ASSIGN}>
          {t('message.no-table-pipeline')}
        </ErrorPlaceHolder>
      ) : (
        <ErrorPlaceHolder
          placeholderText={t('message.no-test-suite-table-pipeline')}
          type={ERROR_PLACEHOLDER_TYPE.NO_DATA}
        />
      ),
    [testSuite, testSuiteFQN, createPermission]
  );

  if (!isAirflowAvailable && !(isLoading || isFetchingStatus)) {
    return <ErrorPlaceHolderIngestion />;
  }

  return (
    <Row className="m-t-md" gutter={[16, 16]}>
      {dataSource.length > 0 && (
        <Col className="d-flex justify-end" span={24}>
          <Button
            data-testid="add-pipeline-button"
            type="primary"
            onClick={() => {
              history.push(getTestSuiteIngestionPath(testSuiteFQN));
            }}>
            {t('label.add-entity', { entity: t('label.pipeline') })}
          </Button>
        </Col>
      )}
      <Col span={24}>
        <IngestionListTable
          airflowInformation={airflowInformation}
          deployIngestion={handleDeployIngestion}
          emptyPlaceholder={emptyPlaceholder}
          handleEnableDisableIngestion={handleEnableDisableIngestion}
          handleIngestionListUpdate={handlePipelineListUpdate}
          handlePipelineIdToFetchStatus={handlePipelineIdToFetchStatus}
          ingestionData={testSuitePipelines}
          isLoading={isLoading}
          pipelineIdToFetchStatus={pipelineIdToFetchStatus}
          serviceCategory={ServiceCategory.DATABASE_SERVICES}
          serviceName={getServiceFromTestSuiteFQN(testSuiteFQN)}
          tableClassName="test-suite-pipeline-tab"
          triggerIngestion={handleTriggerIngestion}
          onIngestionWorkflowsUpdate={getAllIngestionWorkflows}
        />
      </Col>
    </Row>
  );
};

export default TestSuitePipelineTab;
