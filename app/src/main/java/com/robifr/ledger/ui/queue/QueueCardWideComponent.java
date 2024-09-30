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

package com.robifr.ledger.ui.queue;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.QueueCardWideBinding;
import com.robifr.ledger.databinding.QueueCardWideExpandedOrderBinding;
import com.robifr.ledger.databinding.QueueCardWideExpandedOrderDataBinding;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class QueueCardWideComponent {
  @NonNull private final Context _context;
  @NonNull private final QueueCardWideBinding _binding;
  @NonNull private final QueueCardWideExpandedOrderBinding _productOrderBinding;

  public QueueCardWideComponent(@NonNull Context context, @NonNull QueueCardWideBinding binding) {
    this._context = Objects.requireNonNull(context);
    this._binding = Objects.requireNonNull(binding);
    this._productOrderBinding =
        QueueCardWideExpandedOrderBinding.bind(this._binding.expandedCard.getRoot());

    final ShapeAppearanceModel imageShape =
        ShapeAppearanceModel.builder(
                this._context,
                com.google.android.material.R.style.Widget_MaterialComponents_ShapeableImageView,
                R.style.Shape_Round)
            .build();
    this._binding.normalCard.customerImage.shapeableImage.setShapeAppearanceModel(imageShape);
    this._binding.expandedCard.customerImage.shapeableImage.setShapeAppearanceModel(imageShape);
  }

  public void setNormalCardQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._setId(queue.id(), true);
    this._setDate(queue.date().atZone(ZoneId.systemDefault()), true);
    this._setStatus(queue.status(), true);
    this._setGrandTotalPrice(queue.grandTotalPrice(), true);
    this._setCustomer(queue.customer(), true);
  }

  public void setExpandedCardQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._setId(queue.id(), false);
    this._setDate(queue.date().atZone(ZoneId.systemDefault()), false);
    this._setStatus(queue.status(), false);
    this._setGrandTotalPrice(queue.grandTotalPrice(), false);
    this._setCustomer(queue.customer(), false);
    this._setPaymentMethod(queue.paymentMethod());
    this._setTotalDiscount(queue.totalDiscount());
    this._setProductOrders(queue.productOrders());
  }

  public void setCardExpanded(boolean isExpanded) {
    final int normalCardVisibility = isExpanded ? View.GONE : View.VISIBLE;
    final int expandedCardVisibility = isExpanded ? View.VISIBLE : View.GONE;

    this._binding.normalCard.getRoot().setVisibility(normalCardVisibility);
    this._binding.expandedCard.getRoot().setVisibility(expandedCardVisibility);
  }

  public void reset() {
    this._binding.normalCard.uniqueId.setText(null);
    this._binding.normalCard.uniqueId.setEnabled(false);
    this._binding.expandedCard.uniqueId.setText(null);
    this._binding.expandedCard.uniqueId.setEnabled(false);

    this._binding.normalCard.date.setText(null);
    this._binding.expandedCard.date.setText(null);

    this._binding.normalCard.statusChip.setText(null);
    this._binding.normalCard.statusChip.setTextColor(0);
    this._binding.normalCard.statusChip.setChipBackgroundColor(ColorStateList.valueOf(0));
    this._binding.normalCard.coloredSideline.setBackgroundColor(0);

    this._binding.expandedCard.statusChip.setText(null);
    this._binding.expandedCard.statusChip.setTextColor(0);
    this._binding.expandedCard.statusChip.setChipBackgroundColor(ColorStateList.valueOf(0));
    this._binding.expandedCard.coloredSideline.setBackgroundColor(0);

    this._binding.expandedCard.paymentMethod.setText(null);

    this._productOrderBinding.totalDiscount.setText(null);

    this._binding.normalCard.grandTotalPrice.setText(null);
    this._productOrderBinding.grandTotalPrice.setText(null);

    this._binding.normalCard.customerImage.text.setText(null);
    this._binding.normalCard.customerName.setText(null);
    this._binding.normalCard.customerName.setEnabled(false);
    this._binding.expandedCard.customerImage.text.setText(null);
    this._binding.expandedCard.customerName.setText(null);
    this._binding.expandedCard.customerName.setEnabled(false);
    // Customer data on the expanded card's product orders detail.
    this._productOrderBinding.customerBalanceTitle.setText(null);
    this._productOrderBinding.customerBalanceTitle.setVisibility(View.GONE);
    this._productOrderBinding.customerBalance.setText(null);
    this._productOrderBinding.customerBalance.setVisibility(View.GONE);
    this._productOrderBinding.customerDebtTitle.setText(null);
    this._productOrderBinding.customerDebtTitle.setVisibility(View.GONE);
    this._productOrderBinding.customerDebt.setText(null);
    this._productOrderBinding.customerDebt.setTextColor(0);
    this._productOrderBinding.customerDebt.setVisibility(View.GONE);
  }

  private void _setId(@Nullable Long id, boolean isNormalCard) {
    final boolean isIdExists = id != null;
    final String queueId =
        id != null ? id.toString() : this._context.getString(R.string.symbol_notavailable);

    if (isNormalCard) {
      this._binding.normalCard.uniqueId.setText(queueId);
      this._binding.normalCard.uniqueId.setEnabled(isIdExists);
    } else {
      this._binding.expandedCard.uniqueId.setText(queueId);
      this._binding.expandedCard.uniqueId.setEnabled(isIdExists);
    }
  }

  private void _setDate(@NonNull ZonedDateTime date, boolean isNormalCard) {
    Objects.requireNonNull(date);

    final String formattedDate = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));

    if (isNormalCard) this._binding.normalCard.date.setText(formattedDate);
    else this._binding.expandedCard.date.setText(formattedDate);
  }

  private void _setStatus(@NonNull QueueModel.Status status, boolean isNormalCard) {
    Objects.requireNonNull(status);

    final String statusText = this._context.getString(status.resourceString());
    final int statusTextColor = this._context.getColor(status.resourceTextColor());
    final int statusBackground = this._context.getColor(status.resourceBackgroundColor());

    if (isNormalCard) {
      this._binding.normalCard.statusChip.setText(statusText);
      this._binding.normalCard.statusChip.setTextColor(statusTextColor);
      this._binding.normalCard.statusChip.setChipBackgroundColor(
          ColorStateList.valueOf(statusBackground));
      this._binding.normalCard.coloredSideline.setBackgroundColor(statusBackground);
    } else {
      this._binding.expandedCard.statusChip.setText(statusText);
      this._binding.expandedCard.statusChip.setTextColor(statusTextColor);
      this._binding.expandedCard.statusChip.setChipBackgroundColor(
          ColorStateList.valueOf(statusBackground));
      this._binding.expandedCard.coloredSideline.setBackgroundColor(statusBackground);
    }
  }

  private void _setPaymentMethod(@NonNull QueueModel.PaymentMethod paymentMethod) {
    Objects.requireNonNull(paymentMethod);

    this._binding.expandedCard.paymentMethod.setText(
        this._context.getString(paymentMethod.resourceString()));
  }

  private void _setTotalDiscount(@NonNull BigDecimal totalDiscount) {
    Objects.requireNonNull(totalDiscount);

    this._productOrderBinding.totalDiscount.setText(
        CurrencyFormat.format(totalDiscount, "id", "ID"));
  }

  private void _setGrandTotalPrice(@NonNull BigDecimal grandTotalPrice, boolean isNormalCard) {
    Objects.requireNonNull(grandTotalPrice);

    final String formattedGrandTotalPrice = CurrencyFormat.format(grandTotalPrice, "id", "ID");

    if (isNormalCard) this._binding.normalCard.grandTotalPrice.setText(formattedGrandTotalPrice);
    else this._productOrderBinding.grandTotalPrice.setText(formattedGrandTotalPrice);
  }

  private void _setCustomer(@Nullable CustomerModel customer, boolean isNormalCard) {
    if (customer == null) {
      final String notAvailableText = this._context.getString(R.string.symbol_notavailable);

      if (isNormalCard) {
        this._binding.normalCard.customerImage.text.setText(null);
        this._binding.normalCard.customerName.setText(notAvailableText);
        this._binding.normalCard.customerName.setEnabled(false);
      } else {
        this._binding.expandedCard.customerImage.text.setText(null);
        this._binding.expandedCard.customerName.setText(notAvailableText);
        this._binding.expandedCard.customerName.setEnabled(false);
        this._productOrderBinding.customerBalanceTitle.setVisibility(View.GONE);
        this._productOrderBinding.customerBalance.setVisibility(View.GONE);
        this._productOrderBinding.customerDebtTitle.setVisibility(View.GONE);
        this._productOrderBinding.customerDebt.setVisibility(View.GONE);
      }

      return;
    }

    final String customerInitialLetterName =
        customer.name().trim().substring(0, Math.min(1, customer.name().trim().length()));
    final String croppedCustomerName =
        customer.name().length() > 12 ? customer.name().substring(0, 12) : customer.name();
    final int debtTextColor =
        customer.debt().compareTo(BigDecimal.ZERO) < 0
            // Negative debt will be shown red.
            ? this._context.getColor(R.color.red)
            : this._context.getColor(R.color.text_enabled);

    if (isNormalCard) {
      this._binding.normalCard.customerImage.text.setText(customerInitialLetterName);
      this._binding.normalCard.customerName.setText(customer.name());
      this._binding.normalCard.customerName.setEnabled(true);
    } else {
      this._binding.expandedCard.customerImage.text.setText(customerInitialLetterName);
      this._binding.expandedCard.customerName.setText(customer.name());
      this._binding.expandedCard.customerName.setEnabled(true);
      // Displaying customer data on the product orders detail.
      this._productOrderBinding.customerBalanceTitle.setText(
          this._context.getString(
              R.string.productordercard_customerbalance_title, croppedCustomerName));
      this._productOrderBinding.customerBalanceTitle.setVisibility(View.VISIBLE);
      this._productOrderBinding.customerBalance.setText(
          CurrencyFormat.format(BigDecimal.valueOf(customer.balance()), "id", "ID"));
      this._productOrderBinding.customerBalance.setVisibility(View.VISIBLE);
      this._productOrderBinding.customerDebtTitle.setText(
          this._context.getString(
              R.string.productordercard_customerdebt_title, croppedCustomerName));
      this._productOrderBinding.customerDebtTitle.setVisibility(View.VISIBLE);
      this._productOrderBinding.customerDebt.setText(
          CurrencyFormat.format(customer.debt(), "id", "ID"));
      this._productOrderBinding.customerDebt.setTextColor(debtTextColor);
      this._productOrderBinding.customerDebt.setVisibility(View.VISIBLE);
    }
  }

  private void _setProductOrders(@NonNull List<ProductOrderModel> productOrders) {
    Objects.requireNonNull(productOrders);

    this._productOrderBinding.table.removeAllViews();

    for (ProductOrderModel productOrder : productOrders) {
      final QueueCardWideExpandedOrderDataBinding dataRowBinding =
          QueueCardWideExpandedOrderDataBinding.inflate(LayoutInflater.from(this._context));
      final boolean isProductExists =
          productOrder.productName() != null && productOrder.productPrice() != null;

      final String productName =
          productOrder.productName() != null
              ? productOrder.productName()
              : this._context.getString(R.string.symbol_notavailable);
      final String productPrice =
          productOrder.productPrice() != null
              ? CurrencyFormat.format(BigDecimal.valueOf(productOrder.productPrice()), "id", "ID")
              : this._context.getString(R.string.symbol_notavailable);
      final int discountVisibility =
          productOrder.discountPercent().compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE;

      dataRowBinding.productName.setText(productName);
      dataRowBinding.productName.setEnabled(isProductExists);
      dataRowBinding.productPrice.setText(productPrice);
      dataRowBinding.quantity.setText(
          CurrencyFormat.format(BigDecimal.valueOf(productOrder.quantity()), "id", "ID", ""));
      dataRowBinding.totalPrice.setText(
          CurrencyFormat.format(productOrder.totalPrice(), "id", "ID"));
      dataRowBinding.discount.setText(
          this._context.getString(
              R.string.productordercard_discount_title,
              productOrder.discountPercent().toPlainString()));
      dataRowBinding.discount.setVisibility(discountVisibility);
      this._productOrderBinding.table.addView(dataRowBinding.getRoot());
    }
  }
}
