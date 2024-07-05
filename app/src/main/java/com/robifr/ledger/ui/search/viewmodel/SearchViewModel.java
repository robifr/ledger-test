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

package com.robifr.ledger.ui.search.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class SearchViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final ProductRepository _productRepository;
  @NonNull private final Handler _handler = new Handler(Looper.getMainLooper());

  @NonNull
  private final SafeMutableLiveData<Optional<List<CustomerModel>>> _customers =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull
  private final SafeMutableLiveData<Optional<List<ProductModel>>> _products =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull private String _query = "";

  @Inject
  public SearchViewModel(
      @NonNull CustomerRepository customerRepository,
      @NonNull ProductRepository productRepository) {
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._productRepository = Objects.requireNonNull(productRepository);
  }

  @NonNull
  public SafeLiveData<Optional<List<CustomerModel>>> customers() {
    return this._customers;
  }

  @NonNull
  public SafeLiveData<Optional<List<ProductModel>>> products() {
    return this._products;
  }

  @NonNull
  public String query() {
    return this._query;
  }

  public void onSearch(@NonNull String query) {
    this._query = Objects.requireNonNull(query);

    // Remove old runnable to ensure old query result wouldn't appear in future.
    this._handler.removeCallbacksAndMessages(null);
    this._handler.postDelayed(
        () -> {
          // Send null when user hasn't type anything to prevent
          // no-results-found illustration shows up.
          if (this._query.isEmpty()) {
            this._customers.postValue(Optional.empty());
            this._products.postValue(Optional.empty());
          } else {
            this._customerRepository
                .search(this._query)
                .thenAcceptAsync(customers -> this._customers.postValue(Optional.of(customers)));
            this._productRepository
                .search(this._query)
                .thenAcceptAsync(products -> this._products.postValue(Optional.of(products)));
          }
        },
        300);
  }
}
