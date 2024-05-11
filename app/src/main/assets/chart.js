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

class ChartLayout {
  /**
   * @param {number} width
   * @param {number} height
   * @param {number} marginTop
   * @param {number} marginBottom
   * @param {number} marginLeft
   * @param {number} marginRight
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
    this.marginTop = marginTop + 6;
    /** @type {number} */
    this.marginBottom = marginBottom + 6;
    /** @type {number} */
    this.marginLeft = marginLeft + 6;
    /** @type {number} */
    this.marginRight = marginRight + 6;
    /** @type {number} */
    this.fontSize = fontSize;
    /** @type {colorHex} */
    this.backgroundColor = backgroundColor;

    Object.freeze(this);
    Object.seal(this);
  }
}

/** @abstract */
class ChartAxis {
  static BOTTOM_POSITION = 1;
  static LEFT_POSITION = 2;

  /**
   * @param {function(): *} scale
   * @param {function(): *} axis
   * @param {number} position
   */
  constructor(scale, axis, position) {
    if (this.constructor === ChartAxis) {
      throw new Error(`Can't instantiate abstract class ${this.constructor.name}`);
    }

    /** @type {function(): *} */
    this.scale = scale;
    /** @type {function(): *} */
    this.axis = axis;
    /** @type {number} */
    this.position = position;

    Object.freeze(this);
    Object.seal(this);
  }

  /**
   * @param {ChartLayout} layout
   * @param {number} axisPosition
   * @returns {number[]}
   */
  static #_withRange(layout, axisPosition) {
    const minRange = (() => {
      switch (axisPosition) {
        case this.BOTTOM_POSITION:
          return layout.marginLeft;

        case this.LEFT_POSITION:
        default:
          return layout.height - layout.marginBottom;
      }
    })();

    const maxRange = (() => {
      switch (axisPosition) {
        case this.BOTTOM_POSITION:
          return layout.width - layout.marginRight;

        case this.LEFT_POSITION:
        default:
          return layout.marginTop;
      }
    })();

