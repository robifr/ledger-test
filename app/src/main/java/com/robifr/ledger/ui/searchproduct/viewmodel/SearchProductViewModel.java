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

package com.robifr.ledger.ui.searchproduct.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.searchproduct.SearchProductFragment;
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
public class SearchProductViewModel extends ViewModel {
  @NonNull private final ProductRepository _productRepository;
  @NonNull private final Handler _handler = new Handler(Looper.getMainLooper());
  @NonNull private final String _initialQuery;
  @NonNull private final List<Long> _initialSelectedProductIds;

  /**
   * Whether the fragment should return {@link SearchProductFragment.Request#SELECT_PRODUCT} on back
   * navigation.
   */
  private final boolean _isSelectionEnabled;

  @NonNull
  private final ProductChangedListener _productChangedListener = new ProductChangedListener(this);

  @NonNull
  private final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final SafeMutableLiveData<Optional<List<ProductModel>>> _products =
      new SafeMutableLiveData<>(Optional.empty());

  /**
   * Currently expanded product index from {@link #_products}. -1 to represent none being expanded.
   */
  @NonNull
  private final SafeMutableLiveData<Integer> _expandedProductIndex = new SafeMutableLiveData<>(-1);

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
    this._initialSelectedProductIds =
        Arrays.stream(
                Objects.requireNonNullElse(
                    savedStateHandle.get(
                        SearchProductFragment.Arguments.INITIAL_SELECTED_PRODUCT_IDS_LONG_ARRAY
                            .key()),
                    new long[] {}))
            .boxed()
            .collect(Collectors.toList());
    this._isSelectionEnabled =
        Objects.requireNonNullElse(
            savedStateHandle.get(
                SearchProductFragment.Arguments.IS_SELECTION_ENABLED_BOOLEAN.key()),
            false);

    this._productRepository.addModelChangedListener(this._productChangedListener);
  }

  @Override
  public void onCleared() {
    this._productRepository.removeModelChangedListener(this._productChangedListener);
  }

  @NonNull
  public String initialQuery() {
    return this._initialQuery;
  }

  @NonNull
  public List<Long> initialSelectedProductIds() {
    return this._initialSelectedProductIds;
  }

  /**
   * @see #_isSelectionEnabled
   */
  public boolean isSelectionEnabled() {
    return this._isSelectionEnabled;
  }

  @NonNull
  public LiveData<SafeEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<Optional<List<ProductModel>>> products() {
    return this._products;
  }

  /**
   * @see #_expandedProductIndex
   */
  public SafeLiveData<Integer> expandedProductIndex() {
    return this._expandedProductIndex;
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

  public void onDeleteProduct(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    this._productRepository
        .delete(product)
        .thenAcceptAsync(
            effected -> {
              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.searchProduct_deleted_n_product, effected, effected)
                      : new StringResources.Strings(R.string.searchProduct_deleteProductError);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }

  public void onExpandedProductIndexChanged(int index) {
    this._expandedProductIndex.setValue(index);
  }

  public void onProductSelected(@Nullable ProductModel product) {
    this._resultSelectedProductId.setValue(
        new SafeEvent<>(Optional.ofNullable(product).map(ProductModel::id)));
  }

  void _onProductsChanged(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._products.setValue(Optional.of(Collections.unmodifiableList(products)));
  }
}
