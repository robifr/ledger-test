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

package com.robifr.ledger.data.display;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;
import com.robifr.ledger.R;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public record QueueDate(
    @NonNull Range range, @NonNull ZonedDateTime dateStart, @NonNull ZonedDateTime dateEnd) {
  public QueueDate {
    Objects.requireNonNull(range);
    Objects.requireNonNull(dateStart);
    Objects.requireNonNull(dateEnd);
  }

  /**
   * @apiNote Use {@link QueueDate#withCustomRange(ZonedDateTime, ZonedDateTime)} specifically for
   *     {@link Range#CUSTOM} enum. Otherwise, the initial epoch time will be set for both {@link
   *     QueueDate#dateStart} and {@link QueueDate#dateEnd}.
   */
  public static QueueDate withRange(@NonNull Range range) {
    Objects.requireNonNull(range);

    return new QueueDate(range, range._dateStartEnd().first, range._dateStartEnd().second);
  }

  public static QueueDate withCustomRange(
      @NonNull ZonedDateTime dateStart, @NonNull ZonedDateTime dateEnd) {
    Objects.requireNonNull(dateStart);
    Objects.requireNonNull(dateEnd);

    return new QueueDate(Range.CUSTOM, dateStart, dateEnd);
  }

  public enum Range {
    ALL_TIME(R.string.text_all_time),
    TODAY(R.string.text_today),
    YESTERDAY(R.string.text_yesterday),
    THIS_WEEK(R.string.text_this_week),
    THIS_MONTH(R.string.text_this_month),
    CUSTOM(R.string.queuefilter_date_selecteddate_chip);

    @StringRes private final int _resourceString;

    private Range(@StringRes int resourceString) {
      this._resourceString = resourceString;
    }

    @StringRes
    public int resourceString() {
      return this._resourceString;
    }

    /**
     * @return Pair of start (first) and end (second) date.
     * @apiNote For {@link Range#CUSTOM} enum, you have to manually specify the value, otherwise
     *     initial epoch time will be set.
     */
    @NonNull
    private Pair<ZonedDateTime, ZonedDateTime> _dateStartEnd() {
      return switch (this) {
        case TODAY ->
            new Pair<>(
                LocalDate.now().atStartOfDay(ZoneId.systemDefault()),
                LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));

        case YESTERDAY ->
            new Pair<>(
                LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()),
                LocalDate.now().minusDays(1).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));

        case THIS_WEEK ->
            new Pair<>(
                LocalDate.now()
                    .with(ChronoField.DAY_OF_WEEK, 1)
                    .atStartOfDay()
                    .atZone(ZoneId.systemDefault()),
                LocalDate.now()
                    .with(ChronoField.DAY_OF_WEEK, 7)
                    .atTime(LocalTime.MAX)
                    .atZone(ZoneId.systemDefault()));

        case THIS_MONTH ->
            new Pair<>(
                LocalDate.now()
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .atStartOfDay(ZoneId.systemDefault()),
                LocalDate.now()
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .atTime(LocalTime.MAX)
                    .atZone(ZoneId.systemDefault()));

        case CUSTOM ->
            new Pair<>(
                Instant.EPOCH.atZone(ZoneId.systemDefault()),
                Instant.EPOCH.atZone(ZoneId.systemDefault()));

          // Defaults to `DateRange#ALL_TIME`.
        default ->
            new Pair<>(
                Instant.EPOCH.atZone(ZoneId.systemDefault()),
                LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));
      };
    }
  }
}
