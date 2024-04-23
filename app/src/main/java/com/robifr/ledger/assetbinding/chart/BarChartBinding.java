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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BarChartBinding {
  private BarChartBinding() {}

  /**
   * @param layoutBinding Instance script from {@link ChartLayoutBinding#init(int, int, int, int,
   *     int, int, int, int)}.
   * @param xAxisBinding Instance script from {@link ChartAxisBinding#withBandScale(String,
   *     ChartAxisBinding.Position, List, boolean)}.
   * @param yAxisBinding Instance script from either:
   *     <ul>
   *       <li>{@link ChartAxisBinding#withLinearScale(String, ChartAxisBinding.Position, Pair)},
   *       <li>{@link ChartAxisBinding#withBandScale(String, ChartAxisBinding.Position, List,
   *           boolean)}.
   *     </ul>
   *
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static String init(
      @NonNull String layoutBinding, @NonNull String xAxisBinding, @NonNull String yAxisBinding) {
    Objects.requireNonNull(layoutBinding);
    Objects.requireNonNull(xAxisBinding);
    Objects.requireNonNull(yAxisBinding);

    return "new BarChart(" + layoutBinding + ", " + xAxisBinding + ", " + yAxisBinding + ")";
  }

  /**
   * @param barChartBinding Instance script from {@link BarChartBinding#init(String, String,
   *     String)}.
   * @param data Map of data to be rendered with. The key should match the {@code domain} from
   *     {@link BarChartBinding#init(String, String, String) xAxisBinding}.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static String render(@NonNull String barChartBinding, @NonNull Map<String, Integer> data) {
    Objects.requireNonNull(barChartBinding);
    Objects.requireNonNull(data);

    final JSONArray formattedData =
        new JSONArray(
            data.entrySet().stream()
                .map(
                    entry -> {
                      try {
                        return new JSONObject()
                            .put("key", entry.getKey())
                            .put("value", entry.getValue());
                      } catch (JSONException e) {
                        throw new RuntimeException(e);
                      }
                    })
                .collect(Collectors.toList()));
    return barChartBinding + ".render(" + formattedData + ")";
  }
}
