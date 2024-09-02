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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.assetbinding.JsInterface;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONArray;

public class ChartBinding {
  private ChartBinding() {}

  /**
   * @param layoutBinding JavaScript code from {@link ChartLayoutBinding#init}.
   * @param xScaleBinding JavaScript code from {@link ChartScaleBinding#createBandScale}.
   * @param yScaleBinding JavaScript code from {@link ChartScaleBinding#createLinearScale} or {@link
   *     ChartScaleBinding#createPercentageLinearScale}.
   * @param data List of data to be rendered with.
   * @param color Bar color.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static <K, V> String renderBarChart(
      @NonNull String layoutBinding,
      @NonNull String xScaleBinding,
      @NonNull String yScaleBinding,
      @NonNull List<ChartData.Single<K, V>> data,
      @ColorInt int color) {
    Objects.requireNonNull(layoutBinding);
    Objects.requireNonNull(xScaleBinding);
    Objects.requireNonNull(yScaleBinding);
    Objects.requireNonNull(data);

    final JSONArray formattedData =
        new JSONArray(data.stream().map(ChartData::toJson).collect(Collectors.toList()));

    return "chart.renderBarChart("
        + layoutBinding
        + ", "
        + xScaleBinding
        + ", "
        + yScaleBinding
        + ", "
        + formattedData
        + ", \""
        + JsInterface.argbToRgbaHex(color)
        + "\")";
  }

  /**
   * @param layoutBinding JavaScript code from {@link ChartLayoutBinding#init}.
   * @param xScaleBinding JavaScript code from {@link ChartScaleBinding#createBandScale}.
   * @param yScaleBinding JavaScript code from {@link ChartScaleBinding#createLinearScale} or {@link
   *     ChartScaleBinding#createPercentageLinearScale}.
   * @param data List of data to be rendered with.
   * @param colors Ordered bar colors for the {@code groupInOrder}.
   * @param groupInOrder Ordered groups, indicating which one is drawn first.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static <K, V, G> String renderStackedBarChart(
      @NonNull String layoutBinding,
      @NonNull String xScaleBinding,
      @NonNull String yScaleBinding,
      @NonNull List<ChartData.Multiple<K, V, G>> data,
      @NonNull @ColorInt List<Integer> colors,
      @NonNull Set<String> groupInOrder) {
    Objects.requireNonNull(layoutBinding);
    Objects.requireNonNull(xScaleBinding);
    Objects.requireNonNull(yScaleBinding);
    Objects.requireNonNull(data);
    Objects.requireNonNull(colors);
    Objects.requireNonNull(groupInOrder);

    final JSONArray formattedData =
        new JSONArray(data.stream().map(ChartData::toJson).collect(Collectors.toList()));
    final JSONArray formattedColors =
        new JSONArray(colors.stream().map(JsInterface::argbToRgbaHex).collect(Collectors.toList()));

    return "chart.renderStackedBarChart("
        + layoutBinding
        + ", "
        + xScaleBinding
        + ", "
        + yScaleBinding
        + ", "
        + formattedData
        + ", "
        + formattedColors
        + ", "
        + new JSONArray(groupInOrder)
        + ")";
  }

  /**
   * @param layoutBinding JavaScript code from {@link ChartLayoutBinding#init}.
   * @param data List of ordered data to be rendered with.
   * @param colors Ordered donut slice colors for the data.
   * @param svgTextInCenter Text in SVG format (within {@code <text>} attribute) positioned at the
   *     center of the donut graph.
   * @return A valid JavaScript code for this method.
   */
  @NonNull
  public static <K, V> String renderDonutChart(
      @NonNull String layoutBinding,
      @NonNull List<ChartData.Single<K, V>> data,
      @NonNull @ColorInt List<Integer> colors,
      @Nullable String svgTextInCenter) {
    Objects.requireNonNull(layoutBinding);
    Objects.requireNonNull(data);
    Objects.requireNonNull(colors);

    final JSONArray formattedData =
        new JSONArray(data.stream().map(ChartData::toJson).collect(Collectors.toList()));
    final JSONArray formattedColors =
        new JSONArray(colors.stream().map(JsInterface::argbToRgbaHex).collect(Collectors.toList()));

    return "chart.renderDonutChart("
        + layoutBinding
        + ", "
        + formattedData
        + ", "
        + formattedColors
        + ", `"
        + svgTextInCenter
        + "`)";
  }
}
