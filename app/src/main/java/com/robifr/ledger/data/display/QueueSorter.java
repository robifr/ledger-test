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
