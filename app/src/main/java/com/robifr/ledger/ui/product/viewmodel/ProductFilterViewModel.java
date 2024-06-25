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

package com.robifr.ledger.ui.product.viewmodel;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.robifr.ledger.data.display.ProductFilterer;
import com.robifr.ledger.data.display.ProductFilters;
import com.robifr.ledger.data.display.ProductSortMethod;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductFilterViewModel {
  @NonNull private final ProductViewModel _viewModel;
  @NonNull private final ProductFilterer _filterer;
  @NonNull private final MutableLiveData<String> _inputtedMinPriceText = new MutableLiveData<>();
  @NonNull private final MutableLiveData<String> _inputtedMaxPriceText = new MutableLiveData<>();

  public ProductFilterViewModel(
      @NonNull ProductViewModel viewModel, @NonNull ProductFilterer filterer) {
    this._viewModel = Objects.requireNonNull(viewModel);
    this._filterer = Objects.requireNonNull(filterer);
  }

  @NonNull
  public LiveData<String> inputtedMinPriceText() {
    return this._inputtedMinPriceText;
  }

  @NonNull
  public LiveData<String> inputtedMaxPriceText() {
    return this._inputtedMaxPriceText;
  }

  /**
   * Get current inputted filter from any corresponding inputted live data. If any live data is set
   * using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public ProductFilters inputtedFilters() {
    // Nullable value to represent unbounded range.
    Long minPrice = this._filterer.filters().filteredPrice().first;
    Long maxPrice = this._filterer.filters().filteredPrice().second;

    try {
      final String minPriceText = this._inputtedMinPriceText.getValue();

      if (minPriceText != null && !minPriceText.isBlank()) {
        minPrice = CurrencyFormat.parse(minPriceText, "id", "ID").longValue();
      }

    } catch (ParseException ignore) {
    }

    try {
      final String maxPriceText = this._inputtedMaxPriceText.getValue();

      if (maxPriceText != null && !maxPriceText.isBlank()) {
        maxPrice = CurrencyFormat.parse(maxPriceText, "id", "ID").longValue();
      }

    } catch (ParseException ignore) {
    }

    return ProductFilters.toBuilder().setFilteredPrice(new Pair<>(minPrice, maxPrice)).build();
  }

  public void onMinPriceTextChanged(@NonNull String minPrice) {
    Objects.requireNonNull(minPrice);

    this._inputtedMinPriceText.setValue(minPrice);
  }

  public void onMaxPriceTextChanged(@NonNull String maxPrice) {
    Objects.requireNonNull(maxPrice);

    this._inputtedMaxPriceText.setValue(maxPrice);
  }

  public void onFiltersChanged(@NonNull ProductFilters filters) {
    final List<ProductModel> products =
        Objects.requireNonNullElse(this._viewModel.products().getValue(), new ArrayList<>());
    this.onFiltersChanged(filters, products);
  }

  public void onFiltersChanged(
      @NonNull ProductFilters filters, @NonNull List<ProductModel> products) {
    Objects.requireNonNull(filters);
    Objects.requireNonNull(products);

    final String minTotalPrice =
        filters.filteredPrice().first != null
            ? CurrencyFormat.format(BigDecimal.valueOf(filters.filteredPrice().first), "id", "ID")
            : "";
    final String maxTotalPrice =
        filters.filteredPrice().second != null
            ? CurrencyFormat.format(BigDecimal.valueOf(filters.filteredPrice().second), "id", "ID")
            : "";

    this.onMinPriceTextChanged(minTotalPrice);
    this.onMaxPriceTextChanged(maxTotalPrice);
    this._filterer.setFilters(filters);

    final List<ProductModel> filteredProducts = this._filterer.filter(products);
    final ProductSortMethod sortMethod = this._viewModel.sortMethod().getValue();

    // Re-sort the list, after previously re-populating the list with a new filtered value.
    if (sortMethod != null) this._viewModel.onSortMethodChanged(sortMethod, filteredProducts);
    // Put in the else to prevent multiple call of `ProductFilterViewModel#onCustomersChanged()`
    // from `ProductFilterViewModel#onSortMethodChanged()`.
    else this._viewModel.onProductsChanged(filteredProducts);
  }
}
