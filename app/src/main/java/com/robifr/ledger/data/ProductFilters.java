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
import androidx.core.util.Pair;
import com.robifr.ledger.data.model.ProductModel;
import java.util.Objects;

/**
 * @param filteredPrice Filter product if {@link ProductModel#price() price} is in-between min
 *     (first) and max (second). Set the pair value as null to represent unbounded number.
 */
public record ProductFilters(@NonNull Pair<Long, Long> filteredPrice) {
  public ProductFilters {
    Objects.requireNonNull(filteredPrice);
  }

  @NonNull
  public static Builder toBuilder() {
    return new Builder();
  }

  @NonNull
  public static Builder toBuilder(@NonNull ProductFilters filters) {
    Objects.requireNonNull(filters);

    return new Builder().setFilteredPrice(filters.filteredPrice());
  }

  public static class Builder {
    @NonNull private Pair<Long, Long> _filteredPrice = new Pair<>(null, null);

    private Builder() {}

    @NonNull
    public Builder setFilteredPrice(@NonNull Pair<Long, Long> price) {
      this._filteredPrice = Objects.requireNonNull(price);
      return this;
    }

    @NonNull
    public ProductFilters build() {
      return new ProductFilters(this._filteredPrice);
    }
  }
}
