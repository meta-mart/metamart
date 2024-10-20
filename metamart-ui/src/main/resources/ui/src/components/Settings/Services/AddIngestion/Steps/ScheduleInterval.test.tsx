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
  findByTestId,
  findByText,
  render,
  screen,
} from '@testing-library/react';
import React from 'react';
import { ScheduleIntervalProps } from '../IngestionWorkflow.interface';
import ScheduleInterval from './ScheduleInterval';

jest.mock('../../../../common/CronEditor/CronEditor', () => {
  return jest.fn().mockImplementation(() => <div>CronEditor.component</div>);
});

const mockScheduleIntervalProps: ScheduleIntervalProps = {
  status: 'initial',
  scheduleInterval: '',
  onBack: jest.fn(),
  onDeploy: jest.fn(),
  submitButtonLabel: 'Add',
  onChange: jest.fn(),
};

describe('Test ScheduleInterval component', () => {
  it('ScheduleInterval component should render', async () => {
    const { container } = render(
      <ScheduleInterval {...mockScheduleIntervalProps} />
    );

    const scheduleIntervelContainer = await findByTestId(
      container,
      'schedule-intervel-container'
    );
    const backButton = await findByTestId(container, 'back-button');
    const deployButton = await findByTestId(container, 'deploy-button');
    const cronEditor = await findByText(container, 'CronEditor.component');

    expect(scheduleIntervelContainer).toBeInTheDocument();
    expect(cronEditor).toBeInTheDocument();
    expect(backButton).toBeInTheDocument();
    expect(deployButton).toBeInTheDocument();
  });

  it('should not render debug log switch when allowEnableDebugLog is false', () => {
    render(<ScheduleInterval {...mockScheduleIntervalProps} />);

    expect(screen.queryByTestId('enable-debug-log')).toBeNull();
  });

  it('should render enable debug log switch when allowEnableDebugLog is true', () => {
    render(
      <ScheduleInterval {...mockScheduleIntervalProps} allowEnableDebugLog />
    );

    expect(screen.getByTestId('enable-debug-log')).toBeInTheDocument();
  });

  it('debug log switch should be initially checked when debugLogInitialValue is true', () => {
    render(
      <ScheduleInterval
        {...mockScheduleIntervalProps}
        allowEnableDebugLog
        debugLogInitialValue
      />
    );

    expect(screen.getByTestId('enable-debug-log')).toHaveClass(
      'ant-switch-checked'
    );
  });

  it('debug log switch should not be initially checked when debugLogInitialValue is false', () => {
    render(
      <ScheduleInterval {...mockScheduleIntervalProps} allowEnableDebugLog />
    );

    expect(screen.getByTestId('enable-debug-log')).not.toHaveClass(
      'ant-switch-checked'
    );
  });
});
