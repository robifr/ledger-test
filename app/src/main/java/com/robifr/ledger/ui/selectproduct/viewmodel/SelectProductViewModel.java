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

  @NonNull
  public LiveData<SafeEvent<Optional<Long>>> resultSelectedProductId() {
    return this._resultSelectedProductId;
  }

  public void onProductSelected(@Nullable ProductModel product) {
    this._resultSelectedProductId.setValue(
        new SafeEvent<>(Optional.ofNullable(product).map(ProductModel::id)));
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
