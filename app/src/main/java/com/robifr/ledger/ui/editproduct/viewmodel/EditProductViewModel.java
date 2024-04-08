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

package com.robifr.ledger.ui.editproduct.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createproduct.viewmodel.CreateProductViewModel;
import com.robifr.ledger.ui.editproduct.EditProductFragment;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class EditProductViewModel extends CreateProductViewModel {
  @NonNull
  private final MediatorLiveData<LiveDataEvent<ProductModel>> _initializedInitialProductToEdit =
      new MediatorLiveData<>();

  @Nullable private ProductModel _initialProductToEdit = null;

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _resultEditedProductId =
      new MutableLiveData<>();

  @Inject
  public EditProductViewModel(
      @NonNull ProductRepository productRepository, @NonNull SavedStateHandle savedStateHandle) {
    super(productRepository);
    Objects.requireNonNull(savedStateHandle);

    this._initializedInitialProductToEdit.addSource(
        // Shouldn't be null when editing data.
        this.selectProductById(
            Objects.requireNonNull(
                savedStateHandle.get(
                    EditProductFragment.Arguments.INITIAL_PRODUCT_ID_TO_EDIT.key()))),
        product -> {
          this._initialProductToEdit = product;
          this._initializedInitialProductToEdit.setValue(new LiveDataEvent<>(product));
        });
  }

  @Override
  @NonNull
  public ProductModel inputtedProduct() {
    final Long id =
        this._initialProductToEdit != null && this._initialProductToEdit.id() != null
            ? this._initialProductToEdit.id()
            : null;
    return ProductModel.toBuilder(super.inputtedProduct()).setId(id).build();
  }

  @Override
  public void onSave() {
    if (this._inputtedNameText.getValue() == null || this._inputtedNameText.getValue().isBlank()) {
      this._inputtedNameError.setValue(
          new LiveDataEvent<>(new StringResources.Strings(R.string.text_product_name_is_required)));
      return;
    }

    this._updateProduct(this.inputtedProduct());
  }

  @NonNull
  public LiveData<LiveDataEvent<ProductModel>> initializedInitialProductToEdit() {
    return this._initializedInitialProductToEdit;
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> resultEditedProductId() {
    return this._resultEditedProductId;
  }

  @NonNull
  public LiveData<ProductModel> selectProductById(@Nullable Long productId) {
    final MutableLiveData<ProductModel> result = new MutableLiveData<>();

    this._productRepository
        .selectById(productId)
        .thenAcceptAsync(
            product -> {
              if (product == null) {
                this._snackbarMessage.postValue(
                    new LiveDataEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_failed_to_find_related_product)));
              }

              result.postValue(product);
            });
    return result;
  }

  private void _updateProduct(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    this._productRepository
        .update(product)
        .thenAcceptAsync(
            effected -> {
              if (effected > 0) {
                this._resultEditedProductId.postValue(new LiveDataEvent<>(product.id()));
              }

              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_updated_x_product, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_update_product);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
  }
}
