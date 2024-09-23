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
