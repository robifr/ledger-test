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

package com.robifr.ledger.ui.searchcustomer.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerFragment;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class SearchCustomerViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final Handler _handler = new Handler(Looper.getMainLooper());

  @NonNull
  private final MutableLiveData<SafeEvent<String>> _initializedInitialQuery =
      new MediatorLiveData<>();

  @NonNull
  private final SafeMutableLiveData<Optional<List<CustomerModel>>> _customers =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultSelectedCustomerId =
      new MutableLiveData<>();

  @Inject
  public SearchCustomerViewModel(
      @NonNull CustomerRepository customerRepository, @NonNull SavedStateHandle savedStateHandle) {
    Objects.requireNonNull(savedStateHandle);

    this._customerRepository = Objects.requireNonNull(customerRepository);

    this._initializedInitialQuery.setValue(
        new SafeEvent<>(
            Objects.requireNonNullElse(
                savedStateHandle.get(SearchCustomerFragment.Arguments.INITIAL_QUERY_STRING.key()),
                "")));
  }

  @NonNull
  public LiveData<SafeEvent<String>> initializedInitialQuery() {
    return this._initializedInitialQuery;
  }

  @NonNull
  public SafeLiveData<Optional<List<CustomerModel>>> customers() {
    return this._customers;
  }

  @NonNull
  public LiveData<SafeEvent<Optional<Long>>> resultSelectedCustomerId() {
    return this._resultSelectedCustomerId;
  }

  public void onSearch(@NonNull String query) {
    Objects.requireNonNull(query);

    // Remove old runnable to ensure old query result wouldn't appear in future.
    this._handler.removeCallbacksAndMessages(null);
    this._handler.postDelayed(
        () -> {
          // Send null when user hasn't type anything to prevent
          // no-results-found illustration shows up.
          if (query.isEmpty()) {
            this._customers.postValue(Optional.empty());
          } else {
            this._customerRepository
                .search(query)
                .thenAcceptAsync(customers -> this._customers.postValue(Optional.of(customers)));
          }
        },
        300);
  }

  public void onCustomerSelected(@Nullable CustomerModel customer) {
    this._resultSelectedCustomerId.setValue(
        new SafeEvent<>(Optional.ofNullable(customer).map(CustomerModel::id)));
  }
}
