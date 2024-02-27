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

package com.robifr.ledger.ui.main.queue.filter;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.robifr.ledger.R;
import com.robifr.ledger.data.QueueFilters;
import com.robifr.ledger.databinding.QueueDialogFilterBinding;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import com.robifr.ledger.ui.main.queue.QueueFragment;
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
    this._minPriceTextWatcher = new TotalPriceTextWatcher(minTotalPrice, "id", "ID");
    this._maxPriceTextWatcher = new TotalPriceTextWatcher(maxTotalPrice, "id", "ID");

    minTotalPrice.addTextChangedListener(this._minPriceTextWatcher);
    maxTotalPrice.addTextChangedListener(this._maxPriceTextWatcher);
  }

  /**
   * @param minTotalPrice Formatted text of min {@link QueueFilters#filteredTotalPrice() total
   *     price}.
   */
  public void setFilteredMinTotalPriceText(@Nullable String minTotalPrice) {
    final String currentText =
        this._dialogBinding.filterTotalPrice.minimumTotalPrice.getText().toString();
    if (currentText.equals(minTotalPrice)) return;

    final int cursorPosition = minTotalPrice != null ? minTotalPrice.length() : 0;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterTotalPrice.minimumTotalPrice.removeTextChangedListener(
        this._minPriceTextWatcher);
    this._dialogBinding.filterTotalPrice.minimumTotalPrice.setText(minTotalPrice);
    this._dialogBinding.filterTotalPrice.minimumTotalPrice.setSelection(cursorPosition);
    this._dialogBinding.filterTotalPrice.minimumTotalPrice.addTextChangedListener(
        this._minPriceTextWatcher);
  }

  /**
   * @param maxTotalPrice Formatted text of max {@link QueueFilters#filteredTotalPrice() total
   *     price}.
   */
  public void setFilteredMaxTotalPriceText(@Nullable String maxTotalPrice) {
    final String currentText =
        this._dialogBinding.filterTotalPrice.maximumTotalPrice.getText().toString();
    if (currentText.equals(maxTotalPrice)) return;

    final int cursorPosition = maxTotalPrice != null ? maxTotalPrice.length() : 0;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterTotalPrice.maximumTotalPrice.removeTextChangedListener(
        this._maxPriceTextWatcher);
    this._dialogBinding.filterTotalPrice.maximumTotalPrice.setText(maxTotalPrice);
    this._dialogBinding.filterTotalPrice.maximumTotalPrice.setSelection(cursorPosition);
    this._dialogBinding.filterTotalPrice.maximumTotalPrice.addTextChangedListener(
        this._maxPriceTextWatcher);
  }

  private class TotalPriceTextWatcher extends CurrencyTextWatcher {
    public TotalPriceTextWatcher(
        @NonNull EditText view, @NonNull String language, @NonNull String country) {
      super(view, language, country);
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
