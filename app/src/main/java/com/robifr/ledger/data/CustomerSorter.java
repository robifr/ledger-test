/**
 * Copyright (c) 2022-present Robi
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
