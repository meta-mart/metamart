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

import { ServiceCategory } from '../../../../enums/service.enum';
import { PipelineType } from '../../../../generated/api/services/ingestionPipelines/createIngestionPipeline';
import { IngestionPipeline } from '../../../../generated/entity/services/ingestionPipelines/ingestionPipeline';
import { Paging } from '../../../../generated/type/paging';
import { UsePagingInterface } from '../../../../hooks/paging/usePaging';
import { UseAirflowStatusProps } from '../../../../hooks/useAirflowStatus';
import { ServicesType } from '../../../../interface/service.interface';
import { PagingHandlerParams } from '../../../common/NextPrevious/NextPrevious.interface';

export interface ConnectorConfig {
  username: string;
  password: string;
  host: string;
  database: string;
  includeFilterPattern: Array<string>;
  excludeFilterPattern: Array<string>;
  includeViews: boolean;
  excludeDataProfiler?: boolean;
  enableDataProfiler?: boolean;
}

export interface IngestionProps {
  ingestionPagingInfo: UsePagingInterface;
  serviceDetails: ServicesType;
  serviceName: string;
  serviceCategory: ServiceCategory;
  ingestionPipelineList: Array<IngestionPipeline>;
  pipelineType?: PipelineType;
  isLoading?: boolean;
  searchText: string;
  airflowInformation: UseAirflowStatusProps;
  onIngestionWorkflowsUpdate: (
    paging?: Omit<Paging, 'total'>,
    limit?: number
  ) => void;
  handleIngestionListUpdate: (
    ingestionList: React.SetStateAction<IngestionPipeline[]>
  ) => void;
  handleSearchChange: (searchValue: string) => void;
  onPageChange: ({ cursorType, currentPage }: PagingHandlerParams) => void;
}

export interface SelectedRowDetails {
  id: string;
  name: string;
  displayName?: string;
  state: string;
}
