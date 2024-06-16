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

package com.robifr.ledger.data;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.QueueModel;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @param filteredCustomerIds Filter queue if {@link QueueModel#customerId() customer ID} is
 *     included inside the list.
 * @param isNullCustomerShown Whether queue with no {@link QueueModel#customerId() customer ID}
 *     should be shown or not.
 * @param filteredStatus Filter queue if {@link QueueModel#status() status} is included.
 * @param filteredTotalPrice Filter queue if {@link QueueModel#grandTotalPrice() grand total price}
 *     is in-between min (first) and max (second). Set the pair value as null to represent unbounded
 *     number.
 * @param filteredDate Additional field mostly for UI purposes, simplifying the retrieval of the
 *     date range for {@link QueueFilters#filteredDateStartEnd}. This field doesn't directly impact
 *     the filtering.
 * @param filteredDateStartEnd Filter queues if {@link QueueModel#date() date} is still considered
 *     within specified range of start (first) and end (second) date. Use {@link
 *     DateRange#dateStartEnd()} to obtain the value.
 */
public record QueueFilters(
    @NonNull List<Long> filteredCustomerIds,
    boolean isNullCustomerShown,
    @NonNull Set<QueueModel.Status> filteredStatus,
    @NonNull Pair<BigDecimal, BigDecimal> filteredTotalPrice,
    @NonNull DateRange filteredDate,
    @NonNull Pair<ZonedDateTime, ZonedDateTime> filteredDateStartEnd) {
  public enum DateRange {
    ALL_TIME(R.string.text_all_time),
    TODAY(R.string.text_today),
    YESTERDAY(R.string.text_yesterday),
    THIS_WEEK(R.string.text_this_week),
    THIS_MONTH(R.string.text_this_month),
    CUSTOM(R.string.queuefilter_date_selecteddate_chip);

    @StringRes private final int _resourceString;

    private DateRange(@StringRes int resourceString) {
      this._resourceString = resourceString;
    }

    @StringRes
    public int resourceString() {
      return this._resourceString;
    }

    /**
     * @return Pair of start (first) and end (second) date.
     * @apiNote For {@link DateRange#CUSTOM} enum, you have to manually specify the value, otherwise
     *     initial epoch time will be set.
     */
    @NonNull
    public Pair<ZonedDateTime, ZonedDateTime> dateStartEnd() {
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

  public QueueFilters {
    Objects.requireNonNull(filteredCustomerIds);
    Objects.requireNonNull(filteredStatus);
    Objects.requireNonNull(filteredTotalPrice);
    Objects.requireNonNull(filteredDate);
    Objects.requireNonNull(filteredDateStartEnd);
    Objects.requireNonNull(filteredDateStartEnd.first);
    Objects.requireNonNull(filteredDateStartEnd.second);

    filteredCustomerIds = Collections.unmodifiableList(filteredCustomerIds);
    filteredStatus = Collections.unmodifiableSet(filteredStatus);
  }

  @NonNull
  public static Builder toBuilder() {
    return new Builder();
  }

  @NonNull
  public static Builder toBuilder(@NonNull QueueFilters filters) {
    Objects.requireNonNull(filters);

    return new Builder()
        .setFilteredCustomerIds(filters.filteredCustomerIds())
        .setNullCustomerShown(filters.isNullCustomerShown())
        .setFilteredStatus(filters.filteredStatus())
        .setFilteredTotalPrice(filters.filteredTotalPrice())
        .setFilteredDate(filters.filteredDate())
        .setFilteredDateStartEnd(filters.filteredDateStartEnd());
  }

  public static class Builder {
    @NonNull private List<Long> _filteredCustomerIds = new ArrayList<>();
    private boolean _isNullCustomerShown = false;
    @NonNull private Set<QueueModel.Status> _filteredStatus = new HashSet<>();
    @NonNull private Pair<BigDecimal, BigDecimal> _filteredTotalPrice = new Pair<>(null, null);
    @NonNull private DateRange _filteredDate = DateRange.ALL_TIME;

    @NonNull
    private Pair<ZonedDateTime, ZonedDateTime> _filteredDateStartEnd =
        this._filteredDate.dateStartEnd();

    private Builder() {}

    @NonNull
    public Builder setFilteredCustomerIds(@NonNull List<Long> customerIds) {
      this._filteredCustomerIds = Objects.requireNonNull(customerIds);
      return this;
    }

    @NonNull
    public Builder setNullCustomerShown(boolean isShown) {
      this._isNullCustomerShown = isShown;
      return this;
    }

    @NonNull
    public Builder setFilteredStatus(@NonNull Set<QueueModel.Status> status) {
      this._filteredStatus = Objects.requireNonNull(status);
      return this;
    }

    @NonNull
    public Builder setFilteredTotalPrice(@NonNull Pair<BigDecimal, BigDecimal> price) {
      this._filteredTotalPrice = Objects.requireNonNull(price);
      return this;
    }

    @NonNull
    public Builder setFilteredDate(@NonNull DateRange date) {
      this._filteredDate = Objects.requireNonNull(date);
      return this;
    }

    @NonNull
    public Builder setFilteredDateStartEnd(
        @NonNull Pair<ZonedDateTime, ZonedDateTime> dateStartEnd) {
      Objects.requireNonNull(dateStartEnd);
      Objects.requireNonNull(dateStartEnd.first);
      Objects.requireNonNull(dateStartEnd.second);

      this._filteredDateStartEnd = dateStartEnd;
      return this;
    }

    @NonNull
    public QueueFilters build() {
      return new QueueFilters(
          this._filteredCustomerIds,
          this._isNullCustomerShown,
          this._filteredStatus,
          this._filteredTotalPrice,
          this._filteredDate,
          this._filteredDateStartEnd);
    }
  }
}
