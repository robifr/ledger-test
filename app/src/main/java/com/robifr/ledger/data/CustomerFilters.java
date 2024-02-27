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
