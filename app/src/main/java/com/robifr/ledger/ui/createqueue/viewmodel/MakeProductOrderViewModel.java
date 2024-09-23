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

package com.robifr.ledger.ui.createqueue.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.util.CurrencyFormat;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class MakeProductOrderViewModel {
  @NonNull private final CreateQueueViewModel _viewModel;

  @NonNull
  private final SafeMutableLiveData<Optional<ProductModel>> _inputtedProduct =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull
  private final SafeMutableLiveData<String> _inputtedQuantityText = new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<String> _inputtedDiscountText = new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<BigDecimal> _inputtedTotalPrice =
      new SafeMutableLiveData<>(BigDecimal.ZERO);

  @Nullable private ProductOrderModel _productOrderToEdit = null;

  public MakeProductOrderViewModel(@NonNull CreateQueueViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @NonNull
  public SafeLiveData<Optional<ProductModel>> inputtedProduct() {
    return this._inputtedProduct;
  }

  @NonNull
  public SafeLiveData<String> inputtedQuantityText() {
    return this._inputtedQuantityText;
  }

  @NonNull
  public SafeLiveData<String> inputtedDiscountText() {
    return this._inputtedDiscountText;
  }

  @NonNull
  public SafeLiveData<BigDecimal> inputtedTotalPrice() {
    return this._inputtedTotalPrice;
  }

  @Nullable
  public ProductOrderModel productOrderToEdit() {
    return this._productOrderToEdit;
  }

  /**
   * Get current inputted product order from any corresponding inputted live data. If any live data
   * is set using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public ProductOrderModel inputtedProductOrder() {
    final Long id =
        this._productOrderToEdit != null && this._productOrderToEdit.id() != null
            ? this._productOrderToEdit.id()
            : null;
    final Long queueId =
        this._productOrderToEdit != null && this._productOrderToEdit.queueId() != null
            ? this._productOrderToEdit.queueId()
            : null;
    double quantity = 0.0;
    long discount = 0L;

    try {
      if (!this._inputtedQuantityText.getValue().isBlank()) {
        quantity =
            CurrencyFormat.parse(this._inputtedQuantityText.getValue(), "id", "ID")
                .stripTrailingZeros()
                .doubleValue();
      }

    } catch (ParseException ignore) {
    }

    try {
      if (!this._inputtedDiscountText.getValue().isBlank()) {
        discount =
            CurrencyFormat.parse(this._inputtedDiscountText.getValue(), "id", "ID").longValue();
      }

    } catch (ParseException ignore) {
    }

    return ProductOrderModel.toBuilder()
        .setId(id)
        .setQueueId(queueId)
        .setProductId(this._inputtedProduct.getValue().map(ProductModel::id).orElse(null))
        .setProductName(this._inputtedProduct.getValue().map(ProductModel::name).orElse(null))
        .setProductPrice(this._inputtedProduct.getValue().map(ProductModel::price).orElse(null))
        .setTotalPrice(this._inputtedTotalPrice.getValue())
        .setQuantity(quantity)
        .setDiscount(discount)
        .build();
  }

  public void onProductChanged(@Nullable ProductModel product) {
    this._inputtedProduct.setValue(Optional.ofNullable(product));
    this.onTotalPriceChanged(this.inputtedProductOrder().calculateTotalPrice());
  }

  public void onQuantityTextChanged(@NonNull String quantity) {
    Objects.requireNonNull(quantity);

    this._inputtedQuantityText.setValue(quantity);
    this.onTotalPriceChanged(this.inputtedProductOrder().calculateTotalPrice());
  }

  public void onDiscountTextChanged(@NonNull String discount) {
    Objects.requireNonNull(discount);

    this._inputtedDiscountText.setValue(discount);
    this.onTotalPriceChanged(this.inputtedProductOrder().calculateTotalPrice());
  }

  public void onTotalPriceChanged(@NonNull BigDecimal totalPrice) {
    Objects.requireNonNull(totalPrice);

    this._inputtedTotalPrice.setValue(totalPrice);
  }

  public void onProductOrderToEditChanged(@NonNull ProductOrderModel productOrder) {
    Objects.requireNonNull(productOrder);

    this._productOrderToEdit = productOrder;
    final String quantity =
        CurrencyFormat.format(BigDecimal.valueOf(productOrder.quantity()), "id", "ID", "");
    final String discount =
        CurrencyFormat.format(BigDecimal.valueOf(productOrder.discount()), "id", "ID");

    this.onProductChanged(productOrder.referencedProduct());
    this.onQuantityTextChanged(quantity);
    this.onDiscountTextChanged(discount);
    this.onTotalPriceChanged(productOrder.totalPrice());
  }

  public void onSave() {
    final ArrayList<ProductOrderModel> productOrders =
        new ArrayList<>(this._viewModel.inputtedProductOrders().getValue());
    final int indexToUpdate = productOrders.indexOf(this._productOrderToEdit);

    // Add as new when there's no product order to update,
    // otherwise update them with the one user inputted.
    if (indexToUpdate == -1) productOrders.add(this.inputtedProductOrder());
    else productOrders.set(indexToUpdate, this.inputtedProductOrder());

    this._viewModel.onProductOrdersChanged(productOrders);
  }

  public void onReset() {
    this._productOrderToEdit = null;
    this._inputtedProduct.setValue(Optional.empty());
    this._inputtedQuantityText.setValue("");
    this._inputtedDiscountText.setValue("");
    this._inputtedTotalPrice.setValue(BigDecimal.ZERO);
  }
}
