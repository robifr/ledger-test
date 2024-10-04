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

package com.robifr.ledger.ui.customer;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
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

  public void setCardChecked(boolean isChecked) {
    final int textVisibility = isChecked ? View.GONE : View.VISIBLE;
    final int iconVisibility = isChecked ? View.VISIBLE : View.GONE;

    this._binding.cardView.setChecked(isChecked);
    this._binding.normalCard.image.text.setVisibility(textVisibility);
    this._binding.normalCard.image.icon.setVisibility(iconVisibility);
    this._binding.expandedCard.image.text.setVisibility(textVisibility);
    this._binding.expandedCard.image.icon.setVisibility(iconVisibility);
  }

  public void reset() {
    this._binding.normalCard.uniqueId.setText(null);
    this._binding.normalCard.uniqueId.setEnabled(false);
    this._binding.expandedCard.uniqueId.setText(null);
    this._binding.expandedCard.uniqueId.setEnabled(false);

    this._binding.normalCard.name.setText(null);
    this._binding.normalCard.image.text.setText(null);
    this._binding.normalCard.image.icon.setVisibility(View.GONE);
    this._binding.expandedCard.name.setText(null);
    this._binding.expandedCard.image.text.setText(null);
    this._binding.expandedCard.image.icon.setVisibility(View.GONE);

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
        isIdExists ? id.toString() : this._context.getString(R.string.symbol_notAvailable);

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
    final String formattedBalance =
        CurrencyFormat.format(
            BigDecimal.valueOf(balance),
            AppCompatDelegate.getApplicationLocales().toLanguageTags());

    if (isNormalCard) this._binding.normalCard.balance.setText(formattedBalance);
    else this._binding.expandedCard.balance.setText(formattedBalance);
  }

  private void _setDebt(@NonNull BigDecimal debt, boolean isNormalCard) {
    Objects.requireNonNull(debt);

    final String debtText =
        CurrencyFormat.format(debt, AppCompatDelegate.getApplicationLocales().toLanguageTags());
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
