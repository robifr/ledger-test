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
