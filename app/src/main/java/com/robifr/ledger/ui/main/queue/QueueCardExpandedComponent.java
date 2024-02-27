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

package com.robifr.ledger.ui.main.queue;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.QueueCardExpandedBinding;
import com.robifr.ledger.databinding.QueueCardExpandedProductOrderDataBinding;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class QueueCardExpandedComponent {
  @NonNull private final Context _context;
  @NonNull private final QueueCardExpandedBinding _binding;

  public QueueCardExpandedComponent(
      @NonNull Context context, @NonNull QueueCardExpandedBinding binding) {
    Objects.requireNonNull(context);

    this._context = context.getApplicationContext();
    this._binding = Objects.requireNonNull(binding);

    this._binding.customerImage.shapeableImage.setShapeAppearanceModel(
        ShapeAppearanceModel.builder(
                this._context,
                com.google.android.material.R.style.Widget_MaterialComponents_ShapeableImageView,
                R.style.Shape_Round)
            .build());
  }

  public void setQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._setId(queue.id());
    this._setDate(queue.date().atZone(ZoneId.systemDefault()));
    this._setStatus(queue.status());
    this._setPaymentMethod(queue.paymentMethod());
    this._setTotalDiscount(queue.totalDiscount());
    this._setGrandTotalPrice(queue.grandTotalPrice());
    this._setCustomer(queue.customer());
    this._setProductOrders(queue.productOrders());
  }

  public void reset() {
    this._binding.uniqueId.setText(null);
    this._binding.date.setText(null);
    this._binding.paymentMethod.setText(null);

    this._binding.statusChip.setText(null);
    this._binding.statusChip.setTextColor(0);
    this._binding.statusChip.setChipBackgroundColor(null);
    this._binding.coloredSideline.setBackgroundColor(0);

    this._binding.productOrderTotalRow.totalDiscount.setText(null);
    this._binding.productOrderTotalRow.grandTotalPrice.setText(null);
    this._binding.productOrderTotalRow.customerBalanceTitle.setText(null);
    this._binding.productOrderTotalRow.customerBalance.setText(null);
    this._binding.productOrderTotalRow.customerDebtTitle.setText(null);
    this._binding.productOrderTotalRow.customerDebt.setText(null);
    this._binding.productOrderTotalRow.customerDebt.setTextColor(0);
    // Remove all data rows type in table except for both
    // title — index 0 — and total — last index — row type.
    this._binding.productOrderTable.removeViews(
        1, this._binding.productOrderTable.getChildCount() - 2);

    this._binding.customerImage.text.setText(null);
    this._binding.customerName.setText(null);
  }

  private void _setId(@Nullable Long id) {
    final String queueId =
        id != null ? id.toString() : this._context.getString(R.string.symbol_notavailable);
    this._binding.uniqueId.setText(queueId);
  }

  private void _setDate(@NonNull ZonedDateTime date) {
    Objects.requireNonNull(date);

    this._binding.date.setText(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")));
  }

  private void _setStatus(@NonNull QueueModel.Status status) {
    Objects.requireNonNull(status);

    final int statusBackground =
        ContextCompat.getColor(this._context, status.resourceBackgroundColor());

    this._binding.statusChip.setText(this._context.getString(status.resourceString()));
    this._binding.statusChip.setTextColor(
        ContextCompat.getColor(this._context, status.resourceTextColor()));
    this._binding.statusChip.setChipBackgroundColor(ColorStateList.valueOf(statusBackground));
    this._binding.coloredSideline.setBackgroundColor(statusBackground);
  }

  private void _setPaymentMethod(@NonNull QueueModel.PaymentMethod paymentMethod) {
    Objects.requireNonNull(paymentMethod);

    this._binding.paymentMethod.setText(this._context.getString(paymentMethod.resourceString()));
  }

  private void _setTotalDiscount(@NonNull BigDecimal totalDiscount) {
    Objects.requireNonNull(totalDiscount);

    this._binding.productOrderTotalRow.totalDiscount.setText(
        CurrencyFormat.format(totalDiscount, "id", "ID"));
  }

  private void _setGrandTotalPrice(@NonNull BigDecimal grandTotalPrice) {
    Objects.requireNonNull(grandTotalPrice);

    this._binding.productOrderTotalRow.grandTotalPrice.setText(
        CurrencyFormat.format(grandTotalPrice, "id", "ID"));
  }

  private void _setCustomer(@Nullable CustomerModel customer) {
    if (customer == null) {
      this._binding.customerImage.text.setText(null);
      this._binding.customerName.setText(this._context.getString(R.string.symbol_notavailable));
      this._binding.customerName.setEnabled(false);
      this._binding.productOrderTotalRow.customerBalanceTitle.setVisibility(View.GONE);
      this._binding.productOrderTotalRow.customerBalance.setVisibility(View.GONE);
      this._binding.productOrderTotalRow.customerDebtTitle.setVisibility(View.GONE);
      this._binding.productOrderTotalRow.customerDebt.setVisibility(View.GONE);
      return;
    }

    final String croppedName =
        customer.name().length() > 12 ? customer.name().substring(0, 12) : customer.name();
    final int debtTextColor =
        customer.debt().compareTo(BigDecimal.ZERO) < 0
            // Negative debt will be shown red.
            ? ContextCompat.getColor(this._context, R.color.red)
            : ContextCompat.getColor(this._context, R.color.text_enabled);

    this._binding.customerImage.text.setText(
        customer.name().trim().substring(0, Math.min(1, customer.name().trim().length())));
    this._binding.customerName.setText(customer.name());
    this._binding.customerName.setEnabled(true);

    // Displaying customer data on the product orders table, total typed row.
    this._binding.productOrderTotalRow.customerBalanceTitle.setText(
        this._context.getString(R.string.productordercard_customerbalance_title, croppedName));
    this._binding.productOrderTotalRow.customerBalanceTitle.setVisibility(View.VISIBLE);
    this._binding.productOrderTotalRow.customerBalance.setText(
        CurrencyFormat.format(BigDecimal.valueOf(customer.balance()), "id", "ID"));
    this._binding.productOrderTotalRow.customerBalance.setVisibility(View.VISIBLE);
    this._binding.productOrderTotalRow.customerDebtTitle.setText(
        this._context.getString(R.string.productordercard_customerdebt_title, croppedName));
    this._binding.productOrderTotalRow.customerDebtTitle.setVisibility(View.VISIBLE);
    this._binding.productOrderTotalRow.customerDebt.setText(
        CurrencyFormat.format(customer.debt(), "id", "ID"));
    this._binding.productOrderTotalRow.customerDebt.setTextColor(debtTextColor);
    this._binding.productOrderTotalRow.customerDebt.setVisibility(View.VISIBLE);
  }

  private void _setProductOrders(@NonNull List<ProductOrderModel> productOrders) {
    Objects.requireNonNull(productOrders);

    // Obtain both title and total typed row before clearing all the rows.
    final TableRow titleRow =
        this._binding.productOrderTable.findViewById(R.id.productOrderHeaderRow);
    final ConstraintLayout totalRow =
        this._binding.productOrderTable.findViewById(R.id.productOrderTotalRow);

    // Clear everything to ensure proper clean-up.
    this._binding.productOrderTable.removeAllViews();

    for (ProductOrderModel productOrder : productOrders) {
      final QueueCardExpandedProductOrderDataBinding dataRowBinding =
          QueueCardExpandedProductOrderDataBinding.inflate(LayoutInflater.from(this._context));
      final boolean isProductNameExists = productOrder.productName() != null;
      final boolean isProductPriceExists = productOrder.productPrice() != null;
      final int productTextColor =
          isProductNameExists && isProductPriceExists
              ? ContextCompat.getColor(this._context, R.color.text_enabled)
              : ContextCompat.getColor(this._context, R.color.text_disabled);

      final String productName =
          isProductNameExists
              ? productOrder.productName()
              : this._context.getString(R.string.symbol_notavailable);
      final String productPrice =
          isProductPriceExists
              ? CurrencyFormat.format(BigDecimal.valueOf(productOrder.productPrice()), "id", "ID")
              : this._context.getString(R.string.symbol_notavailable);
      final String quantity =
          CurrencyFormat.format(BigDecimal.valueOf(productOrder.quantity()), "id", "ID", "");
      final String totalPrice = CurrencyFormat.format(productOrder.totalPrice(), "id", "ID");

      dataRowBinding.productName.setText(productName);
      dataRowBinding.productName.setTextColor(productTextColor);
      dataRowBinding.productPrice.setText(productPrice);
      dataRowBinding.productPrice.setTextColor(productTextColor);
      dataRowBinding.quantity.setText(quantity);
      dataRowBinding.totalPrice.setText(totalPrice);
      this._binding.productOrderTable.addView(dataRowBinding.getRoot());
    }

    // Add them back.
    this._binding.productOrderTable.addView(titleRow, 0);
    this._binding.productOrderTable.addView(totalRow);
  }
}
