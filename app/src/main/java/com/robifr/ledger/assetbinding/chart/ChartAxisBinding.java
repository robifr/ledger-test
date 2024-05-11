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
import androidx.core.util.Pair;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;

public class ChartAxisBinding {
  public enum Position {
    BOTTOM(1),
    LEFT(2);

    private final int _value;

    private Position(int value) {
      this._value = value;
    }

    public int value() {
      return this._value;
    }
  }

  private ChartAxisBinding() {}

  /**
   * @param layoutBinding Instance script from {@link ChartLayoutBinding#init(int, int, int, int,
   *     int, int, int, int)}.
   * @param axisPosition Position for the axis.
   * @param domain Pair of min (first) and max (second) number for the label.
   * @param isEvenLabelIndexVisible Whether label with even-index visible or not.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static String withLinearScale(
      @NonNull String layoutBinding,
      @NonNull Position axisPosition,
      @NonNull Pair<Integer, Integer> domain,
      boolean isEvenLabelIndexVisible) {
    Objects.requireNonNull(layoutBinding);
    Objects.requireNonNull(axisPosition);
    Objects.requireNonNull(domain);

    return "ChartAxis.withLinearScale("
        + layoutBinding
        + ", "
        + axisPosition.value()
        + ", "
        + new JSONArray(List.of(domain.first, domain.second))
        + ", "
        + isEvenLabelIndexVisible
        + ")";
  }

  /**
   * @param layoutBinding Instance script from {@link ChartLayoutBinding#init(int, int, int, int,
   *     int, int, int, int)}.
   * @param axisPosition Position for the axis.
   * @param domain List of string for the label.
   * @param isEvenLabelIndexVisible Whether label with even-index visible or not.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static String withBandScale(
      @NonNull String layoutBinding,
      @NonNull Position axisPosition,
      @NonNull List<String> domain,
      boolean isEvenLabelIndexVisible) {
    Objects.requireNonNull(layoutBinding);
    Objects.requireNonNull(axisPosition);
    Objects.requireNonNull(domain);

    return "ChartAxis.withBandScale("
        + layoutBinding
        + ", "
        + axisPosition.value()
        + ", "
        + new JSONArray(domain)
        + ", "
        + isEvenLabelIndexVisible
        + ")";
  }
}
