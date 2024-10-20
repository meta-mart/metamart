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
import { SettingType } from '../../generated/settings/settings';
import { getSettingsConfigFromConfigType } from '../../rest/settingConfigAPI';
import ProfilerConfigurationPage from './ProfilerConfigurationPage';

const mockHistory = {
  goBack: jest.fn(),
};

jest.mock('../../components/common/Loader/Loader', () =>
  jest.fn().mockReturnValue(<div>Loading...</div>)
);
jest.mock(
  '../../components/common/TitleBreadcrumb/TitleBreadcrumb.component',
  () => jest.fn().mockReturnValue(<div>TitleBreadcrumb.component</div>)
);
jest.mock('../../components/PageHeader/PageHeader.component', () =>
  jest.fn().mockReturnValue(<div>PageHeader.component</div>)
);
jest.mock('react-router-dom', () => ({
  useHistory: jest.fn().mockImplementation(() => mockHistory),
}));
jest.mock('../../rest/settingConfigAPI', () => ({
  getSettingsConfigFromConfigType: jest.fn().mockResolvedValue({}),
  updateSettingsConfig: jest.fn(),
}));
jest.mock('../../utils/GlobalSettingsUtils', () => ({
  getSettingPageEntityBreadCrumb: jest.fn().mockReturnValue([]),
}));
jest.mock('../../components/PageLayoutV1/PageLayoutV1', () =>
  jest.fn().mockImplementation(({ children }) => <div>{children}</div>)
);
jest.mock('../../constants/profiler.constant', () => ({
  DEFAULT_PROFILER_CONFIG_VALUE: {
    metricConfiguration: [
      {
        dataType: undefined,
        metrics: undefined,
        disabled: false,
      },
    ],
  },
  PROFILER_METRICS_TYPE_OPTIONS: [],
}));

describe('ProfilerConfigurationPage', () => {
  beforeEach(() => {
    act(() => {
      render(<ProfilerConfigurationPage />);
    });
  });

  it('renders the page correctly', async () => {
    expect(
      await screen.findByText('TitleBreadcrumb.component')
    ).toBeInTheDocument();
    expect(await screen.findByText('PageHeader.component')).toBeInTheDocument();
    expect(await screen.findByText('label.data-type')).toBeInTheDocument();
    expect(await screen.findByText('label.disable')).toBeInTheDocument();
    expect(await screen.findByText('label.metric-type')).toBeInTheDocument();
    expect(
      await screen.findByTestId('profiler-config-form')
    ).toBeInTheDocument();
    expect(await screen.findByTestId('data-type-select')).toBeInTheDocument();
    expect(await screen.findByTestId('metric-type-select')).toBeInTheDocument();
    expect(await screen.findByTestId('disabled-switch')).toBeInTheDocument();
    expect(await screen.findByTestId('add-fields')).toBeInTheDocument();
    expect(await screen.findByTestId('cancel-button')).toBeInTheDocument();
    expect(await screen.findByTestId('save-button')).toBeInTheDocument();
  });

  it('should fetch the profiler config data on initial render', () => {
    const mockGetSettingsConfigFromConfigType =
      getSettingsConfigFromConfigType as jest.Mock;

    expect(mockGetSettingsConfigFromConfigType).toHaveBeenCalledWith(
      SettingType.ProfilerConfiguration
    );
    expect(mockGetSettingsConfigFromConfigType).toHaveBeenCalledTimes(1);
  });

  it("onCancel should call history's goBack method", () => {
    const cancelButton = screen.getByTestId('cancel-button');
    act(() => {
      cancelButton.click();
    });

    expect(mockHistory.goBack).toHaveBeenCalledTimes(1);
  });
});
