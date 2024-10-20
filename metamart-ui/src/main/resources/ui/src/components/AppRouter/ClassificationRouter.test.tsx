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
import { render, screen } from '@testing-library/react';
import React from 'react';
import { MemoryRouter, Route, Switch } from 'react-router-dom';
import ClassificationRouter from './ClassificationRouter';

jest.mock('./AdminProtectedRoute', () => {
  return jest.fn().mockImplementation((props) => <Route {...props} />);
});

jest.mock('../../utils/PermissionsUtils', () => {
  return {
    userPermissions: {
      hasViewPermissions: jest.fn(() => true),
    },
  };
});

jest.mock(
  '../../pages/ClassificationVersionPage/ClassificationVersionPage',
  () => {
    return jest.fn().mockImplementation(() => <>ClassificationVersionPage</>);
  }
);

jest.mock('../../pages/TagsPage/TagsPage', () => {
  return jest.fn().mockImplementation(() => <>TagsPage</>);
});

describe('ClassificationRouter', () => {
  it('should render TagsPage component when route matches "/tags" or "/tags/:tagId"', async () => {
    render(
      <MemoryRouter initialEntries={['/tags']}>
        <Switch>
          <Route path="/tags">
            <ClassificationRouter />
          </Route>
        </Switch>
      </MemoryRouter>
    );

    expect(await screen.findByText('TagsPage')).toBeInTheDocument();
  });

  it('should render ClassificationVersionPage component when route matches "/tags/version"', async () => {
    render(
      <MemoryRouter initialEntries={['/tags/testTag/versions/123']}>
        <Switch>
          <Route path="/tags">
            <ClassificationRouter />
          </Route>
        </Switch>
      </MemoryRouter>
    );

    expect(
      await screen.findByText('ClassificationVersionPage')
    ).toBeInTheDocument();
  });
});
