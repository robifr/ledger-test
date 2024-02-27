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

package com.robifr.ledger.ui.create_product;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import java.util.Objects;

public class CreateProductPrice {
  @NonNull private final CreateProductFragment _fragment;
  @NonNull private final PriceTextWatcher _priceTextWatcher;

  public CreateProductPrice(@NonNull CreateProductFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._priceTextWatcher =
        new PriceTextWatcher(this._fragment.fragmentBinding().price, "id", "ID");

    this._fragment.fragmentBinding().price.addTextChangedListener(this._priceTextWatcher);
  }

  public void setInputtedPriceText(@Nullable String price) {
    final String currentText = this._fragment.fragmentBinding().price.getText().toString();
    if (currentText.equals(price)) return;

    final int cursorPosition = price != null ? price.length() : 0;

    // Remove listener to prevent any sort of formatting.
    this._fragment.fragmentBinding().price.removeTextChangedListener(this._priceTextWatcher);
    this._fragment.fragmentBinding().price.setText(price);
    this._fragment.fragmentBinding().price.setSelection(cursorPosition);
    this._fragment.fragmentBinding().price.addTextChangedListener(this._priceTextWatcher);
  }

  private class PriceTextWatcher extends CurrencyTextWatcher {
    public PriceTextWatcher(
        @NonNull EditText view, @NonNull String language, @NonNull String country) {
      super(view, language, country);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);
      CreateProductPrice.this._fragment.createProductViewModel().onPriceTextChanged(this.newText());
    }
  }
}
