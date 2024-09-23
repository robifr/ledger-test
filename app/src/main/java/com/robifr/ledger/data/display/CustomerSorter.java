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
import com.robifr.ledger.data.model.CustomerModel;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CustomerSorter {
  @NonNull
  private CustomerSortMethod _sortMethod =
      new CustomerSortMethod(CustomerSortMethod.SortBy.NAME, true);

  @NonNull
  public CustomerSortMethod sortMethod() {
    return this._sortMethod;
  }

  public void setSortMethod(@NonNull CustomerSortMethod sortMethod) {
    this._sortMethod = Objects.requireNonNull(sortMethod);
  }

  @NonNull
  public List<CustomerModel> sort(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    return switch (this._sortMethod.sortBy()) {
      case NAME -> this._sortByName(customers);
      case BALANCE -> this._sortByBalance(customers);
    };
  }

  @NonNull
  private List<CustomerModel> _sortByName(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    final Collator collator = Collator.getInstance(new Locale("id", "ID"));
    collator.setStrength(Collator.SECONDARY);

    final Comparator<CustomerModel> compare = Comparator.comparing(CustomerModel::name, collator);
    final Comparator<CustomerModel> comparator =
        this._sortMethod.isAscending() ? compare : compare.reversed();

    final ArrayList<CustomerModel> sortedCustomers = new ArrayList<>(customers);
    sortedCustomers.sort(comparator);
    return sortedCustomers;
  }

  @NonNull
  private List<CustomerModel> _sortByBalance(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    final Comparator<CustomerModel> compare = Comparator.comparing(CustomerModel::balance);
    final Comparator<CustomerModel> comparator =
        this._sortMethod.isAscending() ? compare : compare.reversed();

    final ArrayList<CustomerModel> sortedCustomers = new ArrayList<>(customers);
    sortedCustomers.sort(comparator);
    return sortedCustomers;
  }
}
