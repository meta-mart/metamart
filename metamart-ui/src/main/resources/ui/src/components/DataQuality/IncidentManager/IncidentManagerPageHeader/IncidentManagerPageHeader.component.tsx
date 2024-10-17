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
import { Divider, Skeleton, Space, Tooltip, Typography } from 'antd';
import { AxiosError } from 'axios';
import { compare } from 'fast-json-patch';
import { first, isUndefined, last } from 'lodash';
import QueryString from 'qs';
import React, { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router-dom';
import { getEntityDetailsPath } from '../../../../constants/constants';
import { usePermissionProvider } from '../../../../context/PermissionProvider/PermissionProvider';
import { ResourceEntity } from '../../../../context/PermissionProvider/PermissionProvider.interface';
import { EntityTabs, EntityType } from '../../../../enums/entity.enum';
import { ThreadType } from '../../../../generated/api/feed/createThread';
import { CreateTestCaseResolutionStatus } from '../../../../generated/api/tests/createTestCaseResolutionStatus';
import {
  Thread,
  ThreadTaskStatus,
} from '../../../../generated/entity/feed/thread';
import { Operation } from '../../../../generated/entity/policies/policy';
import { EntityReference } from '../../../../generated/tests/testCase';
import {
  Severities,
  TestCaseResolutionStatus,
  TestCaseResolutionStatusTypes,
} from '../../../../generated/tests/testCaseResolutionStatus';
import {
  getListTestCaseIncidentByStateId,
  postTestCaseIncidentStatus,
  updateTestCaseIncidentById,
} from '../../../../rest/incidentManagerAPI';
import { getNameFromFQN } from '../../../../utils/CommonUtils';
import { getEntityName } from '../../../../utils/EntityUtils';
import { getEntityFQN } from '../../../../utils/FeedUtils';
import { checkPermission } from '../../../../utils/PermissionsUtils';
import { getDecodedFqn } from '../../../../utils/StringsUtils';
import { getTaskDetailPath } from '../../../../utils/TasksUtils';
import { showErrorToast } from '../../../../utils/ToastUtils';
import { useActivityFeedProvider } from '../../../ActivityFeed/ActivityFeedProvider/ActivityFeedProvider';
import { OwnerLabel } from '../../../common/OwnerLabel/OwnerLabel.component';
import { TableProfilerTab } from '../../../Database/Profiler/ProfilerDashboard/profilerDashboard.interface';
import Severity from '../Severity/Severity.component';
import TestCaseIncidentManagerStatus from '../TestCaseStatus/TestCaseIncidentManagerStatus.component';
import { IncidentManagerPageHeaderProps } from './IncidentManagerPageHeader.interface';

const IncidentManagerPageHeader = ({
  onOwnerUpdate,
  testCaseData,
  fetchTaskCount,
}: IncidentManagerPageHeaderProps) => {
  const { t } = useTranslation();
  const [activeTask, setActiveTask] = useState<Thread>();
  const [testCaseStatusData, setTestCaseStatusData] =
    useState<TestCaseResolutionStatus>();
  const [isLoading, setIsLoading] = useState(true);

  const { fqn } = useParams<{ fqn: string }>();
  const decodedFqn = getDecodedFqn(fqn);
  const {
    setActiveThread,
    entityThread,
    getFeedData,
    testCaseResolutionStatus,
    updateTestCaseIncidentStatus,
    initialAssignees,
  } = useActivityFeedProvider();

  const tableFqn = useMemo(
    () => getEntityFQN(testCaseData?.entityLink ?? ''),
    [testCaseData]
  );

  const handleSeverityUpdate = async (severity: Severities) => {
    if (isUndefined(testCaseStatusData)) {
      return;
    }

    const updatedData = { ...testCaseStatusData, severity };
    const patch = compare(testCaseStatusData, updatedData);
    try {
      await updateTestCaseIncidentById(testCaseStatusData.id ?? '', patch);
      setTestCaseStatusData(updatedData);
      updateTestCaseIncidentStatus([
        ...testCaseResolutionStatus.slice(0, -1),
        updatedData,
      ]);
    } catch (error) {
      showErrorToast(error as AxiosError);
    }
  };

  const updateAssignee = async (data: CreateTestCaseResolutionStatus) => {
    try {
      await postTestCaseIncidentStatus(data);
    } catch (error) {
      showErrorToast(error as AxiosError);
    }
  };

  const onIncidentStatusUpdate = (data: TestCaseResolutionStatus) => {
    setTestCaseStatusData(data);
    updateTestCaseIncidentStatus([...testCaseResolutionStatus, data]);
  };

  const handleAssigneeUpdate = (assignee?: EntityReference[]) => {
    if (isUndefined(testCaseStatusData)) {
      return;
    }

    const assigneeData = assignee?.[0];

    const updatedData: TestCaseResolutionStatus = {
      ...testCaseStatusData,
      testCaseResolutionStatusDetails: {
        ...testCaseStatusData?.testCaseResolutionStatusDetails,
        assignee: assigneeData,
      },
      testCaseResolutionStatusType: TestCaseResolutionStatusTypes.Assigned,
    };

    const createTestCaseResolutionStatus: CreateTestCaseResolutionStatus = {
      severity: testCaseStatusData.severity,
      testCaseReference:
        testCaseStatusData.testCaseReference?.fullyQualifiedName ?? '',
      testCaseResolutionStatusType: TestCaseResolutionStatusTypes.Assigned,
      testCaseResolutionStatusDetails: {
        assignee: assigneeData,
      },
    };

    updateAssignee(createTestCaseResolutionStatus);
    onIncidentStatusUpdate(updatedData);
  };

  const fetchTestCaseResolution = async (id: string) => {
    try {
      const { data } = await getListTestCaseIncidentByStateId(id);

      setTestCaseStatusData(first(data));
    } catch (error) {
      setTestCaseStatusData(undefined);
    }
  };

  useEffect(() => {
    if (decodedFqn) {
      setIsLoading(true);
      getFeedData(
        undefined,
        undefined,
        ThreadType.Task,
        EntityType.TEST_CASE,
        decodedFqn
      ).finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, [decodedFqn]);

  useEffect(() => {
    const openTask = entityThread.find(
      (thread) => thread.task?.status === ThreadTaskStatus.Open
    );
    setActiveTask(openTask);
    setActiveThread(openTask);
  }, [entityThread]);

  useEffect(() => {
    const status = last(testCaseResolutionStatus);

    if (status?.stateId === activeTask?.task?.testCaseResolutionStatusId) {
      if (
        status?.testCaseResolutionStatusType ===
        TestCaseResolutionStatusTypes.Resolved
      ) {
        setTestCaseStatusData(undefined);
        fetchTaskCount();
      } else {
        setTestCaseStatusData(status);
      }
    }
  }, [testCaseResolutionStatus]);

  useEffect(() => {
    if (testCaseData?.incidentId) {
      fetchTestCaseResolution(testCaseData.incidentId);
    }
  }, [testCaseData]);

  const { permissions } = usePermissionProvider();
  const hasEditPermission = useMemo(() => {
    return checkPermission(
      Operation.EditAll,
      ResourceEntity.TEST_CASE,
      permissions
    );
  }, [permissions]);

  const statusDetails = useMemo(() => {
    if (isLoading) {
      return <Skeleton.Input size="small" />;
    }

    if (isUndefined(testCaseStatusData)) {
      return (
        <>
          <Divider className="self-center m-x-sm" type="vertical" />
          <Typography.Text className="d-flex items-center gap-2 text-xs whitespace-nowrap">
            <span className="text-grey-muted">{`${t(
              'label.incident-status'
            )}: `}</span>

            <span>{t('label.no-entity', { entity: t('label.incident') })}</span>
          </Typography.Text>
        </>
      );
    }

    const details = testCaseStatusData?.testCaseResolutionStatusDetails;

    return (
      <>
        {activeTask && (
          <>
            <Divider className="self-center m-x-sm" type="vertical" />
            <Typography.Text className="d-flex items-center gap-2 text-xs whitespace-nowrap">
              <span className="text-grey-muted">{`${t(
                'label.incident'
              )}: `}</span>

              <Link
                className="font-medium"
                data-testid="table-name"
                to={getTaskDetailPath(activeTask)}>
                {`#${activeTask?.task?.id}` ?? '--'}
              </Link>
            </Typography.Text>
          </>
        )}
        <Divider className="self-center m-x-sm" type="vertical" />
        <Typography.Text className="d-flex items-center gap-2 text-xs whitespace-nowrap">
          <span className="text-grey-muted">{`${t(
            'label.incident-status'
          )}: `}</span>

          <TestCaseIncidentManagerStatus
            data={testCaseStatusData}
            usersList={initialAssignees}
            onSubmit={onIncidentStatusUpdate}
          />
        </Typography.Text>
        <Divider className="self-center m-x-sm" type="vertical" />
        <Typography.Text
          className="d-flex items-center gap-2 text-xs whitespace-nowrap"
          data-testid="assignee">
          <span className="text-grey-muted">{`${t('label.assignee')}: `}</span>

          <OwnerLabel
            hasPermission={hasEditPermission}
            multiple={{
              user: false,
              team: false,
            }}
            owners={details?.assignee ? [details.assignee] : []}
            placeHolder={t('label.no-entity', {
              entity: t('label.assignee'),
            })}
            tooltipText={t('label.edit-entity', {
              entity: t('label.assignee'),
            })}
            onUpdate={handleAssigneeUpdate}
          />
        </Typography.Text>
        <Divider className="self-center m-x-sm" type="vertical" />
        <Typography.Text className="d-flex items-center gap-2 text-xs whitespace-nowrap">
          <span className="text-grey-muted">{`${t('label.severity')}: `}</span>

          <Severity
            severity={testCaseStatusData.severity}
            onSubmit={handleSeverityUpdate}
          />
        </Typography.Text>
      </>
    );
  }, [testCaseStatusData, isLoading, activeTask, initialAssignees]);

  return (
    <Space wrap align="center">
      <OwnerLabel
        hasPermission={hasEditPermission}
        owners={testCaseData?.owners}
        onUpdate={onOwnerUpdate}
      />
      {statusDetails}
      {tableFqn && (
        <>
          <Divider className="self-center m-x-sm" type="vertical" />
          <Typography.Text className="self-center text-xs whitespace-nowrap">
            <span className="text-grey-muted">{`${t('label.table')}: `}</span>

            <Link
              className="font-medium"
              data-testid="table-name"
              to={{
                pathname: getEntityDetailsPath(
                  EntityType.TABLE,
                  tableFqn,
                  EntityTabs.PROFILER
                ),
                search: QueryString.stringify({
                  activeTab: TableProfilerTab.DATA_QUALITY,
                }),
              }}>
              {getNameFromFQN(tableFqn)}
            </Link>
          </Typography.Text>
        </>
      )}
      <Divider className="self-center m-x-sm" type="vertical" />
      <Typography.Text className="self-center text-xs whitespace-nowrap">
        <span className="text-grey-muted">{`${t('label.test-type')}: `}</span>
        <Tooltip
          placement="bottom"
          title={testCaseData?.testDefinition.description}>
          <span className="font-medium" data-testid="test-definition-name">
            {getEntityName(testCaseData?.testDefinition)}
          </span>
        </Tooltip>
      </Typography.Text>
    </Space>
  );
};

export default IncidentManagerPageHeader;
