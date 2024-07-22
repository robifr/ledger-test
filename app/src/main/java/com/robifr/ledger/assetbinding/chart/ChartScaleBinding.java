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

package com.robifr.ledger.assetbinding.chart;

import androidx.annotation.NonNull;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;

public class ChartScaleBinding {
  public enum AxisPosition {
    BOTTOM(1),
    LEFT(2);

    private final int _value;

    private AxisPosition(int value) {
      this._value = value;
    }

    public int value() {
      return this._value;
    }
  }

  private ChartScaleBinding() {}

  /**
   * @param axisPosition Position for the axis.
   * @param domain List of numbers containing at least two numbers representing the minimum and
   *     maximum values.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static String createLinearScale(
      @NonNull AxisPosition axisPosition, @NonNull List<Double> domain) {
    Objects.requireNonNull(axisPosition);
    Objects.requireNonNull(domain);

    return "chart.createLinearScale(" + axisPosition.value() + ", " + new JSONArray(domain) + ")";
  }

  /**
   * Same as a {@link ChartScaleBinding#createLinearScale} but with support for larger numbers by
   * using percentage. Each domain (0-100) will be presented with the provided domain strings.
   *
   * @param axisPosition Position for the axis.
   * @param domain List of 101 strings representing the percentage values.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static String createPercentageLinearScale(
      @NonNull AxisPosition axisPosition, @NonNull List<String> domain) {
    Objects.requireNonNull(axisPosition);
    Objects.requireNonNull(domain);

    return "chart.createPercentageLinearScale("
        + axisPosition.value()
        + ", "
        + new JSONArray(domain)
        + ")";
  }

  /**
   * @param axisPosition Position for the axis.
   * @param domain List of string for the label.
   * @param isAllLabelVisible Whether label should be visible, even when the domain is large.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static String createBandScale(
      @NonNull AxisPosition axisPosition, @NonNull List<String> domain, boolean isAllLabelVisible) {
    Objects.requireNonNull(axisPosition);
    Objects.requireNonNull(domain);

    return "chart.createBandScale("
        + axisPosition.value()
        + ", "
        + new JSONArray(domain)
        + ", "
        + isAllLabelVisible
        + ")";
  }
}
