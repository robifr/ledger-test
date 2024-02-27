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

package com.robifr.ledger.ui.create_queue;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.databinding.ProductOrderCardBinding;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class ProductOrderCardComponent {
  @NonNull private final Context _context;
  @NonNull private final ProductOrderCardBinding _binding;

  public ProductOrderCardComponent(
      @NonNull Context context, @NonNull ProductOrderCardBinding binding) {
    Objects.requireNonNull(context);

    this._context = context.getApplicationContext();
    this._binding = Objects.requireNonNull(binding);

    this._binding.productImage.shapeableImage.setShapeAppearanceModel(
        ShapeAppearanceModel.builder(
                this._context,
                com.google.android.material.R.style.Widget_MaterialComponents_ShapeableImageView,
                R.style.Shape_Card)
            .build());
  }

  public void setProductOrder(@NonNull ProductOrderModel productOrder) {
    Objects.requireNonNull(productOrder);

    this._setProductName(productOrder.productName());
    this._setProductPrice(productOrder.productPrice());
    this._setQuantity(productOrder.quantity());
    this._setTotalPrice(productOrder.totalPrice());
    this._setDiscount(productOrder.discountPercent());
  }

  private void _setProductName(@Nullable String productName) {
    final boolean shouldViewEnabled = productName != null;
    final String name = shouldViewEnabled ? productName : "n/a";

    this._binding.productName.setText(name);
    this._binding.productName.setEnabled(shouldViewEnabled);
    this._binding.productImage.text.setText(
        name.trim().substring(0, Math.min(1, name.trim().length())));
  }

  private void _setProductPrice(@Nullable Long productPrice) {
    final String price =
        productPrice != null
            ? CurrencyFormat.format(BigDecimal.valueOf(productPrice), "id", "ID")
            : "n/a";
    this._binding.productPrice.setText(price);
  }

  private void _setQuantity(@NonNull Double quantity) {
    Objects.requireNonNull(quantity);

    final String quantities = CurrencyFormat.format(BigDecimal.valueOf(quantity), "id", "ID", "");
    this._binding.quantity.setText(" x " + quantities);
  }

  private void _setTotalPrice(@NonNull BigDecimal totalPrice) {
    Objects.requireNonNull(totalPrice);

    this._binding.totalPrice.setText(CurrencyFormat.format(totalPrice, "id", "ID"));
  }

  private void _setDiscount(@NonNull BigDecimal discountPercent) {
    Objects.requireNonNull(discountPercent);

    final int textVisibility =
        discountPercent.compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE;
    final String text =
        discountPercent.compareTo(BigDecimal.ZERO) == 0
            ? null
            : discountPercent.toPlainString() + "% off";

    this._binding.discount.setVisibility(textVisibility);
    this._binding.discount.setText(text);
  }
}
