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
import androidx.core.util.Function;
import androidx.core.util.Pair;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChartUtil {
  private ChartUtil() {}

  @NonNull
  public static Map<String, BigDecimal> toDateTimeData(
      @NonNull Map<ZonedDateTime, BigDecimal> data,
      @NonNull Pair<ZonedDateTime, ZonedDateTime> dateStartEnd) {
    Objects.requireNonNull(data);
    Objects.requireNonNull(dateStartEnd);

    final Map<String, BigDecimal> result = new LinkedHashMap<>();
    final Function<ChronoUnit, Long> totalDate =
        (unit) -> unit.between(dateStartEnd.first, dateStartEnd.second) + 1; // +1 for inclusivity.
    final BiFunction<ChronoUnit, ZonedDateTime, String> key =
        (unit, date) ->
            switch (unit) {
              case DAYS -> Integer.toString(date.getDayOfMonth());
              case MONTHS ->
                  date.getMonth().name().substring(0, 1).toUpperCase()
                      + date.getMonth().name().substring(1, 3).toLowerCase();
              default -> Integer.toString(date.getYear());
            };
    ChronoUnit groupBy;

    // Determine how the data will be grouped based on the date range.
    if (totalDate.apply(ChronoUnit.YEARS) > 1) groupBy = ChronoUnit.YEARS;
    else if (totalDate.apply(ChronoUnit.MONTHS) > 1) groupBy = ChronoUnit.MONTHS;
    else groupBy = ChronoUnit.DAYS;

    for (int i = 0; i < totalDate.apply(groupBy); i++) {
      final ZonedDateTime date = dateStartEnd.first.plus(i, groupBy);
      final String formattedKey = key.apply(groupBy, date);

      result.put(
          formattedKey,
          data.entrySet().stream()
              .filter(entry -> key.apply(groupBy, entry.getKey()).equals(formattedKey))
              .map(Map.Entry::getValue)
              .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    return result;
  }

  /**
   * @param data Map of data to convert.
   * @param mapType Type of map to be used to determine its sorting behavior.
   */
  @NonNull
  public static Map<String, Double> toPercentageData(
      @NonNull Map<String, BigDecimal> data, @NonNull Supplier<Map<String, Double>> mapType) {
    Objects.requireNonNull(data);
    Objects.requireNonNull(mapType);

    final Map<String, Double> result =
        mapType.get() instanceof TreeMap
            ? new TreeMap<>(
                (a, b) -> {
                  // Special case where string of numbers sorted wrongly.
                  if (a.length() != b.length()) return a.length() < b.length() ? -1 : 1;
                  return a.compareTo(b);
                })
            : mapType.get();
    final BigDecimal actualMaxValue =
        data.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    final BigDecimal paddedMaxValue =
        actualMaxValue.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ONE // Prevent zero division.
            : ChartUtil._ceilToNearestTen(
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

    return amount
        .divide(rounding, 0, RoundingMode.CEILING)
        .multiply(rounding)
        .max(BigDecimal.valueOf(100)); // Set the minimum value to 100.
  }
}
