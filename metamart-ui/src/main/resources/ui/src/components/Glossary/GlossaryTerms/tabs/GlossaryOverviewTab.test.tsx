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
import { act, render } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import {
  MOCKED_GLOSSARY_TERMS,
  MOCK_PERMISSIONS,
} from '../../../../mocks/Glossary.mock';
import GlossaryOverviewTab from './GlossaryOverviewTab.component';

jest.mock('./GlossaryTermSynonyms', () => {
  return jest.fn().mockReturnValue(<p>GlossaryTermSynonyms</p>);
});
jest.mock('./RelatedTerms', () => {
  return jest.fn().mockReturnValue(<p>RelatedTerms</p>);
});
jest.mock('./GlossaryTermReferences', () => {
  return jest.fn().mockReturnValue(<p>GlossaryTermReferences</p>);
});

jest.mock('../../../common/EntityDescription/DescriptionV1', () => {
  return jest.fn().mockReturnValue(<p>Description</p>);
});

jest.mock(
  '../../GlossaryDetailsRightPanel/GlossaryDetailsRightPanel.component',
  () => {
    return jest.fn().mockImplementation(() => <>testGlossaryRightPanel</>);
  }
);

jest.mock('../../../common/ResizablePanels/ResizablePanels', () => {
  return jest.fn().mockImplementation(({ firstPanel, secondPanel }) => (
    <div>
      {firstPanel.children} <div>{secondPanel.children}</div>
    </div>
  ));
});

const onUpdate = jest.fn();

describe('GlossaryOverviewTab', () => {
  const selectedData = MOCKED_GLOSSARY_TERMS[0];
  const permissions = MOCK_PERMISSIONS;

  it('renders the component', async () => {
    const { findByText } = render(
      <GlossaryOverviewTab
        editCustomAttributePermission
        isGlossary={false}
        permissions={permissions}
        selectedData={selectedData}
        onExtensionUpdate={jest.fn()}
        onThreadLinkSelect={jest.fn()}
        onUpdate={onUpdate}
      />,
      { wrapper: MemoryRouter }
    );

    await act(async () => {
      const description = await findByText(/Description/i);
      const synonymsContainer = await findByText(/GlossaryTermSynonyms/i);
      const relatedTermsContainer = await findByText(/RelatedTerms/i);
      const referencesContainer = await findByText(/GlossaryTermReferences/i);

      expect(description).toBeInTheDocument();
      expect(synonymsContainer).toBeInTheDocument();
      expect(relatedTermsContainer).toBeInTheDocument();
      expect(referencesContainer).toBeInTheDocument();
    });
  });
});
