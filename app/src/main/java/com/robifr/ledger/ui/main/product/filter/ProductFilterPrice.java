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

package com.robifr.ledger.ui.main.product.filter;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.robifr.ledger.R;
import com.robifr.ledger.data.ProductFilters;
import com.robifr.ledger.databinding.ProductDialogFilterBinding;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import com.robifr.ledger.ui.main.product.ProductFragment;
import java.util.Objects;

public class ProductFilterPrice {
  @NonNull private final ProductFragment _fragment;
  @NonNull private final ProductDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;
  @NonNull private final PriceTextWatcher _minPriceTextWatcher;
  @NonNull private final PriceTextWatcher _maxPriceTextWatcher;

  public ProductFilterPrice(
      @NonNull ProductFragment fragment,
      @NonNull ProductDialogFilterBinding dialogBinding,
      @NonNull BottomSheetDialog dialog) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = Objects.requireNonNull(dialogBinding);
    this._dialog = Objects.requireNonNull(dialog);

    final TextInputEditText minPrice = this._dialogBinding.filterPrice.minimumPrice;
    final TextInputEditText maxPrice = this._dialogBinding.filterPrice.maximumPrice;
    this._minPriceTextWatcher = new PriceTextWatcher(minPrice, "id", "ID");
    this._maxPriceTextWatcher = new PriceTextWatcher(maxPrice, "id", "ID");

    minPrice.addTextChangedListener(this._minPriceTextWatcher);
    maxPrice.addTextChangedListener(this._maxPriceTextWatcher);
  }

  /**
   * @param minPrice Formatted text of min {@link ProductFilters#filteredPrice() price}.
   */
  public void setFilteredMinPriceText(@Nullable String minPrice) {
    final String currentText = this._dialogBinding.filterPrice.minimumPrice.getText().toString();
    if (currentText.equals(minPrice)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterPrice.minimumPrice.removeTextChangedListener(
        this._minPriceTextWatcher);
    this._dialogBinding.filterPrice.minimumPrice.setText(minPrice);
    this._dialogBinding.filterPrice.minimumPrice.addTextChangedListener(this._minPriceTextWatcher);
  }

  /**
   * @param maxPrice Formatted text of max {@link ProductFilters#filteredPrice() price}.
   */
  public void setFilteredMaxPriceText(@Nullable String maxPrice) {
    final String currentText = this._dialogBinding.filterPrice.maximumPrice.getText().toString();
    if (currentText.equals(maxPrice)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterPrice.maximumPrice.removeTextChangedListener(
        this._maxPriceTextWatcher);
    this._dialogBinding.filterPrice.maximumPrice.setText(maxPrice);
    this._dialogBinding.filterPrice.maximumPrice.addTextChangedListener(this._maxPriceTextWatcher);
  }

  private class PriceTextWatcher extends CurrencyTextWatcher {
    public PriceTextWatcher(
        @NonNull EditText view, @NonNull String language, @NonNull String country) {
      super(view, language, country);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);

      switch (this._view.getId()) {
        case R.id.minimumPrice ->
            ProductFilterPrice.this
                ._fragment
                .productViewModel()
                .filterView()
                .onMinPriceTextChanged(this.newText());

        case R.id.maximumPrice ->
            ProductFilterPrice.this
                ._fragment
                .productViewModel()
                .filterView()
                .onMaxPriceTextChanged(this.newText());
      }
    }
  }
}
