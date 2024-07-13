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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChartBinding {
  private ChartBinding() {}

  /**
   * @param layoutBinding JavaScript code from {@link ChartLayoutBinding#init}.
   * @param xScaleBinding JavaScript code from {@link ChartScaleBinding#createBandScale}.
   * @param yScaleBinding JavaScript code from {@link ChartScaleBinding#createLinearScale} or {@link
   *     ChartScaleBinding#createPercentageLinearScale}.
   * @param data Map of data to be rendered with. The key should match the {@code domain} from
   *     {@code xScaleBinding}.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static String renderBarChart(
      @NonNull String layoutBinding,
      @NonNull String xScaleBinding,
      @NonNull String yScaleBinding,
      @NonNull Map<String, Double> data) {
    Objects.requireNonNull(layoutBinding);
    Objects.requireNonNull(xScaleBinding);
    Objects.requireNonNull(yScaleBinding);
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
    return "chart.renderBarChart("
        + layoutBinding
        + ", "
        + xScaleBinding
        + ", "
        + yScaleBinding
        + ", "
        + formattedData
        + ")";
  }
}
