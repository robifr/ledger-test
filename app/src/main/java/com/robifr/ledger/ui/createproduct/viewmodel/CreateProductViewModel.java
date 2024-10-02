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

package com.robifr.ledger.ui.createproduct.viewmodel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.util.CurrencyFormat;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class CreateProductViewModel extends ViewModel {
  @NonNull protected final ProductRepository _productRepository;

  @NonNull
  protected final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  protected final SafeMutableLiveData<Optional<StringResources>> _inputtedNameError =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull
  protected final SafeMutableLiveData<String> _inputtedNameText = new SafeMutableLiveData<>("");

  @NonNull
  protected final SafeMutableLiveData<String> _inputtedPriceText = new SafeMutableLiveData<>("");

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultCreatedProductId =
      new MutableLiveData<>();

  @Inject
  public CreateProductViewModel(@NonNull ProductRepository productRepository) {
    this._productRepository = Objects.requireNonNull(productRepository);
  }

  @NonNull
  public LiveData<SafeEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<Optional<StringResources>> inputtedNameError() {
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
  public LiveData<SafeEvent<Optional<Long>>> resultCreatedProductId() {
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
      price =
          CurrencyFormat.parse(
                  this._inputtedPriceText.getValue(),
                  AppCompatDelegate.getApplicationLocales().toLanguageTags())
              .longValue();

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
    if (!name.isBlank()) this._inputtedNameError.setValue(Optional.empty());
  }

  public void onPriceTextChanged(@NonNull String price) {
    Objects.requireNonNull(price);

    this._inputtedPriceText.setValue(price);
  }

  public void onSave() {
    if (this._inputtedNameText.getValue().isBlank()) {
      this._inputtedNameError.setValue(
          Optional.of(new StringResources.Strings(R.string.text_product_name_is_required)));
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
              if (id != 0L) {
                this._resultCreatedProductId.postValue(new SafeEvent<>(Optional.of(id)));
              }

              final StringResources stringRes =
                  id != 0L
                      ? new StringResources.Plurals(R.plurals.args_added_x_product, 1, 1)
                      : new StringResources.Strings(R.string.text_error_failed_to_add_product);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }
}
