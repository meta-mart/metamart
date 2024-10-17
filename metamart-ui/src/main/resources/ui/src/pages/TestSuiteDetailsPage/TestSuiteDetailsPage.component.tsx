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

import { Button, Col, Modal, Row, Space } from 'antd';
import { AxiosError } from 'axios';
import { compare } from 'fast-json-patch';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useHistory } from 'react-router-dom';
import DescriptionV1 from '../../components/common/EntityDescription/DescriptionV1';
import ManageButton from '../../components/common/EntityPageInfos/ManageButton/ManageButton';
import ErrorPlaceHolder from '../../components/common/ErrorWithPlaceholder/ErrorPlaceHolder';
import Loader from '../../components/common/Loader/Loader';
import {
  NextPreviousProps,
  PagingHandlerParams,
} from '../../components/common/NextPrevious/NextPrevious.interface';
import { OwnerLabel } from '../../components/common/OwnerLabel/OwnerLabel.component';
import TitleBreadcrumb from '../../components/common/TitleBreadcrumb/TitleBreadcrumb.component';
import { TitleBreadcrumbProps } from '../../components/common/TitleBreadcrumb/TitleBreadcrumb.interface';
import DataQualityTab from '../../components/Database/Profiler/DataQualityTab/DataQualityTab';
import { AddTestCaseList } from '../../components/DataQuality/AddTestCaseList/AddTestCaseList.component';
import PageLayoutV1 from '../../components/PageLayoutV1/PageLayoutV1';
import { INITIAL_PAGING_VALUE } from '../../constants/constants';
import { DEFAULT_SORT_ORDER } from '../../constants/profiler.constant';
import { usePermissionProvider } from '../../context/PermissionProvider/PermissionProvider';
import {
  OperationPermission,
  ResourceEntity,
} from '../../context/PermissionProvider/PermissionProvider.interface';
import { ACTION_TYPE, ERROR_PLACEHOLDER_TYPE } from '../../enums/common.enum';
import { EntityType, TabSpecificField } from '../../enums/entity.enum';
import { TestCase } from '../../generated/tests/testCase';
import { TestSuite } from '../../generated/tests/testSuite';
import { Include } from '../../generated/type/include';
import { useAuth } from '../../hooks/authHooks';
import { usePaging } from '../../hooks/paging/usePaging';
import { useFqn } from '../../hooks/useFqn';
import { DataQualityPageTabs } from '../../pages/DataQuality/DataQualityPage.interface';
import {
  addTestCaseToLogicalTestSuite,
  getListTestCaseBySearch,
  getTestSuiteByName,
  ListTestCaseParamsBySearch,
  updateTestSuiteById,
} from '../../rest/testAPI';
import { getEntityName } from '../../utils/EntityUtils';
import { DEFAULT_ENTITY_PERMISSION } from '../../utils/PermissionsUtils';
import {
  getDataQualityPagePath,
  getTestSuitePath,
} from '../../utils/RouterUtils';
import { showErrorToast } from '../../utils/ToastUtils';
import './test-suite-details-page.styles.less';

