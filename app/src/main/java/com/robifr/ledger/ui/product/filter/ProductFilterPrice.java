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

package com.robifr.ledger.ui.product.filter;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.ProductFilters;
import com.robifr.ledger.databinding.ProductDialogFilterBinding;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import com.robifr.ledger.ui.product.ProductFragment;
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
  public void setFilteredMinPriceText(@NonNull String minPrice) {
    Objects.requireNonNull(minPrice);

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
  public void setFilteredMaxPriceText(@NonNull String maxPrice) {
    Objects.requireNonNull(maxPrice);

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
