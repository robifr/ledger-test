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
import com.robifr.ledger.data.model.ProductModel;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductFilterer {
  @NonNull private ProductFilters _filters = ProductFilters.toBuilder().build();

  @NonNull
  public ProductFilters filters() {
    return this._filters;
  }

  public void setFilters(@NonNull ProductFilters filters) {
    this._filters = Objects.requireNonNull(filters);
  }

  @NonNull
  public List<ProductModel> filter(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    return products.stream()
        .filter(product -> !this._shouldFilteredOutByPrice(product))
        .collect(Collectors.toList());
  }

  private boolean _shouldFilteredOutByPrice(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    final Long first = this._filters.filteredPrice().first;
    final Long second = this._filters.filteredPrice().second;

    return (first != null && product.price() < first)
        || (second != null && product.price() > second);
  }
}