const TestSuiteDetailsPage = () => {
  const { t } = useTranslation();
  const { getEntityPermissionByFqn } = usePermissionProvider();
  const { fqn: testSuiteFQN } = useFqn();
  const { isAdminUser } = useAuth();
  const history = useHistory();

  const afterDeleteAction = () => {
    history.push(getDataQualityPagePath(DataQualityPageTabs.TEST_SUITES));
  };
  const [testSuite, setTestSuite] = useState<TestSuite>();
  const [isDescriptionEditable, setIsDescriptionEditable] = useState(false);
  const [isTestCaseLoading, setIsTestCaseLoading] = useState(true);
  const [testCaseResult, setTestCaseResult] = useState<Array<TestCase>>([]);

  const {
    currentPage,
    handlePageChange,
    pageSize,
    handlePageSizeChange,
    paging,
    handlePagingChange,
    showPagination,
  } = usePaging();
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [testSuitePermissions, setTestSuitePermissions] =
    useState<OperationPermission>(DEFAULT_ENTITY_PERMISSION);
  const [isTestCaseModalOpen, setIsTestCaseModalOpen] =
    useState<boolean>(false);
  const [sortOptions, setSortOptions] =
    useState<ListTestCaseParamsBySearch>(DEFAULT_SORT_ORDER);

  const [slashedBreadCrumb, setSlashedBreadCrumb] = useState<
    TitleBreadcrumbProps['titleLinks']
  >([]);

  const { testSuiteDescription, testSuiteId, testOwners } = useMemo(() => {
    return {
      testOwners: testSuite?.owners,
      testSuiteId: testSuite?.id ?? '',
      testSuiteDescription: testSuite?.description ?? '',
    };
  }, [testSuite]);

  const incidentUrlState = useMemo(() => {
    return [
      {
        name: t('label.test-suite-plural'),
        url: getDataQualityPagePath(DataQualityPageTabs.TEST_SUITES),
      },
      {
        name: getEntityName(testSuite),
        url: getTestSuitePath(testSuite?.fullyQualifiedName ?? ''),
      },
    ];
  }, [testSuite]);

  const saveAndUpdateTestSuiteData = (updatedData: TestSuite) => {
    const jsonPatch = compare(testSuite as TestSuite, updatedData);

    return updateTestSuiteById(testSuiteId as string, jsonPatch);
  };

  const descriptionHandler = (value: boolean) => {
    setIsDescriptionEditable(value);
  };

  const fetchTestSuitePermission = async () => {
    setIsLoading(true);
    try {
      const response = await getEntityPermissionByFqn(
        ResourceEntity.TEST_SUITE,
        testSuiteFQN
      );
      setTestSuitePermissions(response);
    } catch (error) {
      showErrorToast(error as AxiosError);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchTestCases = async (param?: ListTestCaseParamsBySearch) => {
    setIsTestCaseLoading(true);
    try {
      const response = await getListTestCaseBySearch({
        fields: [
          TabSpecificField.TEST_CASE_RESULT,
          TabSpecificField.TEST_DEFINITION,
          TabSpecificField.TESTSUITE,
          TabSpecificField.INCIDENT_ID,
        ],
        testSuiteId,
        ...sortOptions,
        ...param,
        limit: pageSize,
      });

      setTestCaseResult(response.data);
      handlePagingChange(response.paging);
    } catch {
      setTestCaseResult([]);
      showErrorToast(
        t('server.entity-fetch-error', {
          entity: t('label.test-case-plural'),
        })
      );
    } finally {
      setIsTestCaseLoading(false);
    }
  };
  const handleSortTestCase = async (apiParams?: ListTestCaseParamsBySearch) => {
    setSortOptions(apiParams ?? DEFAULT_SORT_ORDER);
    await fetchTestCases({ ...(apiParams ?? DEFAULT_SORT_ORDER), offset: 0 });
    handlePageChange(INITIAL_PAGING_VALUE);
  };

  const handleAddTestCaseSubmit = async (testCases: TestCase[]) => {
    const testCaseIds = testCases.reduce((ids, curr) => {
      return curr.id ? [...ids, curr.id] : ids;
    }, [] as string[]);
    try {
      await addTestCaseToLogicalTestSuite({
        testCaseIds,
        testSuiteId,
      });
      setIsTestCaseModalOpen(false);
      fetchTestCases();
    } catch (error) {
      showErrorToast(error as AxiosError);
    }
  };

  const fetchTestSuiteByName = async () => {
    try {
      const response = await getTestSuiteByName(testSuiteFQN, {
        fields: TabSpecificField.OWNERS,
        include: Include.All,
      });
      setSlashedBreadCrumb([
        {
          name: t('label.test-suite-plural'),
          url: getDataQualityPagePath(DataQualityPageTabs.TEST_SUITES),
        },
        {
          name: getEntityName(response),
          url: '',
        },
      ]);
      setTestSuite(response);
    } catch (error) {
      setTestSuite(undefined);
      showErrorToast(
        error as AxiosError,
        t('server.entity-fetch-error', {
          entity: t('label.test-suite'),
        })
      );
    }
  };

  const updateTestSuiteData = (updatedTestSuite: TestSuite, type: string) => {
    saveAndUpdateTestSuiteData(updatedTestSuite)
      .then((res) => {
        if (res) {
          setTestSuite(res);
        } else {
          showErrorToast(t('server.unexpected-response'));
        }
      })
      .catch((err: AxiosError) => {
        showErrorToast(
          err,
          t(
            `server.entity-${
              type === ACTION_TYPE.UPDATE ? 'updating' : 'removing'
            }-error`,
            {
              entity: t('label.owner'),
            }
          )
        );
      });
  };

  const onUpdateOwner = useCallback(
    (updatedOwners: TestSuite['owners']) => {
      const updatedTestSuite = {
        ...testSuite,
        owners: updatedOwners,
      } as TestSuite;

      updateTestSuiteData(updatedTestSuite, ACTION_TYPE.UPDATE);
    },
    [testOwners, testSuite]
  );

  const onDescriptionUpdate = async (updatedHTML: string) => {
    if (testSuite?.description !== updatedHTML) {
      const updatedTestSuite = { ...testSuite, description: updatedHTML };
      try {
        const response = await saveAndUpdateTestSuiteData(
          updatedTestSuite as TestSuite
        );
        if (response) {
          setTestSuite(response);
        } else {
          throw t('server.unexpected-response');
        }
      } catch (error) {
        showErrorToast(error as AxiosError);
      } finally {
        descriptionHandler(false);
      }
    } else {
      descriptionHandler(false);
    }
  };

  const handleTestCasePaging = ({ currentPage }: PagingHandlerParams) => {
    if (currentPage) {
      handlePageChange(currentPage);
      fetchTestCases({
        offset: (currentPage - 1) * pageSize,
      });
    }
  };

  const handleTestSuiteUpdate = (testCase?: TestCase) => {
    if (testCase) {
      setTestCaseResult((prev) =>
        prev.map((test) =>
          test.id === testCase.id ? { ...test, ...testCase } : test
        )
      );
    }
  };

  useEffect(() => {
    if (testSuitePermissions.ViewAll || testSuitePermissions.ViewBasic) {
      fetchTestSuiteByName();
    }
  }, [testSuitePermissions, testSuiteFQN]);

  useEffect(() => {
    fetchTestSuitePermission();
  }, [testSuiteFQN]);

  useEffect(() => {
    if (testSuiteId) {
      fetchTestCases({ testSuiteId });
    }
  }, [testSuite, pageSize]);

  const pagingData: NextPreviousProps = useMemo(
    () => ({
      isNumberBased: true,
      currentPage,
      pageSize,
      paging,
      onShowSizeChange: handlePageSizeChange,
      pagingHandler: handleTestCasePaging,
    }),
    [currentPage, paging, pageSize, handlePageSizeChange, handleTestCasePaging]
  );

  if (isLoading) {
    return <Loader />;
  }

  if (!testSuitePermissions.ViewAll && !testSuitePermissions.ViewBasic) {
    return <ErrorPlaceHolder type={ERROR_PLACEHOLDER_TYPE.PERMISSION} />;
  }

  return (
    <PageLayoutV1
      pageTitle={t('label.entity-detail-plural', {
        entity: getEntityName(testSuite),
      })}>
      <Row className="page-container" gutter={[0, 32]}>
        <Col span={24}>
          <Space align="center" className="justify-between w-full">
            <TitleBreadcrumb
              data-testid="test-suite-breadcrumb"
              titleLinks={slashedBreadCrumb}
            />
            <Space>
              {(testSuitePermissions.EditAll ||
                testSuitePermissions.EditTests) && (
                <Button
                  data-testid="add-test-case-btn"
                  type="primary"
                  onClick={() => setIsTestCaseModalOpen(true)}>
                  {t('label.add-entity', {
                    entity: t('label.test-case-plural'),
                  })}
                </Button>
              )}
              <ManageButton
                isRecursiveDelete
                afterDeleteAction={afterDeleteAction}
                allowSoftDelete={false}
                canDelete={isAdminUser}
                deleted={testSuite?.deleted}
                displayName={getEntityName(testSuite)}
                entityId={testSuite?.id}
                entityName={testSuite?.fullyQualifiedName as string}
                entityType={EntityType.TEST_SUITE}
              />
            </Space>
          </Space>

          <div className="w-full m-t-xxs m-b-xs">
            <OwnerLabel
              hasPermission={isAdminUser}
              owners={testOwners}
              onUpdate={onUpdateOwner}
            />
          </div>

          <DescriptionV1
            className="test-suite-description"
            description={testSuiteDescription}
            entityName={getEntityName(testSuite)}
            entityType={EntityType.TEST_SUITE}
            hasEditAccess={isAdminUser}
            isEdit={isDescriptionEditable}
            showCommentsIcon={false}
            onCancel={() => descriptionHandler(false)}
            onDescriptionEdit={() => descriptionHandler(true)}
            onDescriptionUpdate={onDescriptionUpdate}
          />
        </Col>

        <Col span={24}>
          <DataQualityTab
            afterDeleteAction={fetchTestCases}
            breadcrumbData={incidentUrlState}
            fetchTestCases={handleSortTestCase}
            isLoading={isLoading || isTestCaseLoading}
            pagingData={pagingData}
            removeFromTestSuite={{ testSuite: testSuite as TestSuite }}
            showPagination={showPagination}
            testCases={testCaseResult}
            onTestCaseResultUpdate={handleTestSuiteUpdate}
            onTestUpdate={handleTestSuiteUpdate}
          />
        </Col>
        <Col span={24}>
          <Modal
            centered
            destroyOnClose
            closable={false}
            footer={null}
            open={isTestCaseModalOpen}
            title={t('label.add-entity', {
              entity: t('label.test-case-plural'),
            })}
            width={750}>
            <AddTestCaseList
              existingTest={testSuite?.tests ?? []}
              onCancel={() => setIsTestCaseModalOpen(false)}
              onSubmit={handleAddTestCaseSubmit}
            />
          </Modal>
        </Col>
      </Row>
    </PageLayoutV1>
  );
};

export default TestSuiteDetailsPage;
