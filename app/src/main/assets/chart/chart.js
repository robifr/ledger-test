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
import { Android } from "./typedefs.js";

/**
 * @typedef {ReturnType<typeof import("./scale.js").createLinearScale>} linearScale
 * @typedef {ReturnType<typeof import("./scale.js").createPercentageLinearScale>} percentageLinearScale
 * @typedef {ReturnType<typeof import("./scale.js").createBandScale>} bandScale
 */

export class ChartLayout {
  /**
   * @param {number} width
   * @param {number} height
   * @param {number} marginTop
   * @param {number} marginBottom
   * @param {number} marginLeft
   * @param {number} marginRight
   * @param {number} fontSize
   * @param {string} backgroundColor
   */
  constructor(
    width,
    height,
    marginTop,
    marginBottom,
    marginLeft,
    marginRight,
    fontSize,
    backgroundColor
  ) {
    /** @type {number} */
    this.width = width;
    // There's a bug where a vertical scroll will appear no matter what. Subtracting by
    // the font width is the most reliable solution for every user with different font sizes.
    /** @type {number} */
    this.height = height - fontSize;

    // Extra margin for the ticks.
    /** @type {number} */
    this.marginTop = marginTop + 10;
    /** @type {number} */
    this.marginBottom = marginBottom + 10;
    /** @type {number} */
    this.marginLeft = marginLeft + 10;
    /** @type {number} */
    this.marginRight = marginRight + 10;
    /** @type {number} */
    this.fontSize = fontSize;
    /** @type {string} */
    this.backgroundColor = backgroundColor;

    Object.freeze(this);
    Object.seal(this);
  }
}

/**
 * @param {ChartLayout} layout
 * @param {bandScale} xScale
 * @param {linearScale | percentageLinearScale} yScale
 * @param {[{ key: string, value: number }]} data
 */
export function renderBarChart(layout, xScale, yScale, data) {
  // Set the corner radius as 20% of the bar width.
  const barCornerRadius = Math.min(
    5,
    ((layout.width - layout.marginLeft - layout.marginRight) / data.length) * 0.2
  );

  d3.select("#container").select("svg").remove();
  const svg = d3.create("svg").attr("width", layout.width).attr("height", layout.height);

  // Draw y-axis.
  svg
    .append("g")
    .attr("transform", `translate(${layout.marginLeft}, 0)`)
    .style("font-size", `${layout.fontSize}`)
    .call(yScale.axis)
    .call((g) => g.select(".domain").remove()) // Remove y-axis line.
    .selectAll("line")
    .style("stroke", Android.colorHex("stroke"))
    .attr("stroke-width", 0.3);

  // Draw bar with rounded corners.
  svg
    .selectAll(".bar-top")
    .data(data)
    .enter()
    .append("rect")
    .style("fill", Android.colorHex("colorPrimary"))
    .attr("x", (d) => xScale.scale(d.key) ?? null)
    .attr("y", (d) => yScale.scale(d.value))
    .attr("width", xScale.scale.bandwidth())
    .attr("height", (d) => layout.height - layout.marginBottom - yScale.scale(d.value))
    .attr("rx", barCornerRadius)
    .attr("ry", barCornerRadius);
  // Draw bottom bar to cover the bottom corners.
  svg
    .selectAll(".bar-bottom")
    .data(data)
    .enter()
    .append("rect")
    .style("fill", Android.colorHex("colorPrimary"))
    .attr("x", (d) => xScale.scale(d.key) ?? null)
    .attr(
      "y",
      (d) => yScale.scale(d.value) + (yScale.scale(0) - yScale.scale(d.value) - barCornerRadius)
    )
    .attr("width", xScale.scale.bandwidth())
    .attr("height", (d) =>
      layout.height - layout.marginBottom - yScale.scale(d.value) > barCornerRadius
        ? barCornerRadius
        : 0
    );

  // Draw x-axis. Ensure that the x-axis is rendered after the bars to prevent overlapping.
  svg
    .append("g")
    .attr("transform", `translate(0, ${layout.height - layout.marginBottom})`)
    .style("font-size", `${layout.fontSize}`)
    .call(xScale.axis);

  d3.select("#container").append(() => svg.node());
  d3.select("body").style("background-color", layout.backgroundColor);
}
