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

package com.robifr.ledger.ui.createcustomer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createcustomer.viewmodel.CreateCustomerViewModel;
import java.math.BigDecimal;
import java.util.Objects;

public class CreateCustomerViewModelHandler {
  @NonNull protected final CreateCustomerFragment _fragment;
  @NonNull protected final CreateCustomerViewModel _viewModel;

  public CreateCustomerViewModelHandler(
      @NonNull CreateCustomerFragment fragment, @NonNull CreateCustomerViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .snackbarMessage()
        .observe(this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onSnackbarMessage));
    this._viewModel
        .createdCustomerId()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onCreatedCustomerId));
    this._viewModel
        .inputtedNameError()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onInputtedNameError));
    this._viewModel
        .inputtedNameText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedNameText);
    this._viewModel
        .inputtedBalance()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedBalance);
    this._viewModel
        .inputtedDebt()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedDebt);

    this._viewModel
        .balanceView()
        .inputtedDepositAmountText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedDepositAmountText);
    this._viewModel
        .balanceView()
        .inputtedWithdrawAmountText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedWithdrawAmountText);
    this._viewModel
        .balanceView()
        .availableBalanceToWithdraw()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onAvailableBalanceToWithdraw);
  }

  private void _onSnackbarMessage(@Nullable StringResources stringRes) {
    if (stringRes == null) return;

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onCreatedCustomerId(@Nullable Long id) {
    if (id != null) {
      final Bundle bundle = new Bundle();
      bundle.putLong(CreateCustomerFragment.Result.CREATED_CUSTOMER_ID.key(), id);

      this._fragment
          .getParentFragmentManager()
          .setFragmentResult(CreateCustomerFragment.Request.CREATE_CUSTOMER.key(), bundle);
    }

    this._fragment.finish();
  }

  private void _onInputtedNameError(@Nullable StringResources stringRes) {
    final String text =
        stringRes != null
            ? StringResources.stringOf(this._fragment.requireContext(), stringRes)
            : null;
    this._fragment.inputName().setError(text);
  }

  private void _onInputtedNameText(@Nullable String name) {
    this._fragment.inputName().setInputtedNameText(this._viewModel.inputtedCustomer().name());
  }

  private void _onInputtedBalance(@Nullable Long balance) {
    if (balance != null) this._fragment.inputBalance().setInputtedBalance(balance);
  }

  private void _onInputtedDebt(@Nullable BigDecimal debt) {
    if (debt != null) this._fragment.inputDebt().setInputtedDebt(debt);
  }

  private void _onInputtedDepositAmountText(@Nullable String amount) {
    this._fragment.inputBalance().setInputtedDepositAmountText(amount);
  }

  private void _onInputtedWithdrawAmountText(@Nullable String amount) {
    this._fragment.inputBalance().setInputtedWithdrawAmountText(amount);
  }

  private void _onAvailableBalanceToWithdraw(@Nullable Long amount) {
    final long availableAmount = Objects.requireNonNullElse(amount, 0L);
    this._fragment.inputBalance().setAvailableAmountToWithdraw(availableAmount);
  }
}
