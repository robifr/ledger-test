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

package com.robifr.ledger.ui.selectproduct.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.ProductSortMethod;
import com.robifr.ledger.data.display.ProductSorter;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.selectproduct.SelectProductFragment;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class SelectProductViewModel extends ViewModel {
  @NonNull private final ProductRepository _productRepository;

  @NonNull
  private final ProductChangedListener _productChangedListener = new ProductChangedListener(this);

  @NonNull private final ProductSorter _sorter = new ProductSorter();
  @Nullable private final ProductModel _initialSelectedProduct;

  @NonNull
  private final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final SafeMutableLiveData<List<ProductModel>> _products =
      new SafeMutableLiveData<>(List.of());

  /** Whether the preview of selected product is expanded or not. */
  @NonNull
  private final SafeMutableLiveData<Boolean> _isSelectedProductExpanded =
      new SafeMutableLiveData<>(false);

  /**
   * Currently expanded product index from {@link SelectProductViewModel#_products}. -1 to represent
   * none being expanded.
   */
  @NonNull
  private final SafeMutableLiveData<Integer> _expandedProductIndex = new SafeMutableLiveData<>(-1);

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultSelectedProductId =
      new MutableLiveData<>();

  @Inject
  public SelectProductViewModel(
      @NonNull ProductRepository productRepository, @NonNull SavedStateHandle savedStateHandle) {
    Objects.requireNonNull(savedStateHandle);

    this._productRepository = Objects.requireNonNull(productRepository);
    this._initialSelectedProduct =
        savedStateHandle.get(
            SelectProductFragment.Arguments.INITIAL_SELECTED_PRODUCT_PARCELABLE.key());

    this._sorter.setSortMethod(new ProductSortMethod(ProductSortMethod.SortBy.NAME, true));
    this._productRepository.addModelChangedListener(this._productChangedListener);

    // Setting up initial values inside a fragment is painful. See commit d5604599.
    SafeEvent.observeOnce(this._selectAllProducts(), this::_onProductsChanged, Objects::nonNull);
  }

  @Override
  public void onCleared() {
    this._productRepository.removeModelChangedListener(this._productChangedListener);
  }

  @Nullable
  public ProductModel initialSelectedProduct() {
    return this._initialSelectedProduct;
  }

  @NonNull
  public LiveData<SafeEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<List<ProductModel>> products() {
    return this._products;
  }

  /**
   * @see SelectProductViewModel#_isSelectedProductExpanded
   */
  public SafeLiveData<Boolean> isSelectedProductExpanded() {
    return this._isSelectedProductExpanded;
  }

  /**
   * @see SelectProductViewModel#_expandedProductIndex
   */
  public SafeLiveData<Integer> expandedProductIndex() {
    return this._expandedProductIndex;
  }

  @NonNull
  public LiveData<SafeEvent<Optional<Long>>> resultSelectedProductId() {
    return this._resultSelectedProductId;
  }

  public void onProductSelected(@Nullable ProductModel product) {
    this._resultSelectedProductId.setValue(
        new SafeEvent<>(Optional.ofNullable(product).map(ProductModel::id)));
  }

  public void onSelectedProductExpanded(boolean isExpanded) {
    this._isSelectedProductExpanded.setValue(isExpanded);
  }

  public void onExpandedProductIndexChanged(int index) {
    this._expandedProductIndex.setValue(index);
  }

  @NonNull
  private LiveData<List<ProductModel>> _selectAllProducts() {
    final MutableLiveData<List<ProductModel>> result = new MutableLiveData<>();

    this._productRepository
        .selectAll()
        .thenAcceptAsync(
            products -> {
              if (products == null) {
                this._snackbarMessage.postValue(
                    new SafeEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_unable_to_retrieve_all_products)));
              }

              result.postValue(products);
            });
    return result;
  }

  void _onProductsChanged(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._products.setValue(Collections.unmodifiableList(this._sorter.sort(products)));
  }
}
