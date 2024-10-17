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
import { render, screen } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import NoProfilerBanner from './NoProfilerBanner.component';

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  Link: jest
    .fn()
    .mockImplementation(({ children, ...props }) => (
      <span {...props}>{children}</span>
    )),
}));

describe('NoProfilerBanner', () => {
  it('should render component', async () => {
    render(<NoProfilerBanner />, { wrapper: MemoryRouter });

    expect(
      await screen.findByTestId('no-profiler-placeholder')
    ).toBeInTheDocument();
    expect(await screen.findByTestId('error-msg')).toBeInTheDocument();
    expect(await screen.findByTestId('documentation-link')).toBeInTheDocument();
  });
});
