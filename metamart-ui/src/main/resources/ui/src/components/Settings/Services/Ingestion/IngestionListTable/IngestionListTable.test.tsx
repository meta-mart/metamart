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
import userEvent from '@testing-library/user-event';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { mockIngestionListTableProps } from '../../../../../mocks/IngestionListTable.mock';
import { ENTITY_PERMISSIONS } from '../../../../../mocks/Permissions.mock';
import { deleteIngestionPipelineById } from '../../../../../rest/ingestionPipelineAPI';
import IngestionListTable from './IngestionListTable';

jest.mock('../../../../../hooks/useApplicationStore', () => ({
  useApplicationStore: jest.fn(() => ({
    theme: { primaryColor: '#fff' },
  })),
}));

jest.mock(
  '../../../../../context/PermissionProvider/PermissionProvider',
  () => ({
    usePermissionProvider: jest.fn().mockImplementation(() => ({
      getEntityPermissionByFqn: jest
        .fn()
        .mockImplementation(() => Promise.resolve(ENTITY_PERMISSIONS)),
    })),
  })
);

jest.mock('../../../../../rest/ingestionPipelineAPI', () => ({
  deleteIngestionPipelineById: jest
    .fn()
    .mockImplementation(() => Promise.resolve()),
}));

jest.mock('../../../../../utils/IngestionUtils', () => ({
  getErrorPlaceHolder: jest.fn().mockImplementation(() => 'ErrorPlaceholder'),
}));

jest.mock('./PipelineActions/PipelineActions', () =>
  jest.fn().mockImplementation(({ handleDeleteSelection }) => (
    <div>
      PipelineActions
      <button
        onClick={handleDeleteSelection({
          id: 'id',
          name: 'name',
          state: 'waiting',
        })}>
        handleDeleteSelection
      </button>
    </div>
  ))
);

jest.mock('../../../../Modals/EntityDeleteModal/EntityDeleteModal', () =>
  jest
    .fn()
    .mockImplementation(({ onConfirm }) => (
      <button onClick={onConfirm}>EntityDeleteModal</button>
    ))
);

jest.mock('../IngestionRecentRun/IngestionRecentRuns.component', () => ({
  IngestionRecentRuns: jest
    .fn()
    .mockImplementation(() => <div>IngestionRecentRuns</div>),
}));

jest.mock(
  '../../../../common/Skeleton/CommonSkeletons/ControlElements/ControlElements.component',
  () => jest.fn().mockImplementation(() => <div>ButtonSkeleton</div>)
);

jest.mock('../../../../common/RichTextEditor/RichTextEditorPreviewer', () =>
  jest.fn().mockImplementation(() => <div>RichTextEditorPreviewer</div>)
);

jest.mock('../../../../common/NextPrevious/NextPrevious', () =>
  jest.fn().mockImplementation(() => <div>NextPrevious</div>)
);

describe('Ingestion', () => {
  it('should render custom emptyPlaceholder if passed', async () => {
    await act(async () => {
      render(
        <IngestionListTable
          {...mockIngestionListTableProps}
          emptyPlaceholder="customErrorPlaceholder"
          ingestionData={[]}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.getByText('customErrorPlaceholder')).toBeInTheDocument();
  });

  it('should render default emptyPlaceholder if not passed customErrorPlaceholder', async () => {
    await act(async () => {
      render(
        <IngestionListTable
          {...mockIngestionListTableProps}
          ingestionData={[]}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.getByText('ErrorPlaceholder')).toBeInTheDocument();
  });

  it('should not show the description column if showDescriptionCol is false', async () => {
    await act(async () => {
      render(
        <IngestionListTable
          {...mockIngestionListTableProps}
          showDescriptionCol={false}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.queryByText('label.description')).toBeNull();
  });

  it('should show the description column if showDescriptionCol is true', async () => {
    await act(async () => {
      render(
        <IngestionListTable
          {...mockIngestionListTableProps}
          showDescriptionCol
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.getByText('label.description')).toBeInTheDocument();
  });

  it('should replace the type column with custom type column if passed as prop', async () => {
    await act(async () => {
      render(
        <IngestionListTable
          {...mockIngestionListTableProps}
          pipelineTypeColumnObj={[]}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.queryByText('label.type')).toBeNull();
  });

  it('should not show the actions column when enableActions is false', async () => {
    await act(async () => {
      render(
        <IngestionListTable
          {...mockIngestionListTableProps}
          enableActions={false}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.queryByText('label.action-plural')).toBeNull();
  });

  it('should not show NextPrevious if ingestionPagingInfo is not present', async () => {
    await act(async () => {
      render(
        <IngestionListTable
          {...mockIngestionListTableProps}
          ingestionPagingInfo={undefined}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.queryByText('NextPrevious')).toBeNull();
  });

  it('should not show NextPrevious if onPageChange is not present', async () => {
    await act(async () => {
      render(
        <IngestionListTable
          {...mockIngestionListTableProps}
          onPageChange={undefined}
        />,
        {
          wrapper: MemoryRouter,
        }
      );
    });

    expect(screen.queryByText('NextPrevious')).toBeNull();
  });

  it('should show NextPrevious if onPageChange and ingestionPagingInfo is present', async () => {
    await act(async () => {
      render(<IngestionListTable {...mockIngestionListTableProps} />, {
        wrapper: MemoryRouter,
      });
    });

    expect(screen.getByText('NextPrevious')).toBeInTheDocument();
  });

  it('should call deleteIngestionPipelineById on confirm click', async () => {
    await act(async () => {
      render(<IngestionListTable {...mockIngestionListTableProps} />, {
        wrapper: MemoryRouter,
      });
    });

    const deleteSelection = screen.getByText('handleDeleteSelection');

    await act(async () => {
      userEvent.click(deleteSelection);
    });

    const confirmButton = screen.getByText('EntityDeleteModal');

    await act(async () => {
      userEvent.click(confirmButton);
    });

    expect(deleteIngestionPipelineById).toHaveBeenCalledWith('id');
  });
});
