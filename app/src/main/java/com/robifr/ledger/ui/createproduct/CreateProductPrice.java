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

package com.robifr.ledger.ui.createproduct;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
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

  public void setInputtedPriceText(@NonNull String price) {
    Objects.requireNonNull(price);

    final String currentText = this._fragment.fragmentBinding().price.getText().toString();
    if (currentText.equals(price)) return;

    // Remove listener to prevent any sort of formatting.
    this._fragment.fragmentBinding().price.removeTextChangedListener(this._priceTextWatcher);
    this._fragment.fragmentBinding().price.setText(price);
    this._fragment.fragmentBinding().price.setSelection(price.length());
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
