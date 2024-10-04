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

package com.robifr.ledger.ui.createcustomer;

import android.content.DialogInterface;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.CreateCustomerDialogTransactionBinding;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import com.robifr.ledger.ui.createcustomer.viewmodel.CustomerBalanceViewModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class CreateCustomerBalance
    implements View.OnClickListener,
        DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener {
  @NonNull private final CreateCustomerFragment _fragment;

  @NonNull private final CreateCustomerDialogTransactionBinding _withdrawDialogBinding;
  @NonNull private final AlertDialog _withdrawDialog;
  @NonNull private final BalanceTextWatcher _withdrawTextWatcher;

  @NonNull private final CreateCustomerDialogTransactionBinding _addBalanceDialogBinding;
  @NonNull private final AlertDialog _addBalanceDialog;
  @NonNull private final BalanceTextWatcher _addBalanceTextWatcher;

  public CreateCustomerBalance(@NonNull CreateCustomerFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._withdrawDialogBinding =
        CreateCustomerDialogTransactionBinding.inflate(this._fragment.getLayoutInflater());
    this._withdrawDialog =
        new MaterialAlertDialogBuilder(this._fragment.requireContext())
            .setView(this._withdrawDialogBinding.getRoot())
            .setNegativeButton(R.string.action_cancel, this)
            .setPositiveButton(R.string.action_withdraw, this)
            .create();
    this._withdrawTextWatcher = new BalanceTextWatcher(this._withdrawDialogBinding.amount);

    this._addBalanceDialogBinding =
        CreateCustomerDialogTransactionBinding.inflate(this._fragment.getLayoutInflater());
    this._addBalanceDialog =
        new MaterialAlertDialogBuilder(this._fragment.requireContext())
            .setView(this._addBalanceDialogBinding.getRoot())
            .setNegativeButton(R.string.action_cancel, this)
            .setPositiveButton(R.string.action_add, this)
            .create();
    this._addBalanceTextWatcher = new BalanceTextWatcher(this._addBalanceDialogBinding.amount);

    this._fragment.fragmentBinding().withdrawButton.setOnClickListener(this);
    this._withdrawDialog.setOnDismissListener(this);
    this._withdrawDialogBinding.title.setText(R.string.createCustomer_balance_withdraw);
    this._withdrawDialogBinding.amount.addTextChangedListener(this._withdrawTextWatcher);

    this._fragment.fragmentBinding().addBalanceButton.setOnClickListener(this);
    this._addBalanceDialog.setOnDismissListener(this);
    this._addBalanceDialogBinding.title.setText(R.string.createCustomer_balance_add);
    this._addBalanceDialogBinding.amount.addTextChangedListener(this._addBalanceTextWatcher);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.withdrawButton -> {
        this._fragment
            .createCustomerViewModel()
            .balanceView()
            .setAvailableBalanceToWithdraw(
                this._fragment.createCustomerViewModel().inputtedCustomer().balance());
        this._withdrawDialog.show();
      }

      case R.id.addBalanceButton -> this._addBalanceDialog.show();
    }
  }

  @Override
  public void onClick(@NonNull DialogInterface dialog, int buttonType) {
    Objects.requireNonNull(dialog);

    switch (buttonType) {
      case DialogInterface.BUTTON_POSITIVE -> {
        if (dialog.equals(this._withdrawDialog)) {
          this._fragment.createCustomerViewModel().balanceView().onWithdrawSubmitted();

        } else if (dialog.equals(this._addBalanceDialog)) {
          this._fragment.createCustomerViewModel().balanceView().onAddSubmitted();
        }

        dialog.dismiss();
      }

      case DialogInterface.BUTTON_NEGATIVE -> dialog.dismiss();
    }
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    Objects.requireNonNull(dialog);

    this._fragment.createCustomerViewModel().balanceView().onReset();

    if (this._withdrawDialog.getCurrentFocus() != null) {
      this._withdrawDialog.getCurrentFocus().clearFocus();
    }

    if (this._addBalanceDialog.getCurrentFocus() != null) {
      this._addBalanceDialog.getCurrentFocus().clearFocus();
    }
  }

  public void setInputtedBalance(long balance) {
    final String formattedBalance =
        CurrencyFormat.format(
            BigDecimal.valueOf(balance),
            AppCompatDelegate.getApplicationLocales().toLanguageTags());
    this._fragment.fragmentBinding().balance.setText(formattedBalance);

    // Disable withdraw button when the balance is zero.
    final boolean isBalanceAboveZero = BigDecimal.valueOf(balance).compareTo(BigDecimal.ZERO) > 0;
    this._fragment.fragmentBinding().withdrawButton.setEnabled(isBalanceAboveZero);

    // Disable button to add balance when the balance is above or equals maximum limit.
    final boolean isBalanceBelowLimit =
        BigDecimal.valueOf(balance).compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) < 0;
    this._fragment.fragmentBinding().addBalanceButton.setEnabled(isBalanceBelowLimit);
  }

  /**
   * @param amount Formatted text of balance amount.
   */
  public void setInputtedBalanceAmountText(@NonNull String amount) {
    Objects.requireNonNull(amount);

    final String currentText = this._addBalanceDialogBinding.amount.getText().toString();
    if (currentText.equals(amount)) return;

    // Remove listener to prevent any sort of formatting.
    this._addBalanceDialogBinding.amount.removeTextChangedListener(this._addBalanceTextWatcher);
    this._addBalanceDialogBinding.amount.setText(amount);
    this._addBalanceDialogBinding.amount.setSelection(amount.length());
    this._addBalanceDialogBinding.amount.addTextChangedListener(this._addBalanceTextWatcher);
  }

  /**
   * @param amount Formatted text of withdraw amount.
   */
  public void setInputtedWithdrawAmountText(@NonNull String amount) {
    Objects.requireNonNull(amount);

    final String currentText = this._withdrawDialogBinding.amount.getText().toString();
    if (currentText.equals(amount)) return;

    // Remove listener to prevent any sort of formatting.
    this._withdrawDialogBinding.amount.removeTextChangedListener(this._withdrawTextWatcher);
    this._withdrawDialogBinding.amount.setText(amount);
    this._withdrawDialogBinding.amount.setSelection(amount.length());
    this._withdrawDialogBinding.amount.addTextChangedListener(this._withdrawTextWatcher);
  }

  public void setAvailableAmountToWithdraw(long amount) {
    this._withdrawDialogBinding.amountLayout.setHelperText(
        this._fragment.getString(
            R.string.createCustomer_balance_withdraw_n_available,
            CurrencyFormat.format(
                BigDecimal.valueOf(amount),
                AppCompatDelegate.getApplicationLocales().toLanguageTags())));
  }

  private class BalanceTextWatcher extends CurrencyTextWatcher {
    public BalanceTextWatcher(@NonNull EditText editText) {
      super(editText);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);

      final CustomerBalanceViewModel balanceViewModel =
          CreateCustomerBalance.this._fragment.createCustomerViewModel().balanceView();

      if (this._view == CreateCustomerBalance.this._addBalanceDialogBinding.amount) {
        balanceViewModel.onBalanceAmountTextChanged(this.newText());

      } else if (this._view == CreateCustomerBalance.this._withdrawDialogBinding.amount) {
        balanceViewModel.onWithdrawAmountTextChanged(this.newText());
      }
    }
  }
}
