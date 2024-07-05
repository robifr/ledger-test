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

package com.robifr.ledger.ui.createproduct.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.util.CurrencyFormat;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.text.ParseException;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class CreateProductViewModel extends ViewModel {
  @NonNull protected final ProductRepository _productRepository;

  @NonNull
  protected final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<LiveDataEvent<StringResources>> _inputtedNameError =
      new MutableLiveData<>();

  @NonNull
  protected final SafeMutableLiveData<String> _inputtedNameText = new SafeMutableLiveData<>("");

  @NonNull
  protected final SafeMutableLiveData<String> _inputtedPriceText = new SafeMutableLiveData<>("");

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _resultCreatedProductId =
      new MutableLiveData<>();

  @Inject
  public CreateProductViewModel(@NonNull ProductRepository productRepository) {
    this._productRepository = Objects.requireNonNull(productRepository);
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> inputtedNameError() {
    return this._inputtedNameError;
  }

  @NonNull
  public SafeLiveData<String> inputtedNameText() {
    return this._inputtedNameText;
  }

  @NonNull
  public SafeLiveData<String> inputtedPriceText() {
    return this._inputtedPriceText;
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> resultCreatedProductId() {
    return this._resultCreatedProductId;
  }

  /**
   * Get current inputted product from any corresponding inputted live data. If any live data is set
   * using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public ProductModel inputtedProduct() {
    long price = 0L;

    try {
      price = CurrencyFormat.parse(this._inputtedPriceText.getValue(), "id", "ID").longValue();

    } catch (ParseException ignore) {
    }

    return ProductModel.toBuilder()
        .setName(this._inputtedNameText.getValue())
        .setPrice(price)
        .build();
  }

  public void onNameTextChanged(@NonNull String name) {
    Objects.requireNonNull(name);

    this._inputtedNameText.setValue(name);

    // Disable error when name field filled.
    if (!name.isBlank()) this._inputtedNameError.setValue(new LiveDataEvent<>(null));
  }

  public void onPriceTextChanged(@NonNull String price) {
    Objects.requireNonNull(price);

    this._inputtedPriceText.setValue(price);
  }

  public void onSave() {
    if (this._inputtedNameText.getValue().isBlank()) {
      this._inputtedNameError.setValue(
          new LiveDataEvent<>(new StringResources.Strings(R.string.text_product_name_is_required)));
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
              if (id != 0L) this._resultCreatedProductId.postValue(new LiveDataEvent<>(id));

              final StringResources stringRes =
                  id != 0L
                      ? new StringResources.Plurals(R.plurals.args_added_x_product, 1, 1)
                      : new StringResources.Strings(R.string.text_error_failed_to_add_product);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
  }
}
