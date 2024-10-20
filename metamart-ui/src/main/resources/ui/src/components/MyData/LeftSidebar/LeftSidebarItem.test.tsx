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
import { fireEvent, render, screen } from '@testing-library/react';
import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import {
  LOGOUT_ITEM,
  SETTING_ITEM,
} from '../../../constants/LeftSidebar.constants';
import LeftSidebarItem from './LeftSidebarItem.component';

const mockOnClick = jest.fn();

describe('LeftSidebar Items', () => {
  it('should renders sidebar items data', () => {
    render(
      <BrowserRouter>
        <LeftSidebarItem data={SETTING_ITEM} />
      </BrowserRouter>
    );

    expect(screen.getByTestId('app-bar-item-settings')).toBeInTheDocument();

    expect(screen.getByText('label.setting-plural')).toBeInTheDocument();

    expect(screen.getByRole('img')).toBeInTheDocument();
  });

  it('should renders sidebar items with redirect url', () => {
    render(
      <BrowserRouter>
        <LeftSidebarItem data={SETTING_ITEM} />
      </BrowserRouter>
    );

    expect(screen.getByTestId('app-bar-item-settings')).toBeInTheDocument();

    expect(screen.getByText('label.setting-plural')).toBeInTheDocument();

    expect(screen.getByRole('link')).toBeInTheDocument();

    expect(screen.getByRole('img')).toBeInTheDocument();
  });

  it('should renders sidebar items without redirect url', () => {
    render(
      <BrowserRouter>
        <LeftSidebarItem data={LOGOUT_ITEM} />
      </BrowserRouter>
    );

    expect(screen.getByTestId('app-bar-item-logout')).toBeInTheDocument();

    expect(screen.getByText('label.logout')).toBeInTheDocument();

    expect(screen.queryByRole('link')).not.toBeInTheDocument();

    expect(screen.getByRole('img')).toBeInTheDocument();
  });

  it('should fire onClick event if provided', () => {
    render(
      <BrowserRouter>
        <LeftSidebarItem data={{ ...LOGOUT_ITEM, onClick: mockOnClick }} />
      </BrowserRouter>
    );

    const logoutItem = screen.getByTestId('app-bar-item-logout');

    expect(logoutItem).toBeInTheDocument();

    fireEvent.click(logoutItem);

    expect(mockOnClick).toHaveBeenCalled();
  });
});
