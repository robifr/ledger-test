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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerFragment;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;

@HiltViewModel
public class SearchCustomerViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final Handler _handler = new Handler(Looper.getMainLooper());
  @NonNull private final String _initialQuery;
  @NonNull private final List<Long> _initialSelectedCustomerIds;

  /**
   * Whether the fragment should return {@link SearchCustomerFragment.Request#SELECT_CUSTOMER} on
   * back navigation.
   */
  private final boolean _isSelectionEnabled;

  @NonNull
  private final CustomerChangedListener _customerChangedListener =
      new CustomerChangedListener(this);

  @NonNull
  private final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final SafeMutableLiveData<Optional<List<CustomerModel>>> _customers =
      new SafeMutableLiveData<>(Optional.empty());

  /**
   * Currently expanded customer index from {@link SearchCustomerViewModel#_customers customers}. -1
   * to represent none being expanded.
   */
  @NonNull
  private final SafeMutableLiveData<Integer> _expandedCustomerIndex = new SafeMutableLiveData<>(-1);

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultSelectedCustomerId =
      new MutableLiveData<>();

  @Inject
  public SearchCustomerViewModel(
      @NonNull CustomerRepository customerRepository, @NonNull SavedStateHandle savedStateHandle) {
    Objects.requireNonNull(savedStateHandle);

    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._initialQuery =
        Objects.requireNonNullElse(
            savedStateHandle.get(SearchCustomerFragment.Arguments.INITIAL_QUERY_STRING.key()), "");
    this._initialSelectedCustomerIds =
        Arrays.stream(
                Objects.requireNonNullElse(
                    savedStateHandle.get(
                        SearchCustomerFragment.Arguments.INITIAL_SELECTED_CUSTOMER_IDS_LONG_ARRAY
                            .key()),
                    new long[] {}))
            .boxed()
            .collect(Collectors.toList());
    this._isSelectionEnabled =
        Objects.requireNonNullElse(
            savedStateHandle.get(
                SearchCustomerFragment.Arguments.IS_SELECTION_ENABLED_BOOLEAN.key()),
            false);

    this._customerRepository.addModelChangedListener(this._customerChangedListener);
  }

  @Override
  public void onCleared() {
    this._customerRepository.removeModelChangedListener(this._customerChangedListener);
  }

  @NonNull
  public String initialQuery() {
    return this._initialQuery;
  }

  @NonNull
  public List<Long> initialSelectedCustomerIds() {
    return this._initialSelectedCustomerIds;
  }

  /**
   * @see SearchCustomerViewModel#_isSelectionEnabled
   */
  public boolean isSelectionEnabled() {
    return this._isSelectionEnabled;
  }

  @NonNull
  public LiveData<SafeEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<Optional<List<CustomerModel>>> customers() {
    return this._customers;
  }

  /**
   * @see SearchCustomerViewModel#_expandedCustomerIndex
   */
  public SafeLiveData<Integer> expandedCustomerIndex() {
    return this._expandedCustomerIndex;
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

  public void onDeleteCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._customerRepository
        .delete(customer)
        .thenAcceptAsync(
            effected -> {
              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_deleted_x_customer, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_delete_customer);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }

  public void onCustomerSelected(@Nullable CustomerModel customer) {
    this._resultSelectedCustomerId.setValue(
        new SafeEvent<>(Optional.ofNullable(customer).map(CustomerModel::id)));
  }

  public void onExpandedCustomerIndexChanged(int index) {
    this._expandedCustomerIndex.setValue(index);
  }

  void _onCustomersChanged(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._customers.setValue(Optional.of(Collections.unmodifiableList(customers)));
  }
}
