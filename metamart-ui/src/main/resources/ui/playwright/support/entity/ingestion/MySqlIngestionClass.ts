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
import {
  expect,
  Page,
  PlaywrightTestArgs,
  PlaywrightWorkerArgs,
  TestType,
} from '@playwright/test';
import { env } from 'process';
import {
  getApiContext,
  redirectToHomePage,
  toastNotification,
  uuid,
} from '../../../utils/common';
import { visitEntityPage } from '../../../utils/entity';
import { visitServiceDetailsPage } from '../../../utils/service';
import {
  checkServiceFieldSectionHighlighting,
  Services,
} from '../../../utils/serviceIngestion';
import ServiceBaseClass from './ServiceBaseClass';

class MysqlIngestionClass extends ServiceBaseClass {
  name: string;
  tableFilter: string[];
  profilerTable = 'alert_entity';
  constructor() {
    super(
      Services.Database,
      `pw-mysql-with-%-${uuid()}`,
      'Mysql',
      'bot_entity'
    );
    this.tableFilter = ['bot_entity', 'alert_entity', 'chart_entity'];
  }

  async createService(page: Page) {
    await super.createService(page);
  }

  async updateService(page: Page) {
    await super.updateService(page);
  }

  async fillConnectionDetails(page: Page) {
    const username = env.PLAYWRIGHT_MYSQL_USERNAME ?? '';
    const password = env.PLAYWRIGHT_MYSQL_PASSWORD ?? '';
    const hostPort = env.PLAYWRIGHT_MYSQL_HOST_PORT ?? '';

    await page.fill('#root\\/username', username);
    await checkServiceFieldSectionHighlighting(page, 'username');
    await page.fill('#root\\/authType\\/password', password);
    await checkServiceFieldSectionHighlighting(page, 'password');
    await page.fill('#root\\/hostPort', hostPort);
    await checkServiceFieldSectionHighlighting(page, 'hostPort');
  }

  async fillIngestionDetails(page: Page) {
    for (const filter of this.tableFilter) {
      await page.fill('#root\\/tableFilterPattern\\/includes', filter);
      await page
        .locator('#root\\/tableFilterPattern\\/includes')
        .press('Enter');
    }
  }

  async runAdditionalTests(
    page: Page,
    test: TestType<PlaywrightTestArgs, PlaywrightWorkerArgs>
  ) {
    await test.step('Add Profiler ingestion', async () => {
      const { apiContext } = await getApiContext(page);
      await redirectToHomePage(page);
      await visitServiceDetailsPage(
        page,
        {
          type: this.category,
          name: this.serviceName,
          displayName: this.serviceName,
        },
        true
      );

      await page.click('[data-testid="ingestions"]');
      await page.waitForSelector('[data-testid="ingestion-details-container"]');
      await page.waitForTimeout(1000);
      await page.click('[data-testid="add-new-ingestion-button"]');
      await page.waitForTimeout(1000);
      await page.click('[data-menu-id*="profiler"]');

      await page.waitForSelector('#root\\/profileSample');
      await page.fill('#root\\/profileSample', '10');
      await page.click('[data-testid="submit-btn"]');
      // Make sure we create ingestion with None schedule to avoid conflict between Airflow and Argo behavior
      await this.scheduleIngestion(page);

      await page.click('[data-testid="view-service-button"]');

      // Header available once page loads
      await page.waitForSelector('[data-testid="data-assets-header"]');
      await page.getByTestId('loader').waitFor({ state: 'detached' });
      await page.getByTestId('ingestions').click();
      await page
        .getByLabel('Ingestions')
        .getByTestId('loader')
        .waitFor({ state: 'detached' });

      const response = await apiContext
        .get(
          `/api/v1/services/ingestionPipelines?service=${encodeURIComponent(
            this.serviceName
          )}&pipelineType=profiler&serviceType=databaseService&limit=1`
        )
        .then((res) => res.json());

      // need manual wait to settle down the deployed pipeline, before triggering the pipeline
      await page.waitForTimeout(3000);

      await page.click(
        `[data-row-key*="${response.data[0].name}"] [data-testid="more-actions"]`
      );
      await page.getByTestId('run-button').click();

      await toastNotification(page, `Pipeline triggered successfully!`);

      // need manual wait to make sure we are awaiting on latest run results
      await page.waitForTimeout(2000);

      await this.handleIngestionRetry('profiler', page);
    });

    await test.step('Validate profiler ingestion', async () => {
      await visitEntityPage({
        page,
        searchTerm: this.profilerTable,
        dataTestId: `${this.serviceName}-${this.profilerTable}`,
      });
    });

    await page.getByTestId('profiler').click();
    await page
      .getByTestId('profiler-tab-left-panel')
      .getByText('Table Profile')
      .click();

    await expect(
      page.locator('[data-testid="no-profiler-placeholder"]')
    ).not.toBeVisible();
  }

  async validateIngestionDetails(page: Page) {
    await page.waitForSelector('.ant-select-selection-item-content');

    await expect(page.locator('.ant-select-selection-item-content')).toHaveText(
      this.tableFilter
    );
  }
}

export default MysqlIngestionClass;
