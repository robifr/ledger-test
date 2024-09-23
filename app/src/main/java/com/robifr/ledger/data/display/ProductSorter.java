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
