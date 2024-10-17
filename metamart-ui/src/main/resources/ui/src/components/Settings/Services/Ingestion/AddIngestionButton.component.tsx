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

import { Button, Dropdown } from 'antd';
import React, { useCallback, useMemo } from 'react';
import { useHistory } from 'react-router-dom';
import { ReactComponent as DropdownIcon } from '../../../../assets/svg/drop-down.svg';
import { MetadataServiceType } from '../../../../generated/api/services/createMetadataService';
import { PipelineType } from '../../../../generated/entity/services/ingestionPipelines/ingestionPipeline';
import LimitWrapper from '../../../../hoc/LimitWrapper';
import {
  getIngestionButtonText,
  getIngestionTypes,
  getMenuItems,
  getSupportedPipelineTypes,
} from '../../../../utils/IngestionUtils';
import { getAddIngestionPath } from '../../../../utils/RouterUtils';
import { AddIngestionButtonProps } from './AddIngestionButton.interface';

function AddIngestionButton({
  serviceDetails,
  pipelineType,
  serviceCategory,
  serviceName,
  ingestionList,
}: Readonly<AddIngestionButtonProps>) {
  const history = useHistory();

  const isMetaMartService = useMemo(
    () =>
      serviceDetails?.connection?.config?.type ===
      MetadataServiceType.MetaMart,
    [serviceDetails]
  );

  const supportedPipelineTypes = useMemo(
    (): PipelineType[] => getSupportedPipelineTypes(serviceDetails),
    [serviceDetails]
  );

  const handleAddIngestionClick = useCallback(
    (type: PipelineType) => {
      history.push(getAddIngestionPath(serviceCategory, serviceName, type));
    },
    [serviceCategory, serviceName]
  );

  // Check if service has at least one metadata pipeline available or not
  const hasMetadata = useMemo(
    () =>
      ingestionList.find(
        (ingestion) => ingestion.pipelineType === PipelineType.Metadata
      ),
    [ingestionList]
  );

  const handleAddIngestionButtonClick = useCallback(
    () =>
      hasMetadata
        ? undefined
        : handleAddIngestionClick(pipelineType ?? PipelineType.Metadata),
    [hasMetadata, pipelineType, handleAddIngestionClick]
  );

  const isDataInSightIngestionExists = useMemo(
    () =>
      ingestionList.some(
        (ingestion) => ingestion.pipelineType === PipelineType.DataInsight
      ),
    [ingestionList]
  );

  const types = useMemo(
    (): PipelineType[] =>
      getIngestionTypes(
        supportedPipelineTypes,
        isMetaMartService,
        ingestionList,
        pipelineType
      ),
    [pipelineType, supportedPipelineTypes, isMetaMartService, ingestionList]
  );

  if (types.length === 0) {
    return null;
  }

  return (
    <LimitWrapper resource="ingestionPipeline">
      <Dropdown
        menu={{
          items: getMenuItems(types, isDataInSightIngestionExists),
          onClick: (item) => {
            handleAddIngestionClick(item.key as PipelineType);
          },
        }}
        placement="bottomRight"
        trigger={['click']}>
        <Button
          className="flex-center gap-2"
          data-testid="add-new-ingestion-button"
          type="primary"
          onClick={handleAddIngestionButtonClick}>
          {getIngestionButtonText(hasMetadata, pipelineType)}
          {hasMetadata && <DropdownIcon height={14} width={14} />}
        </Button>
      </Dropdown>
    </LimitWrapper>
  );
}

export default AddIngestionButton;
