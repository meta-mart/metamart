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
import test, { expect } from '@playwright/test';
import { get } from 'lodash';
import { SidebarItem } from '../../constant/sidebar';
import { DashboardClass } from '../../support/entity/DashboardClass';
import { EntityTypeEndpoint } from '../../support/entity/Entity.interface';
import { TableClass } from '../../support/entity/TableClass';
import { TopicClass } from '../../support/entity/TopicClass';
import { Glossary } from '../../support/glossary/Glossary';
import { GlossaryTerm } from '../../support/glossary/GlossaryTerm';
import { TeamClass } from '../../support/team/TeamClass';
import { UserClass } from '../../support/user/UserClass';
import { performAdminLogin } from '../../utils/admin';
import {
  getRandomLastName,
  redirectToHomePage,
  toastNotification,
  uuid,
} from '../../utils/common';
import {
  addMultiOwner,
  assignGlossaryTerm,
  assignTag,
  updateDescription,
} from '../../utils/entity';
import {
  addAssetToGlossaryTerm,
  addReferences,
  addRelatedTerms,
  addSynonyms,
  approveGlossaryTermTask,
  approveTagsTask,
  assignTagToGlossaryTerm,
  changeTermHierarchyFromModal,
  confirmationDragAndDropGlossary,
  createDescriptionTaskForGlossary,
  createGlossary,
  createGlossaryTerms,
  createTagTaskForGlossary,
  deleteGlossaryOrGlossaryTerm,
  dragAndDropTerm,
  goToAssetsTab,
  renameGlossaryTerm,
  selectActiveGlossary,
  selectActiveGlossaryTerm,
  validateGlossaryTerm,
  verifyGlossaryDetails,
  verifyGlossaryTermAssets,
} from '../../utils/glossary';
import { sidebarClick } from '../../utils/sidebar';
import { TaskDetails } from '../../utils/task';
import { performUserLogin } from '../../utils/user';

const user1 = new UserClass();
const user2 = new UserClass();
const team = new TeamClass();

