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
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createcustomer.viewmodel.CreateCustomerViewModel;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class CreateCustomerViewModelHandler {
  @NonNull protected final CreateCustomerFragment _fragment;
  @NonNull protected final CreateCustomerViewModel _viewModel;

  public CreateCustomerViewModelHandler(
      @NonNull CreateCustomerFragment fragment, @NonNull CreateCustomerViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .resultCreatedCustomerId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultCreatedCustomerId));
    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel
        .inputtedNameError()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedNameError);
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
        .inputtedBalanceAmountText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedBalanceAmountText);
    this._viewModel
        .balanceView()
        .inputtedWithdrawAmountText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedWithdrawAmountText);
    this._viewModel
        .balanceView()
        .availableBalanceToWithdraw()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onAvailableBalanceToWithdraw);
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultCreatedCustomerId(@NonNull Optional<Long> customerId) {
    Objects.requireNonNull(customerId);

    customerId.ifPresent(
        id -> {
          final Bundle bundle = new Bundle();
          bundle.putLong(CreateCustomerFragment.Result.CREATED_CUSTOMER_ID_LONG.key(), id);

          this._fragment
              .getParentFragmentManager()
              .setFragmentResult(CreateCustomerFragment.Request.CREATE_CUSTOMER.key(), bundle);
        });
    this._fragment.finish();
  }

  private void _onSnackbarMessage(@NonNull StringResources stringRes) {
    Objects.requireNonNull(stringRes);

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onInputtedNameError(@NonNull Optional<StringResources> stringRes) {
    Objects.requireNonNull(stringRes);

    this._fragment
        .inputName()
        .setError(
            stringRes
                .map(string -> StringResources.stringOf(this._fragment.requireContext(), string))
                .orElse(null));
  }

  private void _onInputtedNameText(@NonNull String name) {
    Objects.requireNonNull(name);

    this._fragment.inputName().setInputtedNameText(name);
  }

  private void _onInputtedBalance(long balance) {
    this._fragment.inputBalance().setInputtedBalance(balance);
  }

  private void _onInputtedDebt(@NonNull BigDecimal debt) {
    Objects.requireNonNull(debt);

    this._fragment.inputDebt().setInputtedDebt(debt);
  }

  private void _onInputtedBalanceAmountText(@NonNull String amount) {
    Objects.requireNonNull(amount);

    this._fragment.inputBalance().setInputtedBalanceAmountText(amount);
  }

  private void _onInputtedWithdrawAmountText(@NonNull String amount) {
    Objects.requireNonNull(amount);

    this._fragment.inputBalance().setInputtedWithdrawAmountText(amount);
  }

  private void _onAvailableBalanceToWithdraw(long amount) {
    this._fragment.inputBalance().setAvailableAmountToWithdraw(amount);
  }
}
