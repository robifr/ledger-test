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

package com.robifr.ledger.ui.create_queue.view_model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Objects;

public class MakeProductOrderViewModel {
  @NonNull private final CreateQueueViewModel _viewModel;
  @NonNull private final MutableLiveData<ProductModel> _inputtedProduct = new MutableLiveData<>();
  @NonNull private final MutableLiveData<String> _inputtedQuantityText = new MutableLiveData<>();
  @NonNull private final MutableLiveData<String> _inputtedDiscountText = new MutableLiveData<>();
  @NonNull private final MutableLiveData<BigDecimal> _inputtedTotalPrice = new MutableLiveData<>();
  @Nullable private ProductOrderModel _productOrderToEdit = null;

  public MakeProductOrderViewModel(@NonNull CreateQueueViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @NonNull
  public LiveData<ProductModel> inputtedProduct() {
    return this._inputtedProduct;
  }

  @NonNull
  public LiveData<String> inputtedQuantityText() {
    return this._inputtedQuantityText;
  }

  @NonNull
  public LiveData<String> inputtedDiscountText() {
    return this._inputtedDiscountText;
  }

  @NonNull
  public LiveData<BigDecimal> inputtedTotalPrice() {
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
    final ProductOrderModel defaultOrder = ProductOrderModel.toBuilder().build();

    final ProductModel product = this._inputtedProduct.getValue();
    final Long productId =
        product != null && product.id() != null ? product.id() : defaultOrder.productId();
    final String productName = product != null ? product.name() : defaultOrder.productName();
    final Long productPrice =
        product != null ? (Long) product.price() : defaultOrder.productPrice();

    final Long id =
        this._productOrderToEdit != null && this._productOrderToEdit.id() != null
            ? this._productOrderToEdit.id()
            : defaultOrder.id();
    final BigDecimal totalPrice =
        Objects.requireNonNullElse(this._inputtedTotalPrice.getValue(), defaultOrder.totalPrice());
    double quantity = defaultOrder.quantity();
    long discount = defaultOrder.discount();

    try {
      final String quantityText = this._inputtedQuantityText.getValue();

      if (quantityText != null && !quantityText.isBlank()) {
        quantity =
            CurrencyFormat.parse(this._inputtedQuantityText.getValue(), "id", "ID")
                .stripTrailingZeros()
                .doubleValue();
      }

    } catch (ParseException ignore) {
    }

    try {
      final String discountText = this._inputtedDiscountText.getValue();

      if (discountText != null && !discountText.isBlank()) {
        discount =
            CurrencyFormat.parse(this._inputtedDiscountText.getValue(), "id", "ID").longValue();
      }

    } catch (ParseException ignore) {
    }

    return ProductOrderModel.toBuilder(defaultOrder)
        .setId(id)
        .setProductId(productId)
        .setProductName(productName)
        .setProductPrice(productPrice)
        .setTotalPrice(totalPrice)
        .setQuantity(quantity)
        .setDiscount(discount)
        .build();
  }

  public void onProductChanged(@Nullable ProductModel product) {
    this._inputtedProduct.setValue(product);
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
    final int indexToUpdate =
        this._viewModel.inputtedProductOrders().indexOf(this._productOrderToEdit);

    // Add as new when there's no product order to update,
    // otherwise update them with the one user inputted.
    if (indexToUpdate == -1) this._viewModel.onAddProductOrder(this.inputtedProductOrder());
    else this._viewModel.onUpdateProductOrder(indexToUpdate, this.inputtedProductOrder());
  }

  public void onReset() {
    this._productOrderToEdit = null;
    this._inputtedProduct.setValue(null);
    this._inputtedQuantityText.setValue(null);
    this._inputtedDiscountText.setValue(null);
    this._inputtedTotalPrice.setValue(BigDecimal.ZERO);
  }
}
