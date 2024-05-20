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

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.ProductSortMethod;
import com.robifr.ledger.data.ProductSorter;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.LiveDataModelChangedListener;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.selectproduct.SelectProductFragment;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class SelectProductViewModel extends ViewModel {
  @NonNull private final ProductRepository _productRepository;
  @NonNull private final ProductChangedListener _productChangedListener;
  @NonNull private final ProductSorter _sorter = new ProductSorter();

  @Nullable private final ProductModel _initialSelectedProduct;

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<ProductModel>> _products = new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _resultSelectedProductId =
      new MutableLiveData<>();

  @Inject
  public SelectProductViewModel(
      @NonNull ProductRepository productRepository, @NonNull SavedStateHandle savedStateHandle) {
    Objects.requireNonNull(savedStateHandle);

    this._productRepository = Objects.requireNonNull(productRepository);
    this._productChangedListener = new ProductChangedListener(this._products);
    this._initialSelectedProduct =
        savedStateHandle.get(SelectProductFragment.Arguments.INITIAL_SELECTED_PRODUCT.key());

    this._sorter.setSortMethod(new ProductSortMethod(ProductSortMethod.SortBy.NAME, true));
    this._productRepository.addModelChangedListener(this._productChangedListener);

    // It's unusual indeed to call its own method in its constructor. Setting up initial values
    // inside a fragment is painful. You have to consider whether the fragment recreated due to
    // configuration changes, or if it's popped from the backstack, or when the view model itself
    // is recreated due to the fragment being navigated by bottom navigation.
    final LiveData<List<ProductModel>> selectAllProducts = this.selectAllProducts();
    selectAllProducts.observeForever(
        new Observer<>() {
          @Override
          public void onChanged(@Nullable List<ProductModel> products) {
            if (products != null) SelectProductViewModel.this.onProductsChanged(products);
            selectAllProducts.removeObserver(this);
          }
        });
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
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<List<ProductModel>> products() {
    return this._products;
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> resultSelectedProductId() {
    return this._resultSelectedProductId;
  }

  public void onProductsChanged(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._products.setValue(Collections.unmodifiableList(this._sorter.sort(products)));
  }

  public void onProductSelected(@Nullable ProductModel product) {
    final Long productId = product != null && product.id() != null ? product.id() : null;
    this._resultSelectedProductId.setValue(new LiveDataEvent<>(productId));
  }

  @NonNull
  public LiveData<List<ProductModel>> selectAllProducts() {
    final MutableLiveData<List<ProductModel>> result = new MutableLiveData<>();

    this._productRepository
        .selectAll()
        .thenAcceptAsync(
            products -> {
              if (products == null) {
                this._snackbarMessage.postValue(
                    new LiveDataEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_unable_to_retrieve_all_products)));
              }

              result.postValue(products);
            });
    return result;
  }

  private class ProductChangedListener extends LiveDataModelChangedListener<ProductModel> {
    public ProductChangedListener(@NonNull MutableLiveData<List<ProductModel>> products) {
      super(products);
    }

    @Override
    @MainThread
    public void onUpdateLiveData(@NonNull List<ProductModel> products) {
      SelectProductViewModel.this.onProductsChanged(products);
    }
  }
}
