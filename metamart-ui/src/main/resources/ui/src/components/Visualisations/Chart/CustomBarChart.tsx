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

import { Col, Row } from 'antd';
import React, { useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  LegendProps,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { GRAPH_BACKGROUND_COLOR } from '../../../constants/constants';
import {
  axisTickFormatter,
  tooltipFormatter,
  updateActiveChartFilter,
} from '../../../utils/ChartUtils';
import { CustomTooltip } from '../../../utils/DataInsightUtils';
import { formatDateTime } from '../../../utils/date-time/DateTimeUtils';
import ErrorPlaceHolder from '../../common/ErrorWithPlaceholder/ErrorPlaceHolder';
import { CustomBarChartProps } from './Chart.interface';

const CustomBarChart = ({
  chartCollection,
  tickFormatter,
  name,
}: CustomBarChartProps) => {
  const { data, information } = chartCollection;
  const [activeKeys, setActiveKeys] = useState<string[]>([]);

  if (data.length === 0) {
    return (
      <Row align="middle" className="h-full w-full" justify="center">
        <Col>
          <ErrorPlaceHolder className="mt-0-important" />
        </Col>
      </Row>
    );
  }
  const handleClick: LegendProps['onClick'] = (event) => {
    setActiveKeys((prevActiveKeys) =>
      updateActiveChartFilter(event.dataKey, prevActiveKeys)
    );
  };

  return (
    <ResponsiveContainer
      className="custom-legend"
      debounce={200}
      id={`${name}_graph`}
      minHeight={300}>
      <BarChart className="w-full" data={data} margin={{ left: 16 }}>
        <CartesianGrid stroke={GRAPH_BACKGROUND_COLOR} />
        <XAxis
          dataKey="name"
          padding={{ left: 16, right: 16 }}
          tick={{ fontSize: 12 }}
        />

        <YAxis
          allowDataOverflow
          padding={{ top: 16, bottom: 16 }}
          tick={{ fontSize: 12 }}
          tickFormatter={(props) => axisTickFormatter(props, tickFormatter)}
        />
        <Tooltip
          content={
            <CustomTooltip
              dateTimeFormatter={formatDateTime}
              timeStampKey="timestamp"
              valueFormatter={(value) => tooltipFormatter(value, tickFormatter)}
            />
          }
        />
        {information.map((info) => (
          <Bar
            dataKey={info.dataKey}
            fill={info.color}
            hide={
              activeKeys.length ? !activeKeys.includes(info.dataKey) : false
            }
            key={info.dataKey}
            name={info.title}
            stackId={info.stackId}
          />
        ))}
        <Legend onClick={handleClick} />
      </BarChart>
    </ResponsiveContainer>
  );
};

export default CustomBarChart;
