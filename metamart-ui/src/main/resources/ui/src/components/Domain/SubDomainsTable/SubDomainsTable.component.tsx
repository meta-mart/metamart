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
import { Table } from 'antd';
import { ColumnsType } from 'antd/lib/table';
import { isEmpty } from 'lodash';
import React, { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { ERROR_PLACEHOLDER_TYPE } from '../../../enums/common.enum';
import {
  Domain,
  EntityReference,
} from '../../../generated/entity/domains/domain';
import { getEntityName } from '../../../utils/EntityUtils';
import { getDomainDetailsPath } from '../../../utils/RouterUtils';
import ErrorPlaceHolder from '../../common/ErrorWithPlaceholder/ErrorPlaceHolder';
import Loader from '../../common/Loader/Loader';
import { OwnerLabel } from '../../common/OwnerLabel/OwnerLabel.component';
import RichTextEditorPreviewer from '../../common/RichTextEditor/RichTextEditorPreviewer';
import { SubDomainsTableProps } from './SubDomainsTable.interface';

const SubDomainsTable = ({
  subDomains = [],
  isLoading = false,
  permissions,
  onAddSubDomain,
}: SubDomainsTableProps) => {
  const { t } = useTranslation();

  const columns: ColumnsType<Domain> = useMemo(() => {
    const data = [
      {
        title: t('label.sub-domain-plural'),
        dataIndex: 'name',
        key: 'name',
        render: (name: string, record: Domain) => {
          return (
            <Link
              className="cursor-pointer vertical-baseline"
              data-testid={name}
              style={{ color: record.style?.color }}
              to={getDomainDetailsPath(
                record.fullyQualifiedName ?? record.name
              )}>
              {getEntityName(record)}
            </Link>
          );
        },
      },
      {
        title: t('label.description'),
        dataIndex: 'description',
        key: 'description',
        render: (description: string) =>
          description.trim() ? (
            <RichTextEditorPreviewer
              enableSeeMoreVariant
              markdown={description}
              maxLength={120}
            />
          ) : (
            <span className="text-grey-muted">{t('label.no-description')}</span>
          ),
      },
      {
        title: t('label.owner-plural'),
        dataIndex: 'owners',
        key: 'owners',
        render: (owners: EntityReference[]) => <OwnerLabel owners={owners} />,
      },
    ];

    return data;
  }, [subDomains]);

  if (isLoading) {
    return <Loader />;
  }

  if (isEmpty(subDomains) && !isLoading) {
    return (
      <ErrorPlaceHolder
        className="m-t-xlg"
        heading={t('label.sub-domain')}
        permission={permissions.Create}
        type={ERROR_PLACEHOLDER_TYPE.CREATE}
        onClick={onAddSubDomain}
      />
    );
  }

  return (
    <Table
      bordered
      className="p-md"
      columns={columns}
      dataSource={subDomains}
      pagination={false}
      rowKey="fullyQualifiedName"
      size="small"
    />
  );
};

export default SubDomainsTable;
