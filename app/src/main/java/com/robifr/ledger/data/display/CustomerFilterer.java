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
