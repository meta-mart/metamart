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
import Icon from '@ant-design/icons/lib/components/Icon';
import { Button, List, Popover, Space, Tooltip, Typography } from 'antd';
import classNames from 'classnames';
import { startCase } from 'lodash';
import React, { FC, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ReactComponent as EditIcon } from '../../../assets/svg/edit-new.svg';
import { ReactComponent as IconRemoveColored } from '../../../assets/svg/ic-remove-colored.svg';
import {
  DE_ACTIVE_COLOR,
  NO_DATA_PLACEHOLDER,
} from '../../../constants/constants';
import { OperationPermission } from '../../../context/PermissionProvider/PermissionProvider.interface';
import {
  Metric,
  MetricGranularity,
  MetricType,
  UnitOfMeasurement,
} from '../../../generated/entity/data/metric';
import { ExtraInfoLabel } from '../../DataAssets/DataAssetsHeader/DataAssetsHeader.component';
import './metric-header-info.less';

interface MetricInfoItemOption {
  label: string;
  value: string;
  key: string;
}

interface MetricHeaderInfoProps {
  metricPermissions: OperationPermission;
  metricDetails: Metric;
  onUpdateMetricDetails: (
    updatedData: Metric,
    key: keyof Metric
  ) => Promise<void>;
}

interface MetricInfoItemProps {
  label: string;
  value: string | undefined;
  hasPermission: boolean;
  options: MetricInfoItemOption[];
  valueKey: keyof Metric;
  metricDetails: Metric;
  onUpdateMetricDetails: MetricHeaderInfoProps['onUpdateMetricDetails'];
}

const MetricInfoItem: FC<MetricInfoItemProps> = ({
  label,
  value,
  hasPermission,
  options,
  onUpdateMetricDetails,
  valueKey,
  metricDetails,
}) => {
  const { t } = useTranslation();
  const [popupVisible, setPopupVisible] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);

  const modiFiedLabel = label.toLowerCase().replace(/\s+/g, '-');

  const sortedOptions = useMemo(
    () =>
      options.sort((a, b) => {
        if (a.value === value) {
          return -1;
        }
        if (b.value === value) {
          return 1;
        }

        return 0;
      }),
    [options, value]
  );

  const handleUpdate = async (value: string | undefined) => {
    try {
      setIsUpdating(true);
      const updatedMetricDetails = {
        ...metricDetails,
        [valueKey]: value,
      };

      await onUpdateMetricDetails(updatedMetricDetails, valueKey);
    } catch (error) {
      //
    } finally {
      setIsUpdating(false);
    }
  };

  const list = (
    <List
      dataSource={sortedOptions}
      itemLayout="vertical"
      renderItem={(item) => (
        <List.Item
          className={classNames('selectable-list-item', 'cursor-pointer', {
            active: value === item.value,
          })}
          extra={
            value === item.value && (
              <Tooltip
                title={t('label.remove-entity', {
                  entity: label,
                })}>
                <Icon
                  className="align-middle"
                  component={IconRemoveColored}
                  data-testid={`remove-${modiFiedLabel}-button`}
                  style={{ fontSize: '16px' }}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleUpdate(undefined);
                    setPopupVisible(false);
                  }}
                />
              </Tooltip>
            )
          }
          key={item.key}
          title={item.label}
          onClick={(e) => {
            e.stopPropagation();
            handleUpdate(item.value);
            setPopupVisible(false);
          }}>
          <Typography.Text>{item.label}</Typography.Text>
        </List.Item>
      )}
      size="small"
      style={{
        maxHeight: '250px',
        overflowY: 'auto',
      }}
    />
  );

  return (
    <Space data-testid={modiFiedLabel}>
      <ExtraInfoLabel
        dataTestId={modiFiedLabel}
        label={label}
        value={value ?? NO_DATA_PLACEHOLDER}
      />
      {hasPermission && !metricDetails.deleted && (
        <Popover
          destroyTooltipOnHide
          content={list}
          open={popupVisible}
          overlayClassName="metric-header-info-popover"
          placement="bottomRight"
          showArrow={false}
          trigger="click"
          onOpenChange={setPopupVisible}>
          <Tooltip
            title={t('label.edit-entity', {
              entity: label,
            })}>
            <Button
              className="flex-center p-0"
              data-testid={`edit-${modiFiedLabel}-button`}
              icon={<EditIcon color={DE_ACTIVE_COLOR} width="14px" />}
              loading={isUpdating}
              size="small"
              type="text"
            />
          </Tooltip>
        </Popover>
      )}
    </Space>
  );
};

const MetricHeaderInfo: FC<MetricHeaderInfoProps> = ({
  metricDetails,
  metricPermissions,
  onUpdateMetricDetails,
}) => {
  const { t } = useTranslation();
  const hasPermission = Boolean(metricPermissions.EditAll);

  return (
    <>
      <MetricInfoItem
        hasPermission={hasPermission}
        label={t('label.metric-type')}
        metricDetails={metricDetails}
        options={Object.values(MetricType).map((metricType) => ({
          key: metricType,
          label: startCase(metricType.toLowerCase()),
          value: metricType,
        }))}
        value={metricDetails.metricType}
        valueKey="metricType"
        onUpdateMetricDetails={onUpdateMetricDetails}
      />
      <MetricInfoItem
        hasPermission={hasPermission}
        label={t('label.unit-of-measurement')}
        metricDetails={metricDetails}
        options={Object.values(UnitOfMeasurement).map((unitOfMeasurement) => ({
          key: unitOfMeasurement,
          label: startCase(unitOfMeasurement.toLowerCase()),
          value: unitOfMeasurement,
        }))}
        value={metricDetails.unitOfMeasurement}
        valueKey="unitOfMeasurement"
        onUpdateMetricDetails={onUpdateMetricDetails}
      />
      <MetricInfoItem
        hasPermission={hasPermission}
        label={t('label.granularity')}
        metricDetails={metricDetails}
        options={Object.values(MetricGranularity).map((granularity) => ({
          key: granularity,
          label: startCase(granularity.toLowerCase()),
          value: granularity,
        }))}
        value={metricDetails.granularity}
        valueKey="granularity"
        onUpdateMetricDetails={onUpdateMetricDetails}
      />
    </>
  );
};

export default MetricHeaderInfo;
