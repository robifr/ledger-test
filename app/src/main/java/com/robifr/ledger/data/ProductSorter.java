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
import com.robifr.ledger.data.model.ProductModel;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProductSorter {
  @NonNull
  private ProductSortMethod _sortMethod =
      new ProductSortMethod(ProductSortMethod.SortBy.NAME, true);

  @NonNull
  public ProductSortMethod sortMethod() {
    return this._sortMethod;
  }

  public void setSortMethod(@NonNull ProductSortMethod sortMethod) {
    this._sortMethod = Objects.requireNonNull(sortMethod);
  }

  @NonNull
  public List<ProductModel> sort(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    return switch (this._sortMethod.sortBy()) {
      case NAME -> this._sortByName(products);
      case PRICE -> this._sortByPrice(products);
    };
  }

  @NonNull
  private List<ProductModel> _sortByName(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    final Collator collator = Collator.getInstance(new Locale("id", "ID"));
    collator.setStrength(Collator.SECONDARY);

    final Comparator<ProductModel> compare = Comparator.comparing(ProductModel::name, collator);
    final Comparator<ProductModel> comparator =
        this._sortMethod.isAscending() ? compare : compare.reversed();

    final ArrayList<ProductModel> sortedProducts = new ArrayList<>(products);
    sortedProducts.sort(comparator);
    return sortedProducts;
  }

  @NonNull
  private List<ProductModel> _sortByPrice(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    final Comparator<ProductModel> compare = Comparator.comparing(ProductModel::price);
    final Comparator<ProductModel> comparator =
        this._sortMethod.isAscending() ? compare : compare.reversed();

    final ArrayList<ProductModel> sortedProducts = new ArrayList<>(products);
    sortedProducts.sort(comparator);
    return sortedProducts;
  }
}
