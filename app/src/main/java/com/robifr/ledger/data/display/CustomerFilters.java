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
import androidx.core.util.Pair;
import com.robifr.ledger.data.model.CustomerModel;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @param filteredBalance Filter customer if {@link CustomerModel#balance() balance} is in-between
 *     min (first) and max (second). Set the pair value as null to represent unbounded number.
 * @param filteredDebt Filter customer if {@link CustomerModel#debt() debt} is in-between min
 *     (first) and max (second). Set the pair value as null to represent unbounded number.
 */
public record CustomerFilters(
    @NonNull Pair<Long, Long> filteredBalance, @NonNull Pair<BigDecimal, BigDecimal> filteredDebt) {
  public CustomerFilters {
    Objects.requireNonNull(filteredBalance);
    Objects.requireNonNull(filteredDebt);
  }

  @NonNull
  public static Builder toBuilder() {
    return new Builder();
  }

  @NonNull
  public static Builder toBuilder(@NonNull CustomerFilters filters) {
    Objects.requireNonNull(filters);

    return new Builder()
        .setFilteredBalance(filters.filteredBalance())
        .setFilteredDebt(filters.filteredDebt());
  }

  public static class Builder {
    @NonNull private Pair<Long, Long> _filteredBalance = new Pair<>(null, null);
    @NonNull private Pair<BigDecimal, BigDecimal> _filteredDebt = new Pair<>(null, null);

    private Builder() {}

    @NonNull
    public Builder setFilteredBalance(@NonNull Pair<Long, Long> balance) {
      this._filteredBalance = Objects.requireNonNull(balance);
      return this;
    }

    @NonNull
    public Builder setFilteredDebt(@NonNull Pair<BigDecimal, BigDecimal> debt) {
      this._filteredDebt = Objects.requireNonNull(debt);
      return this;
    }

    @NonNull
    public CustomerFilters build() {
      return new CustomerFilters(this._filteredBalance, this._filteredDebt);
    }
  }
}
