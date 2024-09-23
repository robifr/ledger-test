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

package com.robifr.ledger.ui.product.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.ProductFilterer;
import com.robifr.ledger.data.display.ProductSortMethod;
import com.robifr.ledger.data.display.ProductSorter;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class ProductViewModel extends ViewModel {
  @NonNull private final ProductRepository _productRepository;

  @NonNull
  private final ProductChangedListener _productChangedListener = new ProductChangedListener(this);

  @NonNull private final ProductFilterViewModel _filterView;
  @NonNull private final ProductSorter _sorter = new ProductSorter();

  @NonNull
  private final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final SafeMutableLiveData<List<ProductModel>> _products =
      new SafeMutableLiveData<>(List.of());

  @NonNull
  private final SafeMutableLiveData<ProductSortMethod> _sortMethod =
      new SafeMutableLiveData<>(new ProductSortMethod(ProductSortMethod.SortBy.NAME, true));

  /**
   * Currently expanded product index from {@link ProductViewModel#_products products}. -1 to
   * represent none being expanded.
   */
  @NonNull
  private final SafeMutableLiveData<Integer> _expandedProductIndex = new SafeMutableLiveData<>(-1);

  @Inject
  public ProductViewModel(@NonNull ProductRepository productRepository) {
    this._productRepository = Objects.requireNonNull(productRepository);
    this._filterView = new ProductFilterViewModel(this, new ProductFilterer());

    this._productRepository.addModelChangedListener(this._productChangedListener);

    // Setting up initial values inside a fragment is painful. See commit d5604599.
    SafeEvent.observeOnce(
        this.selectAllProducts(),
        products -> this._filterView.onFiltersChanged(this._filterView.inputtedFilters(), products),
        Objects::nonNull);
  }

  @Override
  public void onCleared() {
    this._productRepository.removeModelChangedListener(this._productChangedListener);
  }

  @NonNull
  public ProductFilterViewModel filterView() {
    return this._filterView;
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
  public SafeLiveData<ProductSortMethod> sortMethod() {
    return this._sortMethod;
  }

  /**
   * @see ProductViewModel#_expandedProductIndex
   */
  public SafeLiveData<Integer> expandedProductIndex() {
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
                    new SafeEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_unable_to_retrieve_all_products)));
              }

              result.postValue(products);
            });
    return result;
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
                          R.plurals.args_deleted_x_product, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_delete_product);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }

  public void onProductsChanged(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._products.setValue(Collections.unmodifiableList(products));
  }

  public void onSortMethodChanged(@NonNull ProductSortMethod sortMethod) {
    this.onSortMethodChanged(sortMethod, this._products.getValue());
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
    this.onSortMethodChanged(sortBy, this._products.getValue());
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

    // Reverse sort order when selecting same sort option.
    final boolean isAscending =
        this._sortMethod.getValue().sortBy() == sortBy
            ? !this._sortMethod.getValue().isAscending()
            : this._sortMethod.getValue().isAscending();

    this.onSortMethodChanged(new ProductSortMethod(sortBy, isAscending), products);
  }

  public void onExpandedProductIndexChanged(int index) {
    this._expandedProductIndex.setValue(index);
  }
}
