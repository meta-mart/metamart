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

import { act, render, screen } from '@testing-library/react';

import React from 'react';

import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import {
  ingestionDataName,
  mockPipelineActionsProps,
} from '../../../../../../mocks/IngestionListTable.mock';
import { DEFAULT_ENTITY_PERMISSION } from '../../../../../../utils/PermissionsUtils';
import PipelineActions from './PipelineActions';

const mockPush = jest.fn();

jest.mock('react-router-dom', () => ({
  useHistory: jest.fn().mockImplementation(() => ({
    push: mockPush,
  })),
}));

jest.mock('./PipelineActionsDropdown', () =>
  jest.fn().mockImplementation(() => <div>PipelineActionsDropdown</div>)
);

describe('PipelineAction', () => {
  it('should render PipelineActionsDropdown if only editAll permission is present', async () => {
    await act(async () => {
      render(
        <PipelineActions
          {...mockPipelineActionsProps}
          ingestionPipelinePermissions={{
            [ingestionDataName]: {
              ...DEFAULT_ENTITY_PERMISSION,
              EditAll: true,
            },
          }}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.getByText('PipelineActionsDropdown')).toBeInTheDocument();
  });

  it('should render PipelineActionsDropdown if only delete permission is present', async () => {
    await act(async () => {
      render(
        <PipelineActions
          {...mockPipelineActionsProps}
          ingestionPipelinePermissions={{
            [ingestionDataName]: {
              ...DEFAULT_ENTITY_PERMISSION,
              Delete: true,
            },
          }}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.getByText('PipelineActionsDropdown')).toBeInTheDocument();
  });

  it('should not render PipelineActionsDropdown if both EditAll and delete permission is not present', async () => {
    await act(async () => {
      render(
        <PipelineActions
          {...mockPipelineActionsProps}
          ingestionPipelinePermissions={{
            [ingestionDataName]: DEFAULT_ENTITY_PERMISSION,
          }}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.queryByText('PipelineActionsDropdown')).toBeNull();
  });

  it('should not render pause or resume button if editStatus permission is not present', async () => {
    await act(async () => {
      render(
        <PipelineActions
          {...mockPipelineActionsProps}
          ingestionPipelinePermissions={{
            [ingestionDataName]: {
              ...DEFAULT_ENTITY_PERMISSION,
            },
          }}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.queryByText('label.pause')).toBeNull();
    expect(screen.queryByText('label.resume')).toBeNull();
  });

  it('should render pause or resume button if editStatus permission is present', async () => {
    await act(async () => {
      render(
        <PipelineActions
          {...mockPipelineActionsProps}
          ingestionPipelinePermissions={{
            [ingestionDataName]: {
              ...DEFAULT_ENTITY_PERMISSION,
              EditIngestionPipelineStatus: true,
            },
          }}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.getByText('label.resume')).toBeInTheDocument();
  });

  it('should render pause button if pipeline is enabled', async () => {
    await act(async () => {
      render(
        <PipelineActions
          {...mockPipelineActionsProps}
          pipeline={{ ...mockPipelineActionsProps.pipeline, enabled: true }}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.getByText('label.pause')).toBeInTheDocument();
  });

  it('should redirect to logs page when clicked on logs button', async () => {
    await act(async () => {
      render(<PipelineActions {...mockPipelineActionsProps} />, {
        wrapper: MemoryRouter,
      });
    });

    const logsButton = screen.getByText('label.log-plural');

    await act(async () => {
      userEvent.click(logsButton);
    });

    expect(mockPush).toHaveBeenCalledWith(
      '/searchServices/MetaMart.MetaMart_elasticSearchReIndex/logs'
    );
  });

  it('should call handleEnableDisableIngestion when clicked on pause or resume click', async () => {
    await act(async () => {
      render(<PipelineActions {...mockPipelineActionsProps} />, {
        wrapper: MemoryRouter,
      });
    });

    const resumeButton = screen.getByText('label.resume');

    await act(async () => {
      userEvent.click(resumeButton);
    });

    expect(
      mockPipelineActionsProps.handleEnableDisableIngestion
    ).toHaveBeenCalledWith(mockPipelineActionsProps.pipeline.id);
  });
});
