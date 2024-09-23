/**
 * Copyright 2024 Robi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";

import { ChartLayout, renderBarChart, renderStackedBarChart, renderDonutChart } from "./chart.js";
import { createLinearScale, createPercentageLinearScale, createBandScale } from "./scale.js";

// Web view only works in global scope.
// @ts-ignore
window.chart = {
  ChartLayout,
  renderBarChart,
  renderStackedBarChart,
  renderDonutChart,
  createLinearScale,
  createPercentageLinearScale,
  createBandScale,
};
