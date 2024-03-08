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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createproduct.viewmodel.CreateProductViewModel;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class EditProductViewModel extends CreateProductViewModel {
  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _editedProductId = new MutableLiveData<>();

  @Nullable private ProductModel _initialProductToEdit = null;

  public EditProductViewModel(@NonNull ProductRepository productRepository) {
    super(productRepository);
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

  public void setInitialProductToEdit(@NonNull ProductModel product) {
    this._initialProductToEdit = Objects.requireNonNull(product);
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> editedProductId() {
    return this._editedProductId;
  }

  @Nullable
  public ProductModel selectProductById(@Nullable Long productId) {
    final StringResources notFoundRes =
        new StringResources.Strings(R.string.text_error_failed_to_find_related_product);
    ProductModel product = null;

    try {
      product = this._productRepository.selectById(productId).get();
      if (product == null) this._snackbarMessage.setValue(new LiveDataEvent<>(notFoundRes));

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(new LiveDataEvent<>(notFoundRes));
    }

    return product;
  }

  private void _updateProduct(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    this._productRepository
        .update(product)
        .thenAcceptAsync(
            effected -> {
              if (effected > 0) this._editedProductId.postValue(new LiveDataEvent<>(product.id()));

              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_updated_x_product, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_update_product);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
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

      final EditProductViewModel viewModel =
          new EditProductViewModel(ProductRepository.instance(this._context));
      return Objects.requireNonNull(cls.cast(viewModel));
    }
  }
}