    return [minRange, maxRange];
  }

  /**
   * @param {ChartLayout} layout
   * @param {number} axisPosition
   * @param {number[]} domain
   * @returns {ChartLinearAxis}
   */
  static withLinearScale(layout, axisPosition, domain, isEvenLabelIndexVisible = true) {
    // Just a hack to prevent label on the edge of axis.
    const minDomain = domain[0] !== 0 ? domain[0] - 0.5 : domain[0];
    const maxDomain = domain[1] + 0.5;

    const scale = d3
      .scaleLinear()
      .domain([minDomain, maxDomain])
      .range(this.#_withRange(layout, axisPosition));
    const axis = (() => {
      switch (axisPosition) {
        case this.BOTTOM_POSITION:
          return d3.axisBottom(scale);

        case this.LEFT_POSITION:
        default:
          return d3.axisLeft(scale);
      }
    })()
      .tickSizeOuter(0)
      .tickFormat((d, i) => {
        if (i % 2 !== 0 && !isEvenLabelIndexVisible) return; // Hide label for even-index.
        if (Math.floor(d) !== d) return; // Hide label for decimal numbers.
        if (d >= 1e9) return d3.format("d")(d) / 1e9 + "b";
        if (d >= 1e6) return d3.format("d")(d) / 1e6 + "m";
        if (d >= 1000) return d3.format("d")(d) / 1000 + "k";
        return d3.format("d")(d);
      });

    return new ChartLinearAxis(scale, axis);
  }

  /**
   * @param {ChartLayout} layout
   * @param {number} axisPosition
   * @param {string[]} domain
   * @param {boolean} isEvenLabelIndexVisible
   * @returns {ChartBandAxis}
   */
  static withBandScale(layout, axisPosition, domain, isEvenLabelIndexVisible = true) {
    const scale = d3
      .scaleBand()
      .domain(domain)
      .range(this.#_withRange(layout, axisPosition))
      .padding(0.4);
    const axis = (() => {
      switch (axisPosition) {
        case this.BOTTOM_POSITION:
          return d3.axisBottom(scale);

        case this.LEFT_POSITION:
        default:
          return d3.axisLeft(scale);
      }
    })()
      .tickSizeOuter(0)
      // Hide label for even-index.
      .tickFormat((d, i) => (i % 2 !== 0 && !isEvenLabelIndexVisible ? null : d));

    return new ChartBandAxis(scale, axis);
  }
}

class ChartLinearAxis extends ChartAxis {}

class ChartBandAxis extends ChartAxis {}

/** @abstract */
class Chart {
  /**
   * @param {ChartLayout} layout
   * @param {ChartAxis} xAxis
   * @param {ChartAxis} yAxis
   */
  constructor(layout, xAxis, yAxis) {
    if (this.constructor === Chart) {
      throw new Error(`Can't instantiate abstract class ${this.constructor.name}`);
    }
    if (this.method === Chart.prototype.render) {
      throw new Error(
        `Abstract method '${this.constructor.prototype.render.name}()' have to be overridden`
      );
    }

    /** @type {ChartLayout} */
    this._layout = layout;
    /** @type {ChartAxis} */
    this._xAxis = xAxis;
    /** @type {ChartAxis} */
    this._yAxis = yAxis;
    /** @type {SVGElement} */
    this._svg = d3
      .create("svg")
      .attr("width", this._layout.width)
      .attr("height", this._layout.height);

    Object.seal(this);
  }

  /**
   * @param {Object[]} data
   * @param {string} data[].key
   * @param {number} data[].value
   */
  render(data) {}
}

class BarChart extends Chart {
  /**
   * @param {ChartLayout} layout
   * @param {ChartBandAxis} xAxis
   * @param {ChartLinearAxis} yAxis
   */
  constructor(layout, xAxis, yAxis) {
    super(layout, xAxis, yAxis);
  }

  render(data) {
    // Draw x-axis.
    this._svg
      .append("g")
      .attr("transform", `translate(0, ${this._layout.height - this._layout.marginBottom})`)
      .style("font-size", `${this._layout.fontSize}`)
      .call(this._xAxis.axis);

    // Draw y-axis.
    this._svg
      .append("g")
      .attr("transform", `translate(${this._layout.marginLeft}, 0)`)
      .style("font-size", `${this._layout.fontSize}`)
      .call(this._yAxis.axis);

    // Draw bar.
    this._svg
      .selectAll()
      .data(data)
      .enter()
      .append("rect")
      .style("fill", Android.colorHex("colorPrimary"))
      .attr("x", (d) => this._xAxis.scale(d.key))
      .attr("y", (d) => this._yAxis.scale(d.value))
      .attr("width", this._xAxis.scale.bandwidth())
      .attr(
        "height",
        (d) => this._layout.height - this._layout.marginBottom - this._yAxis.scale(d.value)
      );

    container.append(this._svg.node());
    d3.select("body").style("background-color", this._layout.backgroundColor);
  }
}

class HorizontalBarChart extends Chart {
  /**
   * @param {ChartLayout} layout
   * @param {ChartLinearAxis} xAxis
   * @param {ChartBandAxis} yAxis
   */
  constructor(layout, xAxis, yAxis) {
    super(layout, xAxis, yAxis);
  }

  render(data) {
    // Draw x-axis.
    this._svg
      .append("g")
      .attr("transform", `translate(0, ${this._layout.height - this._layout.marginBottom})`)
      .style("font-size", `${this._layout.fontSize}`)
      .call(this._xAxis.axis);

    // Draw y-axis.
    this._yAxis.scale.paddingInner(0.55);
    this._svg
      .append("g")
      .attr("transform", `translate(${this._layout.marginLeft}, 0)`)
      .call(this._yAxis.axis.tickValues([]));
    // Create new label above the bar.
    this._svg
      .selectAll(".y-label")
      .data(data)
      .enter()
      .append("text")
      .attr("class", "y-label")
      .attr("x", this._layout.marginLeft + 5)
      .attr("y", (d) => this._yAxis.scale(d.key) - 5)
      .style("font-size", `${this._layout.fontSize}`)
      .text((d) => d.key);

    // Draw bar.
    this._svg
      .selectAll("rect")
      .data(data)
      .enter()
      .append("rect")
      .style("fill", Android.colorHex("colorPrimary"))
      .attr("x", this._layout.marginLeft)
      .attr("y", (d) => this._yAxis.scale(d.key))
      .attr("width", (d) => this._xAxis.scale(d.value) - this._layout.marginLeft)
      .attr("height", this._yAxis.scale.bandwidth());

    container.append(this._svg.node());
    d3.select("body").style("background-color", this._layout.backgroundColor);
  }
}
