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
import ApplicationCard from './ApplicationCard.component';

const props = {
  title: 'Search Index',
  description: 'Hello World',
  linkTitle: 'Show More',
  onClick: jest.fn(),
  appName: 'Search Index',
  showDescription: true,
};

describe('ApplicationCard', () => {
  it('renders the title correctly', () => {
    render(<ApplicationCard {...props} />);

    expect(screen.getByText('Search Index')).toBeInTheDocument();
    expect(screen.getByText('Hello World')).toBeInTheDocument();
  });

  it('does not render the description when showDescription is false', () => {
    render(<ApplicationCard {...props} showDescription={false} />);

    expect(screen.queryByText('Hello World')).toBeNull();
  });

  it('calls onClick when the link button is clicked', () => {
    const onClick = jest.fn();
    render(<ApplicationCard {...props} onClick={onClick} />);
    fireEvent.click(screen.getByTestId('config-btn'));

    expect(onClick).toHaveBeenCalledTimes(1);
  });
});
