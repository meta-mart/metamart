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
import { expect, test } from '@playwright/test';
import { GlobalSettingOptions } from '../../constant/settings';
import { descriptionBox, redirectToHomePage, uuid } from '../../utils/common';
import { settingClick } from '../../utils/sidebar';

const apiServiceConfig = {
  name: `pw-api-service-${uuid()}`,
  displayName: `API Service-${uuid()}`,
  description: 'Testing API service',
  openAPISchemaURL: 'https://example.com/swagger.json',
  token: '********',
};

// use the admin user to login
test.use({ storageState: 'playwright/.auth/admin.json' });

test.describe('API service', () => {
  test.beforeEach('Visit entity details page', async ({ page }) => {
    await redirectToHomePage(page);
  });

  test('add update and delete api service type REST', async ({ page }) => {
    await settingClick(page, GlobalSettingOptions.APIS);

    await page.getByTestId('add-service-button').click();
    await page.getByTestId('Rest').click();
    await page.getByTestId('next-button').click();

    // step 1
    await page.getByTestId('service-name').fill(apiServiceConfig.name);
    await page.fill(descriptionBox, apiServiceConfig.description);
    await page.getByTestId('next-button').click();

    // step 2
    await page
      .locator('#root\\/openAPISchemaURL')
      .fill(apiServiceConfig.openAPISchemaURL);

    await page.locator('#root\\/token').fill(apiServiceConfig.token);
    await page.getByTestId('submit-btn').click();

    // step 3
    await page.getByTestId('view-service-button').click();

    await expect(page.getByTestId('entity-header-display-name')).toHaveText(
      apiServiceConfig.name
    );

    // update display name
    await page.getByTestId('manage-button').click();
    await page.getByTestId('rename-button').click();

    await page.locator('#displayName').fill(apiServiceConfig.displayName);
    await page.getByTestId('save-button').click();

    await expect(page.getByTestId('entity-header-display-name')).toHaveText(
      apiServiceConfig.displayName
    );

    // delete the entity
    await page.getByTestId('manage-button').click();
    await page.getByTestId('delete-button').click();

    await page.waitForSelector('[role="dialog"].ant-modal');

    await expect(page.locator('[role="dialog"].ant-modal')).toBeVisible();

    await page.click('[data-testid="hard-delete-option"]');
    await page.check('[data-testid="hard-delete"]');
    await page.fill('[data-testid="confirmation-text-input"]', 'DELETE');

    const deleteResponse = page.waitForResponse(
      '/api/v1/services/apiServices/*?hardDelete=true&recursive=true'
    );

    await page.click('[data-testid="confirm-button"]');

    await deleteResponse;

    await expect(page.locator('.Toastify__toast-body')).toHaveText(
      /deleted successfully!/
    );

    await page.click('.Toastify__close-button');
  });
});
