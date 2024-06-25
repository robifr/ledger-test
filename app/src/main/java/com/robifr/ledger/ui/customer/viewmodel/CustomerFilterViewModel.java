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

package com.robifr.ledger.ui.customer.viewmodel;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.robifr.ledger.data.display.CustomerFilterer;
import com.robifr.ledger.data.display.CustomerFilters;
import com.robifr.ledger.data.display.CustomerSortMethod;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomerFilterViewModel {
  @NonNull private final CustomerViewModel _viewModel;
  @NonNull private final CustomerFilterer _filterer;
  @NonNull private final MutableLiveData<String> _inputtedMinBalanceText = new MutableLiveData<>();
  @NonNull private final MutableLiveData<String> _inputtedMaxBalanceText = new MutableLiveData<>();
  @NonNull private final MutableLiveData<String> _inputtedMinDebtText = new MutableLiveData<>();
  @NonNull private final MutableLiveData<String> _inputtedMaxDebtText = new MutableLiveData<>();

  public CustomerFilterViewModel(
      @NonNull CustomerViewModel viewModel, @NonNull CustomerFilterer filterer) {
    this._viewModel = Objects.requireNonNull(viewModel);
    this._filterer = Objects.requireNonNull(filterer);
  }

  @NonNull
  public LiveData<String> inputtedMinBalanceText() {
    return this._inputtedMinBalanceText;
  }

  @NonNull
  public LiveData<String> inputtedMaxBalanceText() {
    return this._inputtedMaxBalanceText;
  }

  @NonNull
  public LiveData<String> inputtedMinDebtText() {
    return this._inputtedMinDebtText;
  }

  @NonNull
  public LiveData<String> inputtedMaxDebtText() {
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
    Long minBalance = this._filterer.filters().filteredBalance().first;
    Long maxBalance = this._filterer.filters().filteredBalance().second;
    BigDecimal minDebt = this._filterer.filters().filteredDebt().first;
    BigDecimal maxDebt = this._filterer.filters().filteredDebt().second;

    try {
      final String minBalanceText = this._inputtedMinBalanceText.getValue();

      if (minBalanceText != null && !minBalanceText.isBlank()) {
        minBalance = CurrencyFormat.parse(minBalanceText, "id", "ID").longValue();
      }

    } catch (ParseException ignore) {
    }

    try {
      final String maxBalanceText = this._inputtedMaxBalanceText.getValue();

      if (maxBalanceText != null && !maxBalanceText.isBlank()) {
        maxBalance = CurrencyFormat.parse(maxBalanceText, "id", "ID").longValue();
      }

    } catch (ParseException ignore) {
    }

    try {
      final String minDebtText = this._inputtedMinDebtText.getValue();

      if (minDebtText != null && !minDebtText.isBlank()) {
        minDebt = CurrencyFormat.parse(minDebtText, "id", "ID");
      }

    } catch (ParseException ignore) {
    }

    try {
      final String maxDebtText = this._inputtedMaxDebtText.getValue();

      if (maxDebtText != null && !maxDebtText.isBlank()) {
        maxDebt = CurrencyFormat.parse(maxDebtText, "id", "ID");
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
    final List<CustomerModel> customers =
        Objects.requireNonNullElse(this._viewModel.customers().getValue(), new ArrayList<>());
    this.onFiltersChanged(filters, customers);
  }

  public void onFiltersChanged(
      @NonNull CustomerFilters filters, @NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(filters);
    Objects.requireNonNull(customers);

    final String minBalance =
        filters.filteredBalance().first != null
            ? CurrencyFormat.format(BigDecimal.valueOf(filters.filteredBalance().first), "id", "ID")
            : "";
    final String maxBalance =
        filters.filteredBalance().second != null
            ? CurrencyFormat.format(
                BigDecimal.valueOf(filters.filteredBalance().second), "id", "ID")
            : "";
    final String minDebt =
        filters.filteredDebt().first != null
            ? CurrencyFormat.format(filters.filteredDebt().first, "id", "ID")
            : "";
    final String maxDebt =
        filters.filteredDebt().second != null
            ? CurrencyFormat.format(filters.filteredDebt().second, "id", "ID")
            : "";

    this.onMinBalanceTextChanged(minBalance);
    this.onMaxBalanceTextChanged(maxBalance);
    this.onMinDebtTextChanged(minDebt);
    this.onMaxDebtTextChanged(maxDebt);
    this._filterer.setFilters(filters);

    final List<CustomerModel> filteredCustomers = this._filterer.filter(customers);
    final CustomerSortMethod sortMethod = this._viewModel.sortMethod().getValue();

    // Re-sort the list, after previously re-populating the list with a new filtered value.
    if (sortMethod != null) this._viewModel.onSortMethodChanged(sortMethod, filteredCustomers);
    // Put in the else to prevent multiple call of `CustomerFilterViewModel#onCustomersChanged()`
    // from `CustomerFilterViewModel#onSortMethodChanged()`.
    else this._viewModel.onCustomersChanged(filteredCustomers);
  }
}