test.describe('Glossary tests', () => {
  test.beforeAll(async ({ browser }) => {
    const { afterAction, apiContext } = await performAdminLogin(browser);
    await user2.create(apiContext);
    await user1.create(apiContext);
    team.data.users = [user2.responseData.id];
    await team.create(apiContext);
    await afterAction();
  });

  test('Glossary & terms creation for reviewer as user', async ({
    browser,
  }) => {
    test.slow(true);

    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const { page: page1, afterAction: afterActionUser1 } =
      await performUserLogin(browser, user1);
    const glossary1 = new Glossary();
    glossary1.data.owners = [{ name: 'admin', type: 'user' }];
    glossary1.data.mutuallyExclusive = true;
    glossary1.data.reviewers = [
      { name: `${user1.data.firstName}${user1.data.lastName}`, type: 'user' },
    ];
    glossary1.data.terms = [new GlossaryTerm(glossary1)];

    await test.step('Create Glossary', async () => {
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await createGlossary(page, glossary1.data, false);
      await verifyGlossaryDetails(page, glossary1.data);
    });

    await test.step('Create Glossary Terms', async () => {
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await createGlossaryTerms(page, glossary1.data);
    });

    await test.step('Approve Glossary Term from Glossary Listing', async () => {
      await redirectToHomePage(page1);
      await sidebarClick(page1, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page1, glossary1.data.name);

      await approveGlossaryTermTask(page1, glossary1.data.terms[0].data);
      await redirectToHomePage(page1);
      await sidebarClick(page1, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page1, glossary1.data.name);
      await validateGlossaryTerm(
        page1,
        glossary1.data.terms[0].data,
        'Approved'
      );

      await afterActionUser1();
    });

    await glossary1.delete(apiContext);
    await afterAction();
  });

  test('Glossary & terms creation for reviewer as team', async ({
    browser,
  }) => {
    test.slow(true);

    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const { page: page1, afterAction: afterActionUser1 } =
      await performUserLogin(browser, user2);

    const glossary2 = new Glossary();
    glossary2.data.owners = [{ name: 'admin', type: 'user' }];
    glossary2.data.reviewers = [{ name: team.data.displayName, type: 'team' }];
    glossary2.data.terms = [new GlossaryTerm(glossary2)];

    await test.step('Create Glossary', async () => {
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await createGlossary(page, glossary2.data, false);
      await verifyGlossaryDetails(page, glossary2.data);
    });

    await test.step('Create Glossary Terms', async () => {
      await redirectToHomePage(page);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await createGlossaryTerms(page, glossary2.data);
    });

    await test.step('Approve Glossary Term from Glossary Listing', async () => {
      await redirectToHomePage(page1);
      await sidebarClick(page1, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page1, glossary2.data.name);
      await approveGlossaryTermTask(page1, glossary2.data.terms[0].data);

      await redirectToHomePage(page1);
      await sidebarClick(page1, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page1, glossary2.data.name);
      await validateGlossaryTerm(
        page1,
        glossary2.data.terms[0].data,
        'Approved'
      );

      await afterActionUser1();
    });

    await glossary2.delete(apiContext);
    await afterAction();
  });

  test('Update Glossary and Glossary Term', async ({ browser }) => {
    test.slow(true);

    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const glossary1 = new Glossary();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    const glossaryTerm2 = new GlossaryTerm(glossary1);
    glossary1.data.terms = [glossaryTerm1, glossaryTerm2];
    const user3 = new UserClass();
    const user4 = new UserClass();
    await glossary1.create(apiContext);
    await glossaryTerm1.create(apiContext);
    await glossaryTerm2.create(apiContext);
    await user3.create(apiContext);
    await user4.create(apiContext);

    try {
      await test.step('Update Glossary', async () => {
        await sidebarClick(page, SidebarItem.GLOSSARY);
        await selectActiveGlossary(page, glossary1.data.displayName);

        // Update description
        await updateDescription(page, 'Demo description to be updated');

        // Update Owners
        await addMultiOwner({
          page,
          ownerNames: [user3.getUserName()],
          activatorBtnDataTestId: 'add-owner',
          resultTestId: 'glossary-right-panel-owner-link',
          endpoint: EntityTypeEndpoint.Glossary,
          isSelectableInsideForm: false,
          type: 'Users',
        });

        // Update Reviewer
        await addMultiOwner({
          page,
          ownerNames: [user3.getUserName()],
          activatorBtnDataTestId: 'Add',
          resultTestId: 'glossary-reviewer-name',
          endpoint: EntityTypeEndpoint.Glossary,
          type: 'Users',
        });

        await assignTag(page, 'PersonalData.Personal');
      });

      await test.step('Update Glossary Term', async () => {
        await redirectToHomePage(page);
        await sidebarClick(page, SidebarItem.GLOSSARY);
        await selectActiveGlossary(page, glossary1.data.displayName);
        await selectActiveGlossaryTerm(page, glossaryTerm1.data.displayName);
        // Update description
        await updateDescription(page, 'Demo description to be updated');

        // Update Synonyms
        await addSynonyms(page, [getRandomLastName(), getRandomLastName()]);

        // Update References
        const references = [
          { name: getRandomLastName(), url: 'http://example.com' },
          { name: getRandomLastName(), url: 'http://trial.com' },
        ];
        await addReferences(page, references);

        // Update Related Terms
        await addRelatedTerms(page, [glossaryTerm2]);

        // Update Tag
        await assignTagToGlossaryTerm(
          page,
          'PersonalData.Personal',
          'Add',
          'panel-container'
        );
      });
    } finally {
      await glossaryTerm1.delete(apiContext);
      await glossaryTerm2.delete(apiContext);
      await glossary1.delete(apiContext);
      await user3.delete(apiContext);
      await user4.delete(apiContext);
      await afterAction();
    }
  });

  test('Add and Remove Assets', async ({ browser }) => {
    test.slow(true);

    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const glossary1 = new Glossary();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    const glossaryTerm2 = new GlossaryTerm(glossary1);
    glossary1.data.mutuallyExclusive = true;
    glossary1.data.terms = [glossaryTerm1, glossaryTerm2];

    const glossary2 = new Glossary();
    const glossaryTerm3 = new GlossaryTerm(glossary2);
    const glossaryTerm4 = new GlossaryTerm(glossary2);
    glossary2.data.terms = [glossaryTerm3, glossaryTerm4];

    await glossary1.create(apiContext);
    await glossary2.create(apiContext);
    await glossaryTerm1.create(apiContext);
    await glossaryTerm2.create(apiContext);
    await glossaryTerm3.create(apiContext);
    await glossaryTerm4.create(apiContext);

    const dashboardEntity = new DashboardClass();
    await dashboardEntity.create(apiContext);

    try {
      await test.step('Add asset to glossary term using entity', async () => {
        await sidebarClick(page, SidebarItem.GLOSSARY);

        await selectActiveGlossary(page, glossary2.data.displayName);
        await goToAssetsTab(page, glossaryTerm3.data.displayName);

        await page.waitForSelector(
          'text=Adding a new Asset is easy, just give it a spin!'
        );

        await dashboardEntity.visitEntityPage(page);

        // Dashboard Entity Right Panel
        await page.click(
          '[data-testid="entity-right-panel"] [data-testid="glossary-container"] [data-testid="add-tag"]'
        );

        // Select 1st term
        await page.click('[data-testid="tag-selector"] #tagsForm_tags');

        const glossaryRequest = page.waitForResponse(
          `/api/v1/search/query?q=*&index=glossary_term_search_index&from=0&size=25&deleted=false&track_total_hits=true&getHierarchy=true`
        );
        await page.type(
          '[data-testid="tag-selector"] #tagsForm_tags',
          glossaryTerm1.data.name
        );
        await glossaryRequest;

        await page.getByText(glossaryTerm1.data.displayName).click();
        await page.waitForSelector(
          '[data-testid="tag-selector"]:has-text("' +
            glossaryTerm1.data.displayName +
            '")'
        );

        // Select 2nd term
        await page.click('[data-testid="tag-selector"] #tagsForm_tags');

        const glossaryRequest2 = page.waitForResponse(
          `/api/v1/search/query?q=*&index=glossary_term_search_index&from=0&size=25&deleted=false&track_total_hits=true&getHierarchy=true`
        );
        await page.type(
          '[data-testid="tag-selector"] #tagsForm_tags',
          glossaryTerm2.data.name
        );
        await glossaryRequest2;

        await page.getByText(glossaryTerm2.data.displayName).click();

        await page.waitForSelector(
          '[data-testid="tag-selector"]:has-text("' +
            glossaryTerm2.data.displayName +
            '")'
        );

        const patchRequest = page.waitForResponse(`/api/v1/dashboards/*`);

        await expect(page.getByTestId('saveAssociatedTag')).toBeEnabled();

        await page.getByTestId('saveAssociatedTag').click();
        await patchRequest;

        await toastNotification(
          page,
          /mutually exclusive and can't be assigned together/
        );

        // Add non mutually exclusive tags
        await page.click(
          '[data-testid="entity-right-panel"] [data-testid="glossary-container"] [data-testid="add-tag"]'
        );

        // Select 1st term
        await page.click('[data-testid="tag-selector"] #tagsForm_tags');

        const glossaryRequest3 = page.waitForResponse(
          `/api/v1/search/query?q=*&index=glossary_term_search_index&from=0&size=25&deleted=false&track_total_hits=true&getHierarchy=true`
        );
        await page.type(
          '[data-testid="tag-selector"] #tagsForm_tags',
          glossaryTerm3.data.name
        );
        await glossaryRequest3;

        await page.getByText(glossaryTerm3.data.displayName).click();
        await page.waitForSelector(
          '[data-testid="tag-selector"]:has-text("' +
            glossaryTerm3.data.displayName +
            '")'
        );

        // Select 2nd term
        await page.click('[data-testid="tag-selector"] #tagsForm_tags');

        const glossaryRequest4 = page.waitForResponse(
          `/api/v1/search/query?q=*&index=glossary_term_search_index&from=0&size=25&deleted=false&track_total_hits=true&getHierarchy=true`
        );
        await page.type(
          '[data-testid="tag-selector"] #tagsForm_tags',
          glossaryTerm4.data.name
        );
        await glossaryRequest4;

        await page.getByText(glossaryTerm4.data.displayName).click();

        await page.waitForSelector(
          '[data-testid="tag-selector"]:has-text("' +
            glossaryTerm4.data.displayName +
            '")'
        );

        const patchRequest2 = page.waitForResponse(`/api/v1/dashboards/*`);

        await expect(page.getByTestId('saveAssociatedTag')).toBeEnabled();

        await page.getByTestId('saveAssociatedTag').click();
        await patchRequest2;

        // Check if the terms are present
        const glossaryContainer = await page.locator(
          '[data-testid="entity-right-panel"] [data-testid="glossary-container"]'
        );
        const glossaryContainerText = await glossaryContainer.innerText();

        expect(glossaryContainerText).toContain(glossaryTerm3.data.displayName);
        expect(glossaryContainerText).toContain(glossaryTerm4.data.displayName);

        // Check if the icons are present

        const icons = await page.locator(
          '[data-testid="entity-right-panel"] [data-testid="glossary-container"] [data-testid="glossary-icon"]'
        );

        expect(await icons.count()).toBe(2);

        // Add Glossary to Dashboard Charts
        await page.click(
          '[data-testid="glossary-tags-0"] > [data-testid="tags-wrapper"] > [data-testid="glossary-container"] > [data-testid="entity-tags"] [data-testid="add-tag"]'
        );

        await page.click('[data-testid="tag-selector"]');

        const glossaryRequest5 = page.waitForResponse(
          `/api/v1/search/query?q=*&index=glossary_term_search_index&from=0&size=25&deleted=false&track_total_hits=true&getHierarchy=true`
        );
        await page.type(
          '[data-testid="tag-selector"] #tagsForm_tags',
          glossaryTerm3.data.name
        );
        await glossaryRequest5;

        await page
          .getByRole('tree')
          .getByTestId(`tag-${glossaryTerm3.data.fullyQualifiedName}`)
          .click();

        await page.waitForSelector(
          '[data-testid="tag-selector"]:has-text("' +
            glossaryTerm3.data.displayName +
            '")'
        );

        const patchRequest3 = page.waitForResponse(`/api/v1/charts/*`);

        await expect(page.getByTestId('saveAssociatedTag')).toBeEnabled();

        await page.getByTestId('saveAssociatedTag').click();
        await patchRequest3;

        // Check if the term is present
        const tagSelectorText = await page
          .locator(
            '[data-testid="glossary-tags-0"] [data-testid="glossary-container"] [data-testid="tags"]'
          )
          .innerText();

        expect(tagSelectorText).toContain(glossaryTerm3.data.displayName);

        // Check if the icon is visible
        const icon = await page.locator(
          '[data-testid="glossary-tags-0"] > [data-testid="tags-wrapper"] > [data-testid="glossary-container"] [data-testid="glossary-icon"]'
        );

        expect(await icon.isVisible()).toBe(true);

        await sidebarClick(page, SidebarItem.GLOSSARY);

        await selectActiveGlossary(page, glossary2.data.displayName);
        await goToAssetsTab(page, glossaryTerm3.data.displayName, 2);

        // Check if the selected asset are present
        const assetContainer = await page.locator(
          '[data-testid="table-container"] .assets-data-container'
        );

        const assetContainerText = await assetContainer.innerText();

        expect(assetContainerText).toContain(dashboardEntity.entity.name);
        expect(assetContainerText).toContain(dashboardEntity.charts.name);
      });
    } finally {
      await glossaryTerm1.delete(apiContext);
      await glossaryTerm2.delete(apiContext);
      await glossaryTerm3.delete(apiContext);
      await glossaryTerm4.delete(apiContext);
      await glossary1.delete(apiContext);
      await glossary2.delete(apiContext);
      await dashboardEntity.delete(apiContext);
      await afterAction();
    }
  });

  test('Rename Glossary Term and verify assets', async ({ browser }) => {
    test.slow(true);

    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const table = new TableClass();
    const topic = new TopicClass();
    const dashboard = new DashboardClass();

    await table.create(apiContext);
    await topic.create(apiContext);
    await dashboard.create(apiContext);

    const glossary1 = new Glossary();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    glossary1.data.terms = [glossaryTerm1];
    await glossary1.create(apiContext);
    await glossaryTerm1.create(apiContext);

    const assets = [table, topic, dashboard];

    try {
      await test.step('Rename Glossary Term', async () => {
        const newName = `PW.${uuid()}%${getRandomLastName()}`;
        await redirectToHomePage(page);
        await sidebarClick(page, SidebarItem.GLOSSARY);
        await selectActiveGlossary(page, glossary1.data.displayName);
        await goToAssetsTab(page, glossaryTerm1.data.displayName);
        await addAssetToGlossaryTerm(page, assets);
        await renameGlossaryTerm(page, glossaryTerm1, newName);
        await verifyGlossaryTermAssets(
          page,
          glossary1.data,
          glossaryTerm1.data,
          assets.length
        );
      });

      await test.step('Rename the same entity again', async () => {
        const newName = `PW Space.${uuid()}%${getRandomLastName()}`;
        await redirectToHomePage(page);
        await sidebarClick(page, SidebarItem.GLOSSARY);
        await selectActiveGlossary(page, glossary1.data.displayName);
        await goToAssetsTab(
          page,
          glossaryTerm1.data.displayName,
          assets.length
        );
        await renameGlossaryTerm(page, glossaryTerm1, newName);
        await verifyGlossaryTermAssets(
          page,
          glossary1.data,
          glossaryTerm1.data,
          assets.length
        );
      });
    } finally {
      await table.delete(apiContext);
      await topic.delete(apiContext);
      await dashboard.delete(apiContext);
      await glossaryTerm1.delete(apiContext);
      await glossary1.delete(apiContext);
      await afterAction();
    }
  });

  test('Drag and Drop Glossary Term', async ({ browser }) => {
    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const glossary1 = new Glossary();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    const glossaryTerm2 = new GlossaryTerm(glossary1);
    glossary1.data.terms = [glossaryTerm1, glossaryTerm2];

    try {
      await glossary1.create(apiContext);
      await glossaryTerm1.create(apiContext);
      await glossaryTerm2.create(apiContext);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);

      await test.step('Drag and Drop Glossary Term', async () => {
        await dragAndDropTerm(
          page,
          glossaryTerm1.data.displayName,
          glossaryTerm2.data.displayName
        );

        await confirmationDragAndDropGlossary(
          page,
          glossaryTerm1.data.name,
          glossaryTerm2.data.name
        );

        await expect(
          page.getByRole('cell', {
            name: glossaryTerm1.responseData.displayName,
          })
        ).not.toBeVisible();

        const termRes = page.waitForResponse('/api/v1/glossaryTerms?*');

        // verify the term is moved under the parent term
        await page.getByTestId('expand-collapse-all-button').click();
        await termRes;

        await expect(
          page.getByRole('cell', {
            name: glossaryTerm1.responseData.displayName,
          })
        ).toBeVisible();
      });

      await test.step(
        'Drag and Drop Glossary Term back at parent level',
        async () => {
          await redirectToHomePage(page);
          await sidebarClick(page, SidebarItem.GLOSSARY);
          await selectActiveGlossary(page, glossary1.data.displayName);
          await page.getByTestId('expand-collapse-all-button').click();

          await dragAndDropTerm(
            page,
            glossaryTerm1.data.displayName,
            'Terms' // Header Cell
          );

          await confirmationDragAndDropGlossary(
            page,
            glossaryTerm1.data.name,
            glossary1.responseData.displayName,
            true
          );

          // verify the term is moved back at parent level
          await expect(
            page.getByRole('cell', {
              name: glossaryTerm1.responseData.displayName,
            })
          ).toBeVisible();
        }
      );
    } finally {
      await glossaryTerm1.delete(apiContext);
      await glossaryTerm2.delete(apiContext);
      await glossary1.delete(apiContext);
      await afterAction();
    }
  });

  test('Change glossary term hierarchy using menu options', async ({
    browser,
  }) => {
    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const glossary1 = new Glossary();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    const glossaryTerm2 = new GlossaryTerm(glossary1);
    glossary1.data.terms = [glossaryTerm1, glossaryTerm2];

    try {
      await glossary1.create(apiContext);
      await glossaryTerm1.create(apiContext);
      await glossaryTerm2.create(apiContext);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);

      await changeTermHierarchyFromModal(
        page,
        glossaryTerm1.data.displayName,
        glossaryTerm2.data.displayName
      );

      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);

      await expect(
        page.getByRole('cell', {
          name: glossaryTerm1.responseData.displayName,
        })
      ).not.toBeVisible();

      const termRes = page.waitForResponse('/api/v1/glossaryTerms?*');

      // verify the term is moved under the parent term
      await page.getByTestId('expand-collapse-all-button').click();
      await termRes;

      await expect(
        page.getByRole('cell', {
          name: glossaryTerm1.responseData.displayName,
        })
      ).toBeVisible();
    } finally {
      await glossaryTerm1.delete(apiContext);
      await glossaryTerm2.delete(apiContext);
      await glossary1.delete(apiContext);
      await afterAction();
    }
  });

  test('Assign Glossary Term to entity and check assets', async ({
    browser,
  }) => {
    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const table = new TableClass();
    const glossary1 = new Glossary();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    glossary1.data.terms = [glossaryTerm1];

    try {
      await table.create(apiContext);
      await glossary1.create(apiContext);
      await glossaryTerm1.create(apiContext);
      await table.visitEntityPage(page);
      await assignGlossaryTerm(page, glossaryTerm1.responseData);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);
      await goToAssetsTab(page, glossaryTerm1.data.displayName, 1);
      const entityFqn = get(table, 'entityResponseData.fullyQualifiedName');

      await expect(
        page.getByTestId(`table-data-card_${entityFqn}`)
      ).toBeVisible();
    } finally {
      await table.delete(apiContext);
      await glossaryTerm1.delete(apiContext);
      await glossary1.delete(apiContext);
      await afterAction();
    }
  });

  test('Request description task for Glossary', async ({ browser }) => {
    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const glossary1 = new Glossary();
    const user1 = new UserClass();

    try {
      await user1.create(apiContext);
      await glossary1.create(apiContext);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);

      const value: TaskDetails = {
        term: glossary1.data.name,
        assignee: user1.responseData.name,
      };

      await page.getByTestId('request-description').click();

      await createDescriptionTaskForGlossary(page, value, glossary1);

      const taskResolve = page.waitForResponse('/api/v1/feed/tasks/*/resolve');
      await page.click(
        '.ant-btn-compact-first-item:has-text("Accept Suggestion")'
      );
      await taskResolve;

      await redirectToHomePage(page);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);

      const viewerContainerText = await page.textContent(
        '[data-testid="viewer-container"]'
      );

      await expect(viewerContainerText).toContain('Updated description');
    } finally {
      await user1.delete(apiContext);
      await glossary1.delete(apiContext);
      await afterAction();
    }
  });

  test('Request description task for Glossary Term', async ({ browser }) => {
    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const glossary1 = new Glossary();
    const user1 = new UserClass();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    glossary1.data.terms = [glossaryTerm1];

    try {
      await user1.create(apiContext);
      await glossary1.create(apiContext);
      await glossaryTerm1.create(apiContext);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);
      await selectActiveGlossaryTerm(page, glossaryTerm1.data.displayName);

      const value: TaskDetails = {
        term: glossaryTerm1.data.name,
        assignee: user1.responseData.name,
      };

      await page.getByTestId('request-description').click();

      await createDescriptionTaskForGlossary(page, value, glossaryTerm1, false);

      const taskResolve = page.waitForResponse('/api/v1/feed/tasks/*/resolve');
      await page.click(
        '.ant-btn-compact-first-item:has-text("Accept Suggestion")'
      );
      await taskResolve;

      await redirectToHomePage(page);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);
      await selectActiveGlossaryTerm(page, glossaryTerm1.data.displayName);

      const viewerContainerText = await page.textContent(
        '[data-testid="viewer-container"]'
      );

      await expect(viewerContainerText).toContain('Updated description');
    } finally {
      await user1.delete(apiContext);
      await glossaryTerm1.delete(apiContext);
      await glossary1.delete(apiContext);
      await afterAction();
    }
  });

  test('Request tags for Glossary', async ({ browser }) => {
    test.slow(true);

    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const { page: page1, afterAction: afterActionUser1 } =
      await performUserLogin(browser, user2);

    const glossary1 = new Glossary();
    try {
      await glossary1.create(apiContext);
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);

      const value: TaskDetails = {
        term: glossary1.data.name,
        assignee: user2.responseData.name,
        tag: 'PersonalData.Personal',
      };

      await page.getByTestId('request-entity-tags').click();
      await createTagTaskForGlossary(page, value, glossary1);
      await approveTagsTask(page1, value, glossary1);
    } finally {
      await glossary1.delete(apiContext);
      await afterAction();
    }
  });

  test('Delete Glossary and Glossary Term using Delete Modal', async ({
    browser,
  }) => {
    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const glossary1 = new Glossary();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    glossary1.data.terms = [glossaryTerm1];
    await glossary1.create(apiContext);
    await glossaryTerm1.create(apiContext);
    await sidebarClick(page, SidebarItem.GLOSSARY);
    await selectActiveGlossary(page, glossary1.data.displayName);

    // Delete Glossary Term
    await selectActiveGlossaryTerm(page, glossaryTerm1.data.displayName);
    await deleteGlossaryOrGlossaryTerm(page, glossaryTerm1.data.name, true);

    // Delete Glossary
    await deleteGlossaryOrGlossaryTerm(page, glossary1.data.name);
    await afterAction();
  });

  test('Verify Expand All For Nested Glossary Terms', async ({ browser }) => {
    const { page, afterAction, apiContext } = await performAdminLogin(browser);
    const glossary1 = new Glossary();
    const glossaryTerm1 = new GlossaryTerm(glossary1);
    await glossary1.create(apiContext);
    await glossaryTerm1.create(apiContext);
    const glossaryTerm2 = new GlossaryTerm(
      glossary1,
      glossaryTerm1.responseData.fullyQualifiedName
    );
    await glossaryTerm2.create(apiContext);
    const glossaryTerm3 = new GlossaryTerm(
      glossary1,
      glossaryTerm2.responseData.fullyQualifiedName
    );
    await glossaryTerm3.create(apiContext);

    try {
      await sidebarClick(page, SidebarItem.GLOSSARY);
      await selectActiveGlossary(page, glossary1.data.displayName);
      await selectActiveGlossaryTerm(page, glossaryTerm1.data.displayName);
      await page.getByTestId('terms').click();
      await page.getByTestId('expand-collapse-all-button').click();

      await expect(
        page.getByRole('cell', { name: glossaryTerm2.data.displayName })
      ).toBeVisible();
      await expect(
        page.getByRole('cell', { name: glossaryTerm3.data.displayName })
      ).toBeVisible();
    } finally {
      await glossaryTerm3.delete(apiContext);
      await glossaryTerm2.delete(apiContext);
      await glossaryTerm1.delete(apiContext);
      await glossary1.delete(apiContext);
      await afterAction();
    }
  });

  test.afterAll(async ({ browser }) => {
    const { afterAction, apiContext } = await performAdminLogin(browser);
    await user1.delete(apiContext);
    await user2.delete(apiContext);
    await team.delete(apiContext);
    await afterAction();
  });
});
