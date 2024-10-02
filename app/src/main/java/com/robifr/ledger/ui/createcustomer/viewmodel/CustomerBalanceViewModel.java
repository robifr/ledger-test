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

package com.robifr.ledger.ui.createcustomer.viewmodel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.robifr.ledger.util.CurrencyFormat;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Objects;

public class CustomerBalanceViewModel {
  @NonNull private final CreateCustomerViewModel _createCustomerViewModel;

  @NonNull
  private final SafeMutableLiveData<String> _inputtedBalanceAmountText =
      new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<String> _inputtedWithdrawAmountText =
      new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<Long> _availableBalanceToWithdraw =
      new SafeMutableLiveData<>(0L);

  public CustomerBalanceViewModel(@NonNull CreateCustomerViewModel createCustomerViewModel) {
    this._createCustomerViewModel = Objects.requireNonNull(createCustomerViewModel);
  }

  @NonNull
  public SafeLiveData<String> inputtedBalanceAmountText() {
    return this._inputtedBalanceAmountText;
  }

  @NonNull
  public SafeLiveData<String> inputtedWithdrawAmountText() {
    return this._inputtedWithdrawAmountText;
  }

  @NonNull
  public SafeLiveData<Long> availableBalanceToWithdraw() {
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
      inputtedAmount =
          CurrencyFormat.parse(
                  this._inputtedBalanceAmountText.getValue(),
                  AppCompatDelegate.getApplicationLocales().toLanguageTags())
              .longValue();

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
      inputtedAmount =
          CurrencyFormat.parse(
                  this._inputtedWithdrawAmountText.getValue(),
                  AppCompatDelegate.getApplicationLocales().toLanguageTags())
              .longValue();

    } catch (ParseException ignore) {
    }

    return inputtedAmount;
  }

  public void onBalanceAmountTextChanged(@NonNull String amount) {
    Objects.requireNonNull(amount);

    BigDecimal amountToAdd = BigDecimal.ZERO;

    try {
      amountToAdd =
          CurrencyFormat.parse(amount, AppCompatDelegate.getApplicationLocales().toLanguageTags());

    } catch (ParseException ignore) {
    }

    final BigDecimal balanceAfter =
        BigDecimal.valueOf(this._createCustomerViewModel.inputtedBalance().getValue())
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
      amountToWithdraw =
          CurrencyFormat.parse(amount, AppCompatDelegate.getApplicationLocales().toLanguageTags());

    } catch (ParseException ignore) {
    }

    final BigDecimal balanceAfter =
        BigDecimal.valueOf(this._createCustomerViewModel.inputtedBalance().getValue())
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
        this._createCustomerViewModel.inputtedBalance().getValue() + this.inputtedBalanceAmount();
    this._createCustomerViewModel.onBalanceChanged(balance);
  }

  public void onWithdrawSubmitted() {
    final long balance =
        this._createCustomerViewModel.inputtedBalance().getValue() - this.inputtedWithdrawAmount();
    this._createCustomerViewModel.onBalanceChanged(balance);
  }

  public void onReset() {
    this._inputtedBalanceAmountText.setValue("");
    this._inputtedWithdrawAmountText.setValue("");
    this._availableBalanceToWithdraw.setValue(0L);
  }
}
