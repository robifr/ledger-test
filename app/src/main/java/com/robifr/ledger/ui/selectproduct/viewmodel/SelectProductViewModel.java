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

import android.content.Context;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.R;
import com.robifr.ledger.data.ProductSortMethod;
import com.robifr.ledger.data.ProductSorter;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.LiveDataModelUpdater;
import com.robifr.ledger.ui.StringResources;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SelectProductViewModel extends ViewModel {
  @NonNull private final ProductRepository _productRepository;
  @Nullable private final ProductModel _initialSelectedProduct;
  @NonNull private final ProductsUpdater _productsUpdater;
  @NonNull private final ProductSorter _sorter = new ProductSorter();

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _selectedProductId = new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<ProductModel>> _products = new MutableLiveData<>();

  public SelectProductViewModel(
      @NonNull ProductRepository productRepository, @Nullable ProductModel initialSelectedProduct) {
    this._productRepository = Objects.requireNonNull(productRepository);
    this._initialSelectedProduct = initialSelectedProduct;
    this._productsUpdater = new ProductsUpdater(this._products);

    this._sorter.setSortMethod(new ProductSortMethod(ProductSortMethod.SortBy.NAME, true));
    this._productRepository.addModelChangedListener(this._productsUpdater);
  }

  @Override
  public void onCleared() {
    this._productRepository.removeModelChangedListener(this._productsUpdater);
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
  public LiveData<LiveDataEvent<Long>> selectedProductId() {
    return this._selectedProductId;
  }

  @NonNull
  public LiveData<List<ProductModel>> products() {
    return this._products;
  }

  public void onProductsChanged(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._products.setValue(Collections.unmodifiableList(this._sorter.sort(products)));
  }

  public void onProductSelected(@Nullable ProductModel product) {
    final Long productId = product != null && product.id() != null ? product.id() : null;
    this._selectedProductId.setValue(new LiveDataEvent<>(productId));
  }

  @NonNull
  public List<ProductModel> fetchAllProducts() {
    try {
      return this._productRepository.selectAll().get();

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(
          new LiveDataEvent<>(
              new StringResources.Strings(R.string.text_error_unable_to_retrieve_all_customers)));
    }

    return new ArrayList<>();
  }

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Context _context;
    @Nullable private final ProductModel _initialSelectedProduct;

    public Factory(@NonNull Context context, @Nullable ProductModel initialSelectedProduct) {
      Objects.requireNonNull(context);

      this._context = context.getApplicationContext();
      this._initialSelectedProduct = initialSelectedProduct;
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> cls) {
      Objects.requireNonNull(cls);

      final SelectProductViewModel viewModel =
          new SelectProductViewModel(
              ProductRepository.instance(this._context), this._initialSelectedProduct);
      return Objects.requireNonNull(cls.cast(viewModel));
    }
  }

  private class ProductsUpdater extends LiveDataModelUpdater<ProductModel> {
    public ProductsUpdater(@NonNull MutableLiveData<List<ProductModel>> products) {
      super(products);
    }

    @Override
    @MainThread
    public void onUpdateLiveData(@NonNull List<ProductModel> products) {
      SelectProductViewModel.this.onProductsChanged(products);
    }
  }
}
