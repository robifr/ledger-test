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

package com.robifr.ledger.ui.createqueue;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
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
    this._context = Objects.requireNonNull(context);
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
    this._setProductPriceAndQuantity(productOrder.productPrice(), productOrder.quantity());
    this._setTotalPrice(productOrder.totalPrice());
    this._setDiscount(productOrder.discountPercent());
  }

  private void _setProductName(@Nullable String productName) {
    final boolean isNameExists = productName != null;
    final String name =
        isNameExists ? productName : this._context.getString(R.string.symbol_notavailable);

    this._binding.productName.setText(name);
    this._binding.productName.setEnabled(isNameExists);
    this._binding.productImage.text.setText(
        name.trim().substring(0, Math.min(1, name.trim().length())));
  }

  private void _setProductPriceAndQuantity(@Nullable Long productPrice, double quantity) {
    final String price =
        productPrice != null
            ? CurrencyFormat.format(
                BigDecimal.valueOf(productPrice),
                AppCompatDelegate.getApplicationLocales().toLanguageTags())
            : this._context.getString(R.string.symbol_notavailable);
    final String quantities =
        CurrencyFormat.format(
            BigDecimal.valueOf(quantity),
            AppCompatDelegate.getApplicationLocales().toLanguageTags(),
            "");

    this._binding.productPriceQuantity.setText(
        this._context.getString(
            R.string.productordercard_productpriceandquantity_title, price, quantities));
  }

  private void _setTotalPrice(@NonNull BigDecimal totalPrice) {
    Objects.requireNonNull(totalPrice);

    this._binding.totalPrice.setText(
        CurrencyFormat.format(
            totalPrice, AppCompatDelegate.getApplicationLocales().toLanguageTags()));
  }

  private void _setDiscount(@NonNull BigDecimal discountPercent) {
    Objects.requireNonNull(discountPercent);

    final int textVisibility =
        discountPercent.compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE;
    final String text =
        discountPercent.compareTo(BigDecimal.ZERO) == 0
            ? null
            : this._context.getString(
                R.string.productordercard_discount_title, discountPercent.toPlainString());

    this._binding.discount.setVisibility(textVisibility);
    this._binding.discount.setText(text);
  }
}
