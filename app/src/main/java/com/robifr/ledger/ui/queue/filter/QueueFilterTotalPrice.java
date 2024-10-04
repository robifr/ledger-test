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

package com.robifr.ledger.ui.queue.filter;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.QueueFilters;
import com.robifr.ledger.databinding.QueueDialogFilterBinding;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import com.robifr.ledger.ui.queue.QueueFragment;
import java.math.BigDecimal;
import java.util.Objects;

public class QueueFilterTotalPrice {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;
  @NonNull private final TotalPriceTextWatcher _minPriceTextWatcher;
  @NonNull private final TotalPriceTextWatcher _maxPriceTextWatcher;

  public QueueFilterTotalPrice(
      @NonNull QueueFragment fragment,
      @NonNull QueueDialogFilterBinding dialogBinding,
      @NonNull BottomSheetDialog dialog) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = Objects.requireNonNull(dialogBinding);
    this._dialog = Objects.requireNonNull(dialog);

    final TextInputEditText minTotalPrice = this._dialogBinding.filterTotalPrice.minimumTotalPrice;
    final TextInputEditText maxTotalPrice = this._dialogBinding.filterTotalPrice.maximumTotalPrice;
    this._minPriceTextWatcher = new TotalPriceTextWatcher(minTotalPrice);
    this._maxPriceTextWatcher = new TotalPriceTextWatcher(maxTotalPrice);

    minTotalPrice.addTextChangedListener(this._minPriceTextWatcher);
    maxTotalPrice.addTextChangedListener(this._maxPriceTextWatcher);
  }

  /**
   * @param minTotalPrice Formatted text of min {@link QueueFilters#filteredTotalPrice() total
   *     price}.
   */
  public void setFilteredMinTotalPriceText(@NonNull String minTotalPrice) {
    Objects.requireNonNull(minTotalPrice);

    final String currentText =
        this._dialogBinding.filterTotalPrice.minimumTotalPrice.getText().toString();
    if (currentText.equals(minTotalPrice)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterTotalPrice.minimumTotalPrice.removeTextChangedListener(
        this._minPriceTextWatcher);
    this._dialogBinding.filterTotalPrice.minimumTotalPrice.setText(minTotalPrice);
    this._dialogBinding.filterTotalPrice.minimumTotalPrice.setSelection(minTotalPrice.length());
    this._dialogBinding.filterTotalPrice.minimumTotalPrice.addTextChangedListener(
        this._minPriceTextWatcher);
  }

  /**
   * @param maxTotalPrice Formatted text of max {@link QueueFilters#filteredTotalPrice() total
   *     price}.
   */
  public void setFilteredMaxTotalPriceText(@NonNull String maxTotalPrice) {
    Objects.requireNonNull(maxTotalPrice);

    final String currentText =
        this._dialogBinding.filterTotalPrice.maximumTotalPrice.getText().toString();
    if (currentText.equals(maxTotalPrice)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterTotalPrice.maximumTotalPrice.removeTextChangedListener(
        this._maxPriceTextWatcher);
    this._dialogBinding.filterTotalPrice.maximumTotalPrice.setText(maxTotalPrice);
    this._dialogBinding.filterTotalPrice.maximumTotalPrice.setSelection(maxTotalPrice.length());
    this._dialogBinding.filterTotalPrice.maximumTotalPrice.addTextChangedListener(
        this._maxPriceTextWatcher);
  }

  private class TotalPriceTextWatcher extends CurrencyTextWatcher {
    public TotalPriceTextWatcher(@NonNull EditText view) {
      super(view);
      this._maximumAmount = BigDecimal.valueOf(Long.MAX_VALUE);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);

      switch (this._view.getId()) {
        case R.id.minimumTotalPrice ->
            QueueFilterTotalPrice.this
                ._fragment
                .queueViewModel()
                .filterView()
                .onMinTotalPriceTextChanged(this.newText());

        case R.id.maximumTotalPrice ->
            QueueFilterTotalPrice.this
                ._fragment
                .queueViewModel()
                .filterView()
                .onMaxTotalPriceTextChanged(this.newText());
      }
    }
  }
}
