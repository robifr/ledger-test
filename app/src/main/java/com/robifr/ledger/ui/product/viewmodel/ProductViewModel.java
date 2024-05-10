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

package com.robifr.ledger.ui.product.viewmodel;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.ProductFilterer;
import com.robifr.ledger.data.ProductSortMethod;
import com.robifr.ledger.data.ProductSorter;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.LiveDataModelUpdater;
import com.robifr.ledger.ui.StringResources;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class ProductViewModel extends ViewModel {
  @NonNull private final ProductRepository _productRepository;
  @NonNull private final ProductUpdater _productUpdater;
  @NonNull private final ProductFilterViewModel _filterView;

  @NonNull private final ProductSorter _sorter = new ProductSorter();

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<ProductModel>> _products = new MutableLiveData<>();
  @NonNull private final MutableLiveData<ProductSortMethod> _sortMethod = new MutableLiveData<>();

  /**
   * Currently expanded product index from {@link ProductViewModel#_products products}. -1 or null
   * to represent none being expanded.
   */
  @NonNull private final MutableLiveData<Integer> _expandedProductIndex = new MutableLiveData<>();

  @Inject
  public ProductViewModel(@NonNull ProductRepository productRepository) {
    this._productRepository = Objects.requireNonNull(productRepository);
    this._productUpdater = new ProductUpdater(this._products);
    this._filterView = new ProductFilterViewModel(this, new ProductFilterer());

    this._productRepository.addModelChangedListener(this._productUpdater);

    // It's unusual indeed to call its own method in its constructor. Setting up initial values
    // inside a fragment is painful. You have to consider whether the fragment recreated due to
    // configuration changes, or if it's popped from the backstack, or when the view model itself
    // is recreated due to the fragment being navigated by bottom navigation.
    this.onSortMethodChanged(new ProductSortMethod(ProductSortMethod.SortBy.NAME, true));

    final LiveData<List<ProductModel>> selectAllProducts = this.selectAllProducts();
    selectAllProducts.observeForever(
        new Observer<>() {
          @Override
          public void onChanged(@Nullable List<ProductModel> products) {
            if (products != null) {
              ProductViewModel.this._filterView.onFiltersChanged(
                  ProductViewModel.this._filterView.inputtedFilters(), products);
            }

            selectAllProducts.removeObserver(this);
          }
        });
  }

  @Override
  public void onCleared() {
    this._productRepository.removeModelChangedListener(this._productUpdater);
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

  /**
   * @see ProductViewModel#_expandedProductIndex
   */
  public LiveData<Integer> expandedProductIndex() {
    return this._expandedProductIndex;
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

  public void deleteProduct(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    this._productRepository
        .delete(product)
        .thenAcceptAsync(
            effected -> {
              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_deleted_x_product, effected, effected)
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

  public void onExpandedProductIndexChanged(int index) {
    this._expandedProductIndex.setValue(index);
  }

  private class ProductUpdater extends LiveDataModelUpdater<ProductModel> {
    public ProductUpdater(@NonNull MutableLiveData<List<ProductModel>> products) {
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
