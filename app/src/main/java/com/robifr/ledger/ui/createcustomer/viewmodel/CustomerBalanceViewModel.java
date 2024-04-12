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

package com.robifr.ledger.ui.createcustomer.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Objects;

public class CustomerBalanceViewModel {
  @NonNull private final CreateCustomerViewModel _createCustomerViewModel;

  @NonNull
  private final MutableLiveData<String> _inputtedBalanceAmountText = new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<String> _inputtedWithdrawAmountText = new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<Long> _availableBalanceToWithdraw = new MutableLiveData<>();

  public CustomerBalanceViewModel(@NonNull CreateCustomerViewModel createCustomerViewModel) {
    this._createCustomerViewModel = Objects.requireNonNull(createCustomerViewModel);
  }

  @NonNull
  public LiveData<String> inputtedBalanceAmountText() {
    return this._inputtedBalanceAmountText;
  }

  @NonNull
  public LiveData<String> inputtedWithdrawAmountText() {
    return this._inputtedWithdrawAmountText;
  }

  @NonNull
  public LiveData<Long> availableBalanceToWithdraw() {
    return this._availableBalanceToWithdraw;
  }

  public void setAvailableBalanceToWithdraw(long availableBalance) {
    this._availableBalanceToWithdraw.setValue(availableBalance);
  }

  /**
   * Get current inputted balance amount from any corresponding inputted live data. If any live data
   * is set using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  public long inputtedBalanceAmount() {
    long inputtedAmount = 0L;

    try {
      final String text =
          Objects.requireNonNullElse(this._inputtedBalanceAmountText.getValue(), "");
      inputtedAmount = CurrencyFormat.parse(text, "id", "ID").longValue();

    } catch (ParseException ignore) {
    }

    return inputtedAmount;
  }

  /**
   * Get current inputted withdraw amount from any corresponding inputted live data. If any live
   * data is set using {@link MutableLiveData#postValue(Object)}, calling this method may not
   * immediately reflect the latest changes. For accurate results in asynchronous operations,
   * consider calling this method inside {@link Observer}.
   */
  public long inputtedWithdrawAmount() {
    long inputtedAmount = 0L;

    try {
      final String text =
          Objects.requireNonNullElse(this._inputtedWithdrawAmountText.getValue(), "");
      inputtedAmount = CurrencyFormat.parse(text, "id", "ID").longValue();

    } catch (ParseException ignore) {
    }

    return inputtedAmount;
  }

  public void onBalanceAmountTextChanged(@NonNull String amount) {
    Objects.requireNonNull(amount);

    BigDecimal amountToAdd = BigDecimal.ZERO;

    try {
      amountToAdd = CurrencyFormat.parse(amount, "id", "ID");

    } catch (ParseException ignore) {
    }

    final BigDecimal balanceAfter =
        BigDecimal.valueOf(this._createCustomerViewModel.inputtedCustomer().balance())
            .add(amountToAdd);

    // Revert back when trying to add larger than maximum allowed.
    final String balanceAmountText =
        balanceAfter.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0
            ? this._inputtedBalanceAmountText.getValue()
            : amount;
    this._inputtedBalanceAmountText.setValue(balanceAmountText);
  }

  public void onWithdrawAmountTextChanged(@NonNull String amount) {
    Objects.requireNonNull(amount);

    BigDecimal amountToWithdraw = BigDecimal.ZERO;

    try {
      amountToWithdraw = CurrencyFormat.parse(amount, "id", "ID");

    } catch (ParseException ignore) {
    }

    final BigDecimal balanceAfter =
        BigDecimal.valueOf(this._createCustomerViewModel.inputtedCustomer().balance())
            .subtract(amountToWithdraw);
    final boolean isLeftOverBalanceAvailable = balanceAfter.compareTo(BigDecimal.ZERO) >= 0;

    // Revert back when when there's no more available balance to withdraw.
    final String withdrawAmountText =
        isLeftOverBalanceAvailable ? amount : this._inputtedWithdrawAmountText.getValue();
    final Long availableBalance =
        isLeftOverBalanceAvailable
            ? (Long) balanceAfter.longValue()
            : this._availableBalanceToWithdraw.getValue();

    this._inputtedWithdrawAmountText.setValue(withdrawAmountText);
    this._availableBalanceToWithdraw.setValue(availableBalance);
  }

  public void onAddSubmitted() {
    final long balance =
        this._createCustomerViewModel.inputtedCustomer().balance() + this.inputtedBalanceAmount();
    this._createCustomerViewModel.onBalanceChanged(balance);
  }

  public void onWithdrawSubmitted() {
    final long balance =
        this._createCustomerViewModel.inputtedCustomer().balance() - this.inputtedWithdrawAmount();
    this._createCustomerViewModel.onBalanceChanged(balance);
  }

  public void onReset() {
    this._inputtedBalanceAmountText.setValue(null);
    this._inputtedWithdrawAmountText.setValue(null);
    this._availableBalanceToWithdraw.setValue(null);
  }
}
