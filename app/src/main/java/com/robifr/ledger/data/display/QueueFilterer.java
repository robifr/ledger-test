/**
 * Copyright 2024 Robi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robifr.ledger.data.display;

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
