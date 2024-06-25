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

package com.robifr.ledger.ui.customer.filter;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.CustomerFilters;
import com.robifr.ledger.databinding.CustomerDialogFilterBinding;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import com.robifr.ledger.ui.customer.CustomerFragment;
import java.util.Objects;

public class CustomerFilterBalance {
  @NonNull private final CustomerFragment _fragment;
  @NonNull private final CustomerDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;
  @NonNull private final BalanceTextWatcher _minBalanceTextWatcher;
  @NonNull private final BalanceTextWatcher _maxBalanceTextWatcher;

  public CustomerFilterBalance(
      @NonNull CustomerFragment fragment,
      @NonNull CustomerDialogFilterBinding dialogBinding,
      @NonNull BottomSheetDialog dialog) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = Objects.requireNonNull(dialogBinding);
    this._dialog = Objects.requireNonNull(dialog);

    final TextInputEditText minBalance = this._dialogBinding.filterBalance.minimumBalance;
    final TextInputEditText maxBalance = this._dialogBinding.filterBalance.maximumBalance;
    this._minBalanceTextWatcher = new BalanceTextWatcher(minBalance, "id", "ID");
    this._maxBalanceTextWatcher = new BalanceTextWatcher(maxBalance, "id", "ID");

    minBalance.addTextChangedListener(this._minBalanceTextWatcher);
    maxBalance.addTextChangedListener(this._maxBalanceTextWatcher);
  }

  /**
   * @param minBalance Formatted text of min {@link CustomerFilters#filteredBalance() balance}.
   */
  public void setFilteredMinBalanceText(@Nullable String minBalance) {
    final String currentText =
        this._dialogBinding.filterBalance.minimumBalance.getText().toString();
    if (currentText.equals(minBalance)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterBalance.minimumBalance.removeTextChangedListener(
        this._minBalanceTextWatcher);
    this._dialogBinding.filterBalance.minimumBalance.setText(minBalance);
    this._dialogBinding.filterBalance.minimumBalance.addTextChangedListener(
        this._minBalanceTextWatcher);
  }

  /**
   * @param maxBalance Formatted text of max {@link CustomerFilters#filteredBalance() balance}.
   */
  public void setFilteredMaxBalanceText(@Nullable String maxBalance) {
    final String currentText =
        this._dialogBinding.filterBalance.maximumBalance.getText().toString();
    if (currentText.equals(maxBalance)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.filterBalance.maximumBalance.removeTextChangedListener(
        this._maxBalanceTextWatcher);
    this._dialogBinding.filterBalance.maximumBalance.setText(maxBalance);
    this._dialogBinding.filterBalance.maximumBalance.addTextChangedListener(
        this._maxBalanceTextWatcher);
  }

  private class BalanceTextWatcher extends CurrencyTextWatcher {
    public BalanceTextWatcher(
        @NonNull EditText view, @NonNull String language, @NonNull String country) {
      super(view, language, country);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);

      switch (this._view.getId()) {
        case R.id.minimumBalance ->
            CustomerFilterBalance.this
                ._fragment
                .customerViewModel()
                .filterView()
                .onMinBalanceTextChanged(this.newText());

        case R.id.maximumBalance ->
            CustomerFilterBalance.this
                ._fragment
                .customerViewModel()
                .filterView()
                .onMaxBalanceTextChanged(this.newText());
      }
    }
  }
}
