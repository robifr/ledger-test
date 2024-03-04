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

package com.robifr.ledger.ui.main.product.viewmodel;

import android.content.Context;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
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

public class ProductViewModel extends ViewModel {
  @NonNull private final ProductRepository _productRepository;
  @NonNull private final ProductsUpdater _productsUpdater;
  @NonNull private final ProductFilterViewModel _filterView = new ProductFilterViewModel(this);
  @NonNull private final ProductSorter _sorter = new ProductSorter();

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<ProductModel>> _products = new MutableLiveData<>();
  @NonNull private final MutableLiveData<ProductSortMethod> _sortMethod = new MutableLiveData<>();

  public ProductViewModel(@NonNull ProductRepository productRepository) {
    this._productRepository = Objects.requireNonNull(productRepository);
    this._productsUpdater = new ProductsUpdater(this._products);

    this._productRepository.addModelChangedListener(this._productsUpdater);
  }

  @Override
  public void onCleared() {
    this._productRepository.removeModelChangedListener(this._productsUpdater);
  }

  @NonNull
  public ProductFilterViewModel filterView() {
    return this._filterView;
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
  public LiveData<ProductSortMethod> sortMethod() {
    return this._sortMethod;
  }

  @NonNull
  public List<ProductModel> fetchAllProducts() {
    try {
      return this._productRepository.selectAll().get();

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(
          new LiveDataEvent<>(
              new StringResources.Strings(R.string.text_error_unable_to_retrieve_all_products)));
    }

    return new ArrayList<>();
  }

  public void deleteProduct(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    this._productRepository
        .delete(product)
        .thenAcceptAsync(
            effected -> {
              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_product_deleted, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_delete_product);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
  }

  public void onProductsChanged(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._products.setValue(Collections.unmodifiableList(products));
  }

  public void onSortMethodChanged(@NonNull ProductSortMethod sortMethod) {
    final List<ProductModel> products =
        Objects.requireNonNullElse(this._products.getValue(), new ArrayList<>());
    this.onSortMethodChanged(sortMethod, products);
  }

  public void onSortMethodChanged(
      @NonNull ProductSortMethod sortMethod, @NonNull List<ProductModel> products) {
    Objects.requireNonNull(sortMethod);
    Objects.requireNonNull(products);

    this._sortMethod.setValue(sortMethod);
    this._sorter.setSortMethod(sortMethod);
    this.onProductsChanged(this._sorter.sort(products));
  }

  /**
   * @see ProductViewModel#onSortMethodChanged(ProductSortMethod.SortBy, List)
   */
  public void onSortMethodChanged(@NonNull ProductSortMethod.SortBy sortBy) {
    final List<ProductModel> products =
        Objects.requireNonNullElse(this._products.getValue(), new ArrayList<>());
    this.onSortMethodChanged(sortBy, products);
  }

  /**
   * Sort {@link ProductViewModel#products() products} based on specified {@link
   * ProductSortMethod.SortBy} type. Doing so will reverse the order — Ascending becomes descending
   * and vice versa. Use {@link ProductViewModel#onSortMethodChanged(ProductSortMethod)} if you want
   * to apply the order by yourself.
   */
  public void onSortMethodChanged(
      @NonNull ProductSortMethod.SortBy sortBy, @NonNull List<ProductModel> products) {
    Objects.requireNonNull(sortBy);

    final ProductSortMethod sortMethod = this._sortMethod.getValue();
    if (sortMethod == null) return;

    // Reverse sort order when selecting same sort option.
    final boolean isAscending =
        sortMethod.sortBy() == sortBy ? !sortMethod.isAscending() : sortMethod.isAscending();

    this.onSortMethodChanged(new ProductSortMethod(sortBy, isAscending), products);
  }

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Context _context;

    public Factory(@NonNull Context context) {
      Objects.requireNonNull(context);

      this._context = context.getApplicationContext();
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> cls) {
      Objects.requireNonNull(cls);

      final ProductViewModel viewModel =
          new ProductViewModel(ProductRepository.instance(this._context));
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
      ProductViewModel.this._filterView.onFiltersChanged(
          ProductViewModel.this._filterView.inputtedFilters(), products);
    }
  }
}
