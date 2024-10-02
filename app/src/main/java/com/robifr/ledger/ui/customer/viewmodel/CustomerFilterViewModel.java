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

package com.robifr.ledger.ui.customer.viewmodel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.robifr.ledger.data.display.CustomerFilterer;
import com.robifr.ledger.data.display.CustomerFilters;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.util.CurrencyFormat;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

public class CustomerFilterViewModel {
  @NonNull private final CustomerViewModel _viewModel;
  @NonNull private final CustomerFilterer _filterer;

  @NonNull
  private final SafeMutableLiveData<String> _inputtedMinBalanceText = new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<String> _inputtedMaxBalanceText = new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<String> _inputtedMinDebtText = new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<String> _inputtedMaxDebtText = new SafeMutableLiveData<>("");

  public CustomerFilterViewModel(
      @NonNull CustomerViewModel viewModel, @NonNull CustomerFilterer filterer) {
    this._viewModel = Objects.requireNonNull(viewModel);
    this._filterer = Objects.requireNonNull(filterer);
  }

  @NonNull
  public SafeLiveData<String> inputtedMinBalanceText() {
    return this._inputtedMinBalanceText;
  }

  @NonNull
  public SafeLiveData<String> inputtedMaxBalanceText() {
    return this._inputtedMaxBalanceText;
  }

  @NonNull
  public SafeLiveData<String> inputtedMinDebtText() {
    return this._inputtedMinDebtText;
  }

  @NonNull
  public SafeLiveData<String> inputtedMaxDebtText() {
    return this._inputtedMaxDebtText;
  }

  /**
   * Get current inputted filter from any corresponding inputted live data. If any live data is set
   * using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public CustomerFilters inputtedFilters() {
    // Nullable value to represent unbounded range.
    Long minBalance = null;
    Long maxBalance = null;
    BigDecimal minDebt = null;
    BigDecimal maxDebt = null;

    try {
      if (!this._inputtedMinBalanceText.getValue().isBlank()) {
        minBalance =
            CurrencyFormat.parse(
                    this._inputtedMinBalanceText.getValue(),
                    AppCompatDelegate.getApplicationLocales().toLanguageTags())
                .longValue();
      }

    } catch (ParseException ignore) {
    }

    try {
      if (!this._inputtedMaxBalanceText.getValue().isBlank()) {
        maxBalance =
            CurrencyFormat.parse(
                    this._inputtedMaxBalanceText.getValue(),
                    AppCompatDelegate.getApplicationLocales().toLanguageTags())
                .longValue();
      }

    } catch (ParseException ignore) {
    }

    try {
      if (!this._inputtedMinDebtText.getValue().isBlank()) {
        minDebt =
            CurrencyFormat.parse(
                this._inputtedMinDebtText.getValue(),
                AppCompatDelegate.getApplicationLocales().toLanguageTags());
      }

    } catch (ParseException ignore) {
    }

    try {
      if (!this._inputtedMaxDebtText.getValue().isBlank()) {
        maxDebt =
            CurrencyFormat.parse(
                this._inputtedMaxDebtText.getValue(),
                AppCompatDelegate.getApplicationLocales().toLanguageTags());
      }

    } catch (ParseException ignore) {
    }

    return CustomerFilters.toBuilder()
        .setFilteredBalance(new Pair<>(minBalance, maxBalance))
        .setFilteredDebt(new Pair<>(minDebt, maxDebt))
        .build();
  }

  public void onMinBalanceTextChanged(@NonNull String minBalance) {
    Objects.requireNonNull(minBalance);

    this._inputtedMinBalanceText.setValue(minBalance);
  }

  public void onMaxBalanceTextChanged(@NonNull String maxBalance) {
    Objects.requireNonNull(maxBalance);

    this._inputtedMaxBalanceText.setValue(maxBalance);
  }

  public void onMinDebtTextChanged(@NonNull String minDebt) {
    Objects.requireNonNull(minDebt);

    this._inputtedMinDebtText.setValue(minDebt);
  }

  public void onMaxDebtTextChanged(@NonNull String maxDebt) {
    Objects.requireNonNull(maxDebt);

    this._inputtedMaxDebtText.setValue(maxDebt);
  }

  public void onFiltersChanged(@NonNull CustomerFilters filters) {
    this.onFiltersChanged(filters, this._viewModel.customers().getValue());
  }

  public void onFiltersChanged(
      @NonNull CustomerFilters filters, @NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(filters);
    Objects.requireNonNull(customers);

    final String minBalance =
        filters.filteredBalance().first != null
            ? CurrencyFormat.format(
                BigDecimal.valueOf(filters.filteredBalance().first),
                AppCompatDelegate.getApplicationLocales().toLanguageTags())
            : "";
    final String maxBalance =
        filters.filteredBalance().second != null
            ? CurrencyFormat.format(
                BigDecimal.valueOf(filters.filteredBalance().second),
                AppCompatDelegate.getApplicationLocales().toLanguageTags())
            : "";
    final String minDebt =
        filters.filteredDebt().first != null
            ? CurrencyFormat.format(
                filters.filteredDebt().first,
                AppCompatDelegate.getApplicationLocales().toLanguageTags())
            : "";
    final String maxDebt =
        filters.filteredDebt().second != null
            ? CurrencyFormat.format(
                filters.filteredDebt().second,
                AppCompatDelegate.getApplicationLocales().toLanguageTags())
            : "";

    this.onMinBalanceTextChanged(minBalance);
    this.onMaxBalanceTextChanged(maxBalance);
    this.onMinDebtTextChanged(minDebt);
    this.onMaxDebtTextChanged(maxDebt);
    this._filterer.setFilters(filters);

    final List<CustomerModel> filteredCustomers = this._filterer.filter(customers);
    // Re-sort the list, after previously re-populating the list with a new filtered value.
    this._viewModel.onSortMethodChanged(this._viewModel.sortMethod().getValue(), filteredCustomers);
  }
}
