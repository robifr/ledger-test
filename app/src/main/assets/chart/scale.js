/**
 * Copyright (c) 2024 Robi
 *
 * Ledger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ledger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ledger. If not, see <https://www.gnu.org/licenses/>.
 */

"use strict";

import * as d3 from "../libs/d3.js";

/**
 * @typedef {import("./chart.js").ChartLayout} ChartLayout
 */

/**
 * @typedef {Object} AxisPosition
 * @property {number} value
 * @property {(scale: d3.scaleLinear | d3.scaleBand) => d3.axisBottom | d3.axisLeft} axis
 * @property {(layout: ChartLayout) => number} minRange
 * @property {(layout: ChartLayout) => number} maxRange
 */

/**
 * @type {Readonly<{BOTTOM: AxisPosition, LEFT: AxisPosition}>}
 */
export const AxisPosition = Object.freeze({
  BOTTOM: {
    value: 1,
    axis: (scale) => d3.axisBottom(scale),
    minRange: (layout) => layout.marginLeft,
    maxRange: (layout) => layout.width - layout.marginRight,
  },

  LEFT: {
    value: 2,
    axis: (scale) => d3.axisLeft(scale),
    minRange: (layout) => layout.height - layout.marginBottom,
    maxRange: (layout) => layout.marginTop,
  },
});

/**
 * @param {ChartLayout} layout
 * @param {number} axisPosition
 * @param {number[]} domain
 * @param {boolean} [isAllLabelVisible=true]
 * @returns {{scale: d3.scaleLinear, axis: d3.axisBottom | d3.axisLeft }}
 */
export function createLinearScale(layout, axisPosition, domain, isAllLabelVisible = true) {
  // Just a hack to prevent label on the edge of axis.
  domain[0] = domain[0] !== 0 ? domain[0] - 0.5 : domain[0];
  domain[domain.length - 1] = domain[domain.length - 1] + 0.5;

  const axisPos = _axisPositionOf(axisPosition);
  const scale = d3
    .scaleLinear()
    .domain(domain)
    .range([axisPos.minRange(layout), axisPos.maxRange(layout)]);
  const axis = axisPos
    .axis(scale)
    .tickSizeOuter(0)
    .tickFormat((d) => {
      // Hide some label to improve readability.
      if (!isAllLabelVisible && _gapBetweenTicks(scale.ticks().length) !== 0) return "";
      // Hide label for decimal numbers.
      if (Math.floor(d) !== d) return "";
      // Format with unit.
      if (d >= 1e9) return d3.format("d")(d / 1e9) + "b";
      if (d >= 1e6) return d3.format("d")(d / 1e6) + "m";
      if (d >= 1000) return d3.format("d")(d / 1000) + "k";
      return d3.format("d")(d);
    });

  return {
    scale: scale,
    axis: axis,
  };
}

/**
 * Same as a linear scale but with support for larger numbers by using percentages.
 * Each domain (0-100) will be replaced with the provided domain strings.
 * @param {ChartLayout} layout
 * @param {number} axisPosition
 * @param {string[]} domain
 * @returns {{scale: d3.scaleLinear, axis: d3.axisBottom | d3.axisLeft }}
 */
export function createPercentageLinearScale(layout, axisPosition, domain) {
  if (domain.length !== 101) throw new Error("Domain size should contain 101 items");

  const axisPos = _axisPositionOf(axisPosition);
  const scale = d3
    .scaleLinear()
    .domain([0, 100])
    .range([axisPos.minRange(layout), axisPos.maxRange(layout)]);
  const axis = axisPos
    .axis(scale)
    .ticks(5)
    .tickSizeOuter(0)
    .tickSize(-layout.width + layout.marginRight + layout.marginLeft)
    .tickFormat((d, i) => {
      if (Math.floor(d) !== d) return ""; // Hide label for decimal numbers.
      return domain[i * 2 * (scale.ticks().length - 1)];
    });

  return { scale: scale, axis: axis };
}

/**
 * @param {ChartLayout} layout
 * @param {number} axisPosition
 * @param {string[]} domain
 * @param {boolean} [isAllLabelVisible=true]
 * @returns {{scale: d3.scaleBand, axis: d3.axisBottom | d3.axisLeft }}
 */
export function createBandScale(layout, axisPosition, domain, isAllLabelVisible = true) {
  const axisPos = _axisPositionOf(axisPosition);
  const scale = d3
    .scaleBand()
    .domain(domain)
    .range([axisPos.minRange(layout), axisPos.maxRange(layout)])
    .padding(0.4);
  const axis = axisPos
    .axis(scale)
    .tickSizeOuter(0)
    // Hide some label to improve readability.
    .tickFormat((d, i) =>
      !isAllLabelVisible && i % _gapBetweenTicks(domain.length) !== 0 ? null : d
    );

  return { scale, axis };
}

/**
 * @param {number} totalTicks
 * @returns {number}
 */
function _gapBetweenTicks(totalTicks) {
  return Math.max(1, Math.ceil(totalTicks / 8));
}

/**
 * @param {number} value
 * @returns {AxisPosition}
 * @throws {TypeError}
 */
function _axisPositionOf(value) {
  const axisPosition = Object.values(AxisPosition).find((pos) => pos.value === value) ?? null;
  if (!axisPosition) throw new TypeError(`Invalid AxisPosition value: ${value}`);
  return axisPosition;
}
