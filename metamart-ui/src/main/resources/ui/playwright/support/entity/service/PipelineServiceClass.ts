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
import { APIRequestContext, Page } from '@playwright/test';
import { Operation } from 'fast-json-patch';
import { SERVICE_TYPE } from '../../../constant/service';
import { uuid } from '../../../utils/common';
import { visitServiceDetailsPage } from '../../../utils/service';
import { EntityTypeEndpoint } from '../Entity.interface';
import { EntityClass } from '../EntityClass';

export class PipelineServiceClass extends EntityClass {
  entity = {
    name: `pw-pipeline-service-${uuid()}`,
    serviceType: 'Dagster',
    connection: {
      config: {
        type: 'Dagster',
        host: 'admin',
        token: 'admin',
        timeout: '1000',
        supportsMetadataExtraction: true,
      },
    },
  };

  entityResponseData: unknown;

  constructor(name?: string) {
    super(EntityTypeEndpoint.PipelineService);
    this.entity.name = name ?? this.entity.name;
    this.type = 'Pipeline Service';
  }

  async create(apiContext: APIRequestContext) {
    const serviceResponse = await apiContext.post(
      '/api/v1/services/pipelineServices',
      {
        data: this.entity,
      }
    );

    const service = await serviceResponse.json();

    this.entityResponseData = service;

    return service;
  }

  async patch(apiContext: APIRequestContext, payload: Operation[]) {
    const serviceResponse = await apiContext.patch(
      `/api/v1/services/pipelineServices/${this.entityResponseData?.['id']}`,
      {
        data: payload,
        headers: {
          'Content-Type': 'application/json-patch+json',
        },
      }
    );

    const service = await serviceResponse.json();

    this.entityResponseData = service;

    return service;
  }

  get() {
    return this.entityResponseData;
  }

  async visitEntityPage(page: Page) {
    await visitServiceDetailsPage(
      page,
      {
        name: this.entity.name,
        type: SERVICE_TYPE.Pipeline,
      },
      false
    );
  }

  async delete(apiContext: APIRequestContext) {
    const serviceResponse = await apiContext.delete(
      `/api/v1/services/pipelineServices/name/${encodeURIComponent(
        this.entityResponseData?.['fullyQualifiedName']
      )}?recursive=true&hardDelete=true`
    );

    return await serviceResponse.json();
  }
}
