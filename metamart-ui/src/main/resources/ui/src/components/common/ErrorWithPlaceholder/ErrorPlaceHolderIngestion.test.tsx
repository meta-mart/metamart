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

import {
  getByTestId,
  render,
  screen,
  waitForElementToBeRemoved,
} from '@testing-library/react';
import React from 'react';
import ErrorPlaceHolderIngestion from './ErrorPlaceHolderIngestion';

jest.mock('../AirflowMessageBanner/AirflowMessageBanner', () => {
  return jest
    .fn()
    .mockReturnValue(<div data-testid="airflow-message-banner" />);
});

describe('Test Error placeholder ingestion Component', () => {
  it('Component should render', async () => {
    const { container } = render(<ErrorPlaceHolderIngestion />);

    await waitForElementToBeRemoved(() => screen.getByTestId('loader'));

    expect(getByTestId(container, 'error-steps')).toBeInTheDocument();
  });
});
