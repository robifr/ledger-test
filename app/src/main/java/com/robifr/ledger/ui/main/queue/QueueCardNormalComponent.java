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

package com.robifr.ledger.ui.main.queue;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.QueueCardWideNormalBinding;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class QueueCardNormalComponent {
  @NonNull private final Context _context;
  @NonNull private final QueueCardWideNormalBinding _binding;

  public QueueCardNormalComponent(
      @NonNull Context context, @NonNull QueueCardWideNormalBinding binding) {
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
    this._setGrandTotalPrice(queue.grandTotalPrice());
    this._setCustomer(queue.customer());
  }

  private void _setId(@Nullable Long id) {
    final boolean isIdExists = id != null;
    final String queueId =
        id != null ? id.toString() : this._context.getString(R.string.symbol_notavailable);

    this._binding.uniqueId.setText(queueId);
    this._binding.uniqueId.setEnabled(isIdExists);
  }

  private void _setDate(@NonNull ZonedDateTime date) {
    Objects.requireNonNull(date);

    this._binding.date.setText(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")));
  }

  private void _setStatus(@NonNull QueueModel.Status status) {
    Objects.requireNonNull(status);

    final int statusBackground = this._context.getColor(status.resourceBackgroundColor());

    this._binding.statusChip.setText(this._context.getString(status.resourceString()));
    this._binding.statusChip.setTextColor(this._context.getColor(status.resourceTextColor()));
    this._binding.statusChip.setChipBackgroundColor(ColorStateList.valueOf(statusBackground));
    this._binding.coloredSideline.setBackgroundColor(statusBackground);
  }

  private void _setGrandTotalPrice(@NonNull BigDecimal grandTotalPrice) {
    Objects.requireNonNull(grandTotalPrice);

    this._binding.grandTotalPrice.setText(CurrencyFormat.format(grandTotalPrice, "id", "ID"));
  }

  private void _setCustomer(@Nullable CustomerModel customer) {
    final boolean isCustomerExists = customer != null;
    final String customerName =
        isCustomerExists ? customer.name() : this._context.getString(R.string.symbol_notavailable);
    final String customerImageText =
        customer != null
            ? customer.name().trim().substring(0, Math.min(1, customer.name().trim().length()))
            : null;

    this._binding.customerName.setText(customerName);
    this._binding.customerName.setEnabled(isCustomerExists);
    this._binding.customerImage.text.setText(customerImageText);
  }
}
