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
import com.robifr.ledger.data.model.QueueModel;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class QueueSorter {
  @NonNull
  private QueueSortMethod _sortMethod =
      new QueueSortMethod(QueueSortMethod.SortBy.CUSTOMER_NAME, true);

  @NonNull
  public QueueSortMethod sortMethod() {
    return this._sortMethod;
  }

  public void setSortMethod(@NonNull QueueSortMethod sortMethod) {
    this._sortMethod = Objects.requireNonNull(sortMethod);
  }

  @NonNull
  public List<QueueModel> sort(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    return switch (this._sortMethod.sortBy()) {
      case CUSTOMER_NAME -> this._sortByCustomerName(queues);
      case DATE -> this._sortByDate(queues);
      case TOTAL_PRICE -> this._sortByTotalPrice(queues);
    };
  }

  @NonNull
  private List<QueueModel> _sortByCustomerName(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final Collator collator = Collator.getInstance(new Locale("id", "ID"));
    collator.setStrength(Collator.SECONDARY);

    final Comparator<String> compare = Comparator.nullsLast(collator);
    final Comparator<String> comparator =
        this._sortMethod.isAscending() ? compare : compare.reversed();

    final ArrayList<QueueModel> sortedQueues = new ArrayList<>(queues);
    sortedQueues.sort(
        (a, b) -> {
          final String first = a.customer() != null ? a.customer().name() : null;
          final String second = b.customer() != null ? b.customer().name() : null;
          return comparator.compare(first, second);
        });
    return sortedQueues;
  }

  @NonNull
  private List<QueueModel> _sortByDate(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final Comparator<QueueModel> compare = Comparator.comparing(QueueModel::date);
    final Comparator<QueueModel> comparator =
        this._sortMethod.isAscending() ? compare : compare.reversed();

    final ArrayList<QueueModel> sortedQueues = new ArrayList<>(queues);
    sortedQueues.sort(comparator);
    return sortedQueues;
  }

  @NonNull
  private List<QueueModel> _sortByTotalPrice(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final Comparator<QueueModel> compare = Comparator.comparing(QueueModel::grandTotalPrice);
    final Comparator<QueueModel> comparator =
        this._sortMethod.isAscending() ? compare : compare.reversed();

    final ArrayList<QueueModel> sortedQueues = new ArrayList<>(queues);
    sortedQueues.sort(comparator);
    return sortedQueues;
  }
}
