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

import { findByTestId, render } from '@testing-library/react';
import React from 'react';
import { ServiceCategory } from '../../../../enums/service.enum';
import AddService from './AddService.component';

jest.mock('react-router-dom', () => ({
  useHistory: jest.fn(),
}));

jest.mock('../AddIngestion/AddIngestion.component', () => () => (
  <>AddIngestion</>
));

jest.mock(
  '../../../common/TitleBreadcrumb/TitleBreadcrumb.component',
  () => () => <>TitleBreadcrumb.component</>
);

jest.mock('../ServiceConfig/ConnectionConfigForm', () => () => (
  <>ConnectionConfigForm</>
));

jest.mock('../Ingestion/IngestionStepper/IngestionStepper.component', () => {
  return jest.fn().mockImplementation(() => <div>IngestionStepper</div>);
});

jest.mock('../../../common/ServiceDocPanel/ServiceDocPanel', () => {
  return jest.fn().mockReturnValue(<div>ServiceDocPanel</div>);
});

jest.mock('../../..//common/ResizablePanels/ResizablePanels', () =>
  jest.fn().mockImplementation(({ firstPanel, secondPanel }) => (
    <>
      <div>{firstPanel.children}</div>
      <div>{secondPanel.children}</div>
    </>
  ))
);

describe('Test AddService component', () => {
  it('AddService component should render', async () => {
    const { container } = render(
      <AddService
        addIngestion={false}
        handleAddIngestion={jest.fn()}
        ingestionAction="Creating"
        ingestionProgress={0}
        isIngestionCreated={false}
        isIngestionDeployed={false}
        newServiceData={undefined}
        serviceCategory={ServiceCategory.DASHBOARD_SERVICES}
        slashedBreadcrumb={[
          {
            name: 'breadcrumb',
            url: '',
          },
        ]}
        onAddIngestionSave={jest.fn()}
        onAddServiceSave={jest.fn()}
      />
    );

    const addNewServiceContainer = await findByTestId(
      container,
      'add-new-service-container'
    );
    const header = await findByTestId(container, 'header');

    expect(addNewServiceContainer).toBeInTheDocument();

    expect(header).toBeInTheDocument();
  });
});
