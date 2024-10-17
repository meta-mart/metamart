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

import React, { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import {
  PipelineStatus,
  StatusType,
} from '../../../../generated/entity/data/pipeline';
import {
  getTableViewData,
  StatusIndicator,
} from '../../../../utils/executionUtils';
import FilterTablePlaceHolder from '../../../common/ErrorWithPlaceholder/FilterTablePlaceHolder';
import Table from '../../../common/Table/Table';

interface ListViewProps {
  executions: Array<PipelineStatus> | undefined;
  status: string;
  loading: boolean;
  searchString: string | undefined;
}

const ListView = ({
  executions,
  status,
  loading,
  searchString,
}: ListViewProps) => {
  const { t } = useTranslation();

  const tableData = useMemo(
    () => getTableViewData(executions, status, searchString),
    [executions, status, searchString]
  );

  const columns = useMemo(
    () => [
      {
        title: t('label.name'),
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: t('label.status'),
        dataIndex: 'status',
        key: 'status',
        render: (status: StatusType) => <StatusIndicator status={status} />,
      },
      {
        title: t('label.date-and-time'),
        dataIndex: 'timestamp',
        key: 'timestamp',
      },
    ],
    []
  );

  return (
    <Table
      bordered
      className="h-full"
      columns={columns}
      data-testid="list-view-table"
      dataSource={tableData}
      loading={loading}
      locale={{
        emptyText: <FilterTablePlaceHolder />,
      }}
      pagination={false}
      rowKey={(record) => `${record.name}-${record.status}-${record.key}`}
    />
  );
};

export default ListView;
