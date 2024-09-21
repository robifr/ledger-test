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

package com.robifr.ledger.ui.searchproduct.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.searchproduct.SearchProductFragment;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class SearchProductViewModel extends ViewModel {
  @NonNull private final ProductRepository _productRepository;
  @NonNull private final Handler _handler = new Handler(Looper.getMainLooper());
  @NonNull private final String _initialQuery;

  @NonNull
  private final SafeMutableLiveData<Optional<List<ProductModel>>> _products =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultSelectedProductId =
      new MutableLiveData<>();

  @Inject
  public SearchProductViewModel(
      @NonNull ProductRepository productRepository, @NonNull SavedStateHandle savedStateHandle) {
    Objects.requireNonNull(savedStateHandle);

    this._productRepository = Objects.requireNonNull(productRepository);
    this._initialQuery =
        Objects.requireNonNullElse(
            savedStateHandle.get(SearchProductFragment.Arguments.INITIAL_QUERY_STRING.key()), "");
  }

  @NonNull
  public String initialQuery() {
    return this._initialQuery;
  }

  @NonNull
  public SafeLiveData<Optional<List<ProductModel>>> products() {
    return this._products;
  }

  @NonNull
  public LiveData<SafeEvent<Optional<Long>>> resultSelectedProductId() {
    return this._resultSelectedProductId;
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
            this._products.postValue(Optional.empty());
          } else {
            this._productRepository
                .search(query)
                .thenAcceptAsync(products -> this._products.postValue(Optional.of(products)));
          }
        },
        300);
  }

  public void onProductSelected(@Nullable ProductModel product) {
    this._resultSelectedProductId.setValue(
        new SafeEvent<>(Optional.ofNullable(product).map(ProductModel::id)));
  }
}
