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
import com.robifr.ledger.data.model.QueueModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QueueFilterer {
  @NonNull private QueueFilters _filters = QueueFilters.toBuilder().build();

  @NonNull
  public QueueFilters filters() {
    return this._filters;
  }

  public void setFilters(@NonNull QueueFilters filters) {
    this._filters = Objects.requireNonNull(filters);
  }

  @NonNull
  public List<QueueModel> filter(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    return queues.stream()
        .filter(
            queue ->
                !this._shouldFilteredOutByCustomerId(queue)
                    && !this._shouldFilteredOutByStatus(queue)
                    && !this._shouldFilteredOutByDate(queue)
                    && !this._shouldFilteredOutByTotalPrice(queue))
        .collect(Collectors.toList());
  }

  private boolean _shouldFilteredOutByCustomerId(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    final boolean isCustomerNotInFilterGroup =
        queue.customerId() != null
            // Show all customers when the list empty.
            && !this._filters.filteredCustomerIds().isEmpty()
            && this._filters.filteredCustomerIds().stream()
                .noneMatch(id -> id.equals(queue.customerId()));

    return (queue.customerId() == null && !this._filters.isNullCustomerShown())
        || isCustomerNotInFilterGroup;
  }

  private boolean _shouldFilteredOutByDate(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    final LocalDate date =
        queue.date().atZone(this._filters.filteredDate().dateStart().getZone()).toLocalDate();
    final LocalDate startDate = this._filters.filteredDate().dateStart().toLocalDate();
    final LocalDate endDate = this._filters.filteredDate().dateEnd().toLocalDate();

    return date.isBefore(startDate) || date.isAfter(endDate);
  }

  private boolean _shouldFilteredOutByStatus(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    return !this._filters.filteredStatus().contains(queue.status());
  }

  private boolean _shouldFilteredOutByTotalPrice(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    final BigDecimal first = this._filters.filteredTotalPrice().first;
    final BigDecimal second = this._filters.filteredTotalPrice().second;

    return (first != null && queue.grandTotalPrice().compareTo(first) < 0)
        || (second != null && queue.grandTotalPrice().compareTo(second) > 0);
  }
}
