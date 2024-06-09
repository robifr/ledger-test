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
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChartUtil {
  private ChartUtil() {}

  @NonNull
  public static Map<String, Double> toPercentageData(@NonNull Map<String, BigDecimal> data) {
    Objects.requireNonNull(data);

    final Map<String, Double> result =
        new TreeMap<>(
            (a, b) -> {
              // To sort string of numbers.
              if (a.length() != b.length()) return a.length() < b.length() ? -1 : 1;
              return a.compareTo(b);
            });
    final BigDecimal actualMaxValue =
        data.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    final BigDecimal paddedMaxValue =
        ChartUtil._ceilToNearestTen(
            actualMaxValue.add(
                actualMaxValue.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)));

    for (Map.Entry<String, BigDecimal> d : data.entrySet()) {
      result.put(
          d.getKey(),
          d.getValue()
              .divide(paddedMaxValue, 2, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100))
              .doubleValue());
    }

    return result;
  }

  @NonNull
  public static List<String> toPercentageLinearDomain(@NonNull Map<String, BigDecimal> data) {
    Objects.requireNonNull(data);

    final BigDecimal actualMaxValue =
        data.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    final BigDecimal paddedMaxValue =
        ChartUtil._ceilToNearestTen(
            actualMaxValue.add(
                actualMaxValue.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)));
    final BigDecimal gap = paddedMaxValue.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

    return IntStream.rangeClosed(0, 100)
        .mapToObj(
            // From percentages, linearly map the amount up to the top value.
            percent ->
                CurrencyFormat.formatWithUnit(
                    BigDecimal.valueOf(percent).multiply(gap), "id", "ID", ""))
        .collect(Collectors.toList());
  }

  private static BigDecimal _ceilToNearestTen(@NonNull BigDecimal amount) {
    // Calculate the amount ensuring it's rounded
    // to the nearest ceiling multiple of 10 (e.g. 10, 1K, 10K).
    final int magnitude = amount.precision() - amount.scale();
    final BigDecimal rounding = BigDecimal.TEN.pow(magnitude - 1);
    return amount.divide(rounding, 0, RoundingMode.CEILING).multiply(rounding);
  }
}
