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

package com.robifr.ledger.ui.main.customer.filter;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.robifr.ledger.R;
import com.robifr.ledger.data.CustomerFilters;
import com.robifr.ledger.databinding.CustomerDialogFilterBinding;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import com.robifr.ledger.ui.main.customer.CustomerFragment;
import java.math.BigDecimal;
import java.util.Objects;

public class CustomerFilterDebt {
  @NonNull private final CustomerFragment _fragment;
  @NonNull private final CustomerDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;
  @NonNull private final DebtTextWatcher _minDebtTextWatcher;
  @NonNull private final DebtTextWatcher _maxDebtTextWatcher;

  public CustomerFilterDebt(
      @NonNull CustomerFragment fragment,
      @NonNull CustomerDialogFilterBinding dialogBinding,
      @NonNull BottomSheetDialog dialog) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = Objects.requireNonNull(dialogBinding);
    this._dialog = Objects.requireNonNull(dialog);

    final TextInputEditText minDebt = this._dialogBinding.filterDebt.minimumDebt;
    final TextInputEditText maxDebt = this._dialogBinding.filterDebt.maximumDebt;
    this._minDebtTextWatcher = new DebtTextWatcher(minDebt, "id", "ID");
    this._maxDebtTextWatcher = new DebtTextWatcher(maxDebt, "id", "ID");

    minDebt.addTextChangedListener(this._minDebtTextWatcher);
    maxDebt.addTextChangedListener(this._maxDebtTextWatcher);
  }

  /**
   * @param minDebt Formatted text of min {@link CustomerFilters#filteredDebt() debt}.
   */
  public void setFilteredMinDebtText(@Nullable String minDebt) {
    final String currentText = this._dialogBinding.filterDebt.minimumDebt.getText().toString();
    if (currentText.equals(minDebt)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterDebt.minimumDebt.removeTextChangedListener(this._minDebtTextWatcher);
    this._dialogBinding.filterDebt.minimumDebt.setText(minDebt);
    this._dialogBinding.filterDebt.minimumDebt.addTextChangedListener(this._minDebtTextWatcher);
  }

  /**
   * @param maxDebt Formatted text of max {@link CustomerFilters#filteredDebt() debt}.
   */
  public void setFilteredMaxDebtText(@Nullable String maxDebt) {
    final String currentText = this._dialogBinding.filterDebt.maximumDebt.getText().toString();
    if (currentText.equals(maxDebt)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterDebt.maximumDebt.removeTextChangedListener(this._maxDebtTextWatcher);
    this._dialogBinding.filterDebt.maximumDebt.setText(maxDebt);
    this._dialogBinding.filterDebt.maximumDebt.addTextChangedListener(this._maxDebtTextWatcher);
  }

  private class DebtTextWatcher extends CurrencyTextWatcher {
    public DebtTextWatcher(
        @NonNull EditText view, @NonNull String language, @NonNull String country) {
      super(view, language, country);
      this._maximumAmount = BigDecimal.valueOf(Long.MAX_VALUE);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);

      switch (this._view.getId()) {
        case R.id.minimumDebt ->
            CustomerFilterDebt.this
                ._fragment
                .customerViewModel()
                .filterView()
                .onMinDebtTextChanged(this.newText());

        case R.id.maximumDebt ->
            CustomerFilterDebt.this
                ._fragment
                .customerViewModel()
                .filterView()
                .onMaxDebtTextChanged(this.newText());
      }
    }
  }
}
