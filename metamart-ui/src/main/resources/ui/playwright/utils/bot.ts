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
import { expect, Page } from '@playwright/test';
import {
  customFormatDateTime,
  getEpochMillisForFutureDays,
} from '../../src/utils/date-time/DateTimeUtils';
import { GlobalSettingOptions } from '../constant/settings';
import {
  descriptionBox,
  redirectToHomePage,
  toastNotification,
  uuid,
} from './common';
import { settingClick } from './sidebar';
import { revokeToken } from './user';

const botName = `pw%bot-test-${uuid()}`;

const BOT_DETAILS = {
  botName: botName,
  botEmail: `${botName}@mail.com`,
  description: 'This is bot description',
  updatedDescription: 'This is updated bot description',
  updatedBotName: `updated-${botName}`,
  unlimitedExpiryTime: 'This token has no expiration date.',
  JWTToken: 'MetaMart JWT',
};

const EXPIRATION_TIME = [1, 7, 30, 60, 90];

export const getCreatedBot = async (
  page: Page,
  {
    botName,
    botDisplayName,
  }: {
    botName: string;
    botDisplayName?: string;
  }
) => {
  // Click on created Bot name
  const fetchResponse = page.waitForResponse(
    `/api/v1/bots/name/${encodeURIComponent(botName)}?*`
  );
  await page.getByTestId(`bot-link-${botDisplayName ?? botName}`).click();
  await fetchResponse;
};

export const createBot = async (page: Page) => {
  // Click on add bot button
  await page.getByTestId('add-bot').click();

  // Fill the form details
  await page.getByTestId('email').fill(BOT_DETAILS.botEmail);

  await page.getByTestId('displayName').fill(BOT_DETAILS.botName);

  // Select expiry time
  await page.click('[data-testid="token-expiry"]');
  await page.locator('[title="1 hr"] div').click();

  await page.locator(descriptionBox).fill(BOT_DETAILS.description);

  const saveResponse = page.waitForResponse('/api/v1/bots');
  await page.click('[data-testid="save-user"]');
  await saveResponse;

  // Verify bot is getting added in the bots listing page
  const table = page.locator('table');

  await expect(table).toContainText(BOT_DETAILS.botName);
  await expect(table).toContainText(BOT_DETAILS.description);

  // Get created bot
  await getCreatedBot(page, { botName });

  await expect(page.getByTestId('revoke-button')).toContainText('Revoke token');

  await expect(page.getByTestId('center-panel')).toContainText(
    `${BOT_DETAILS.JWTToken} Token`
  );

  await expect(page.getByTestId('token-expiry')).toBeVisible();

  await toastNotification(page, 'Bot created successfully.');
};

export const deleteBot = async (page: Page) => {
  await settingClick(page, GlobalSettingOptions.BOTS);

  // Click on delete button
  await page.getByTestId(`bot-delete-${botName}`).click();

  await page.getByTestId('hard-delete-option').click();

  await page.getByTestId('confirmation-text-input').fill('DELETE');

  const deleteResponse = page.waitForResponse(`/api/v1/bots/*`);

  await page.getByTestId('confirm-button').click();

  await deleteResponse;

  await toastNotification(page, /deleted successfully!/);

  await expect(page.getByTestId('page-layout-v1')).not.toContainText(botName);
};

export const updateBotDetails = async (page: Page) => {
  await page.click('[data-testid="edit-displayName"]');
  await page.getByTestId('displayName').fill(BOT_DETAILS.updatedBotName);

  const updateDisplayNameResponse = page.waitForResponse(`api/v1/bots/*`);
  await page.getByTestId('save-displayName').click();
  await updateDisplayNameResponse;

  // Verify the display name is updated on bot details page
  await expect(
    page.locator('[data-testid="left-panel"] .display-name')
  ).toContainText(BOT_DETAILS.updatedBotName);

  // Click on edit description button
  await page.getByTestId('edit-description').click();
  await page.locator(descriptionBox).fill(BOT_DETAILS.updatedDescription);

  const updateDescriptionResponse = page.waitForResponse(`api/v1/bots/*`);
  await page.getByTestId('save').click();
  await updateDescriptionResponse;

  // Click on the breadcrumb link to go back to the bots listing page
  const getBotsPageResponse = page.waitForResponse('/api/v1/bots*');
  await page.locator('[data-testid="breadcrumb-link"]').first().click();
  await getBotsPageResponse;

  // Verify the updated name is displayed in the Bots listing page
  await expect(
    page.getByTestId(`bot-link-${BOT_DETAILS.updatedBotName}`)
  ).toContainText(BOT_DETAILS.updatedBotName);

  await expect(
    page.locator(`[data-row-key="${botName}"] [data-testid="markdown-parser"]`)
  ).toContainText(BOT_DETAILS.updatedDescription);
};

export const tokenExpirationForDays = async (page: Page) => {
  await getCreatedBot(page, {
    botName,
    botDisplayName: BOT_DETAILS.updatedBotName,
  });
  for (const expiryTime of EXPIRATION_TIME) {
    await revokeToken(page, true);

    // Click on dropdown
    await page.click('[data-testid="token-expiry"]');

    // Select the expiration period
    await page.locator(`text=${expiryTime} days`).click();

    // Save the updated date
    const expiryDate = customFormatDateTime(
      getEpochMillisForFutureDays(expiryTime),
      `ccc d'th' MMMM, yyyy`
    );

    await page.click('[data-testid="save-edit"]');

    await expect(
      page.locator('[data-testid="center-panel"] [data-testid="revoke-button"]')
    ).toBeVisible();

    // Verify the expiry time
    const tokenExpiryText = await page
      .locator('[data-testid="token-expiry"]')
      .innerText();

    expect(tokenExpiryText).toContain(`Expires on ${expiryDate}`);
  }
};

export const tokenExpirationUnlimitedDays = async (page: Page) => {
  await revokeToken(page, true);

  // Click on expiry token dropdown
  await page.click('[data-testid="token-expiry"]');
  // Select unlimited days
  await page.getByText('Unlimited days').click();
  // Save the selected changes
  await page.click('[data-testid="save-edit"]');

  // Verify the updated expiry time
  const revokeButton = page.locator(
    '[data-testid="center-panel"] [data-testid="revoke-button"]'
  );

  await expect(revokeButton).toBeVisible();

  // Verify the expiry time
  const tokenExpiry = page.locator('[data-testid="token-expiry"]');

  await expect(tokenExpiry).toBeVisible();

  const tokenExpiryText = await tokenExpiry.innerText();

  expect(tokenExpiryText).toContain(BOT_DETAILS.unlimitedExpiryTime);
};

export const redirectToBotPage = async (page: Page) => {
  await redirectToHomePage(page);
  const fetchResponse = page.waitForResponse('api/v1/bots?*');
  await settingClick(page, GlobalSettingOptions.BOTS);
  await fetchResponse;
};
