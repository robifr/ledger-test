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
import com.robifr.ledger.data.model.CustomerModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomerFilterer {
  @NonNull private CustomerFilters _filters = CustomerFilters.toBuilder().build();

  @NonNull
  public CustomerFilters filters() {
    return this._filters;
  }

  public void setFilters(@NonNull CustomerFilters filters) {
    this._filters = Objects.requireNonNull(filters);
  }

  @NonNull
  public List<CustomerModel> filter(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    return customers.stream()
        .filter(
            customer ->
                !this._shouldFilteredOutByBalance(customer)
                    && !this._shouldFilteredOutByDebt(customer))
        .collect(Collectors.toList());
  }

  private boolean _shouldFilteredOutByBalance(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    final Long first = this._filters.filteredBalance().first;
    final Long second = this._filters.filteredBalance().second;

    return (first != null && customer.balance() < first)
        || (second != null && customer.balance() > second);
  }

  private boolean _shouldFilteredOutByDebt(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    final BigDecimal first = this._filters.filteredDebt().first;
    final BigDecimal second = this._filters.filteredDebt().second;

    return (first != null && customer.debt().abs().compareTo(first.abs()) < 0)
        || (second != null && customer.debt().abs().compareTo(second.abs()) > 0);
  }
}
