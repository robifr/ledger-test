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

package com.robifr.ledger.ui.customer.filter;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
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
  public void setFilteredMinBalanceText(@NonNull String minBalance) {
    Objects.requireNonNull(minBalance);

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
  public void setFilteredMaxBalanceText(@NonNull String maxBalance) {
    Objects.requireNonNull(maxBalance);

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
