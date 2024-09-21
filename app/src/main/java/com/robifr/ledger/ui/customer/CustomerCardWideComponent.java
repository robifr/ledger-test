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

package com.robifr.ledger.ui.customer;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardWideBinding;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class CustomerCardWideComponent {
  @NonNull private final Context _context;
  @NonNull private final CustomerCardWideBinding _binding;

  public CustomerCardWideComponent(
      @NonNull Context context, @NonNull CustomerCardWideBinding binding) {
    this._context = Objects.requireNonNull(context);
    this._binding = Objects.requireNonNull(binding);

    final ShapeAppearanceModel imageShape =
        ShapeAppearanceModel.builder(
                this._context,
                com.google.android.material.R.style.Widget_MaterialComponents_ShapeableImageView,
                R.style.Shape_Round)
            .build();
    this._binding.normalCard.image.shapeableImage.setShapeAppearanceModel(imageShape);
    this._binding.expandedCard.image.shapeableImage.setShapeAppearanceModel(imageShape);
  }

  public void setNormalCardCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._setId(customer.id(), true);
    this._setName(customer.name(), true);
    this._setBalance(customer.balance(), true);
    this._setDebt(customer.debt(), true);
  }

  public void setExpandedCardCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._setId(customer.id(), false);
    this._setName(customer.name(), false);
    this._setBalance(customer.balance(), false);
    this._setDebt(customer.debt(), false);
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

    this._binding.normalCard.name.setText(null);
    this._binding.normalCard.image.text.setText(null);
    this._binding.expandedCard.name.setText(null);
    this._binding.expandedCard.image.text.setText(null);

    this._binding.normalCard.balance.setText(null);
    this._binding.expandedCard.balance.setText(null);

    this._binding.normalCard.debt.setText(null);
    this._binding.normalCard.debt.setTextColor(0);
    this._binding.expandedCard.debt.setText(null);
    this._binding.expandedCard.debt.setTextColor(0);
  }

  private void _setId(@Nullable Long id, boolean isNormalCard) {
    final boolean isIdExists = id != null;
    final String customerId =
        isIdExists ? id.toString() : this._context.getString(R.string.symbol_notavailable);

    if (isNormalCard) {
      this._binding.normalCard.uniqueId.setText(customerId);
      this._binding.normalCard.uniqueId.setEnabled(isIdExists);
    } else {
      this._binding.expandedCard.uniqueId.setText(customerId);
      this._binding.expandedCard.uniqueId.setEnabled(isIdExists);
    }
  }

  private void _setName(@NonNull String name, boolean isNormalCard) {
    Objects.requireNonNull(name);

    final String initialLetterName = name.trim().substring(0, Math.min(1, name.trim().length()));

    if (isNormalCard) {
      this._binding.normalCard.name.setText(name);
      this._binding.normalCard.image.text.setText(initialLetterName);
    } else {
      this._binding.expandedCard.name.setText(name);
      this._binding.expandedCard.image.text.setText(initialLetterName);
    }
  }

  private void _setBalance(long balance, boolean isNormalCard) {
    final String formattedBalance = CurrencyFormat.format(BigDecimal.valueOf(balance), "id", "ID");

    if (isNormalCard) this._binding.normalCard.balance.setText(formattedBalance);
    else this._binding.expandedCard.balance.setText(formattedBalance);
  }

  private void _setDebt(@NonNull BigDecimal debt, boolean isNormalCard) {
    Objects.requireNonNull(debt);

    final String debtText = CurrencyFormat.format(debt, "id", "ID");
    final int debtTextColor =
        debt.compareTo(BigDecimal.ZERO) < 0
            // Negative debt will be shown red.
            ? this._context.getColor(R.color.red)
            : this._context.getColor(R.color.text_enabled);

    if (isNormalCard) {
      this._binding.normalCard.debt.setText(debtText);
      this._binding.normalCard.debt.setTextColor(debtTextColor);
    } else {
      this._binding.expandedCard.debt.setText(debtText);
      this._binding.expandedCard.debt.setTextColor(debtTextColor);
    }
  }
}
