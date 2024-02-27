/**
 * Copyright (c) 2022-present Robi
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

package com.robifr.ledger.ui.create_product.view_model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.util.CurrencyFormat;
import java.text.ParseException;
import java.util.Objects;

public class CreateProductViewModel extends ViewModel {
  @NonNull protected final ProductRepository _productRepository;

  @NonNull
  protected final MutableLiveData<LiveDataEvent<String>> _snackbarMessage = new MutableLiveData<>();

  /** Pair of name field error message (first) and whether the error is enabled (second). */
  @NonNull
  protected final MutableLiveData<LiveDataEvent<Pair<String, Boolean>>> _inputtedNameError =
      new MutableLiveData<>();

  @NonNull protected final MutableLiveData<String> _inputtedNameText = new MutableLiveData<>();
  @NonNull protected final MutableLiveData<String> _inputtedPriceText = new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _createdProductId = new MutableLiveData<>();

  public CreateProductViewModel(@NonNull ProductRepository productRepository) {
    this._productRepository = Objects.requireNonNull(productRepository);
  }

  @NonNull
  public LiveData<LiveDataEvent<String>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> createdProductId() {
    return this._createdProductId;
  }

  /**
   * @see CreateProductViewModel#_inputtedNameError
   */
  @NonNull
  public LiveData<LiveDataEvent<Pair<String, Boolean>>> inputtedNameError() {
    return this._inputtedNameError;
  }

  @NonNull
  public LiveData<String> inputtedNameText() {
    return this._inputtedNameText;
  }

  @NonNull
  public LiveData<String> inputtedPriceText() {
    return this._inputtedPriceText;
  }

  /**
   * Get current inputted product from any corresponding inputted live data. If any live data is set
   * using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public ProductModel inputtedProduct() {
    final String name = Objects.requireNonNullElse(this._inputtedNameText.getValue(), "");
    long price = 0L;

    try {
      final String priceText = Objects.requireNonNullElse(this._inputtedPriceText.getValue(), "");
      price = CurrencyFormat.parse(priceText, "id", "ID").longValue();

    } catch (ParseException ignore) {
    }

    return ProductModel.toBuilder().setName(name).setPrice(price).build();
  }

  public void onNameTextChanged(@NonNull String name) {
    Objects.requireNonNull(name);

    this._inputtedNameText.setValue(name);

    // Disable error when name field filled.
    if (!name.isBlank()) {
      this._inputtedNameError.setValue(new LiveDataEvent<>(new Pair<>("", false)));
    }
  }

  public void onPriceTextChanged(@NonNull String price) {
    Objects.requireNonNull(price);

    this._inputtedPriceText.setValue(price);
  }

  public void onSave() {
    if (this._inputtedNameText.getValue() == null || this._inputtedNameText.getValue().isBlank()) {
      this._inputtedNameError.setValue(
          new LiveDataEvent<>(new Pair<>("Product name is required", true)));
      return;
    }

    this._addProduct(this.inputtedProduct());
  }

  private void _addProduct(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    this._productRepository
        .add(product)
        .thenAcceptAsync(
            id -> {
              if (id != 0L) this._createdProductId.postValue(new LiveDataEvent<>(id));

              final String message =
                  id != 0L ? "Added 1 product(s)" : "Error! Failed to add product(s)";
              this._snackbarMessage.postValue(new LiveDataEvent<>(message));
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

      final CreateProductViewModel viewModel =
          new CreateProductViewModel(ProductRepository.instance(this._context));
      return Objects.requireNonNull(cls.cast(viewModel));
    }
  }
}
