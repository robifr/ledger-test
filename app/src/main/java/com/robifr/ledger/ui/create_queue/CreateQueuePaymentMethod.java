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

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.button.MaterialButton;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.QueueModel;
import java.util.Objects;
import java.util.Set;

public class CreateQueuePaymentMethod implements View.OnClickListener {
  @NonNull private final CreateQueueFragment _fragment;

  public CreateQueuePaymentMethod(@NonNull CreateQueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._fragment.fragmentBinding().paymentMethodCashButton.setOnClickListener(this);
    this._fragment.fragmentBinding().paymentMethodAccountBalanceButton.setOnClickListener(this);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.paymentMethodCashButton, R.id.paymentMethodAccountBalanceButton -> {
        this._fragment
            .createQueueViewModel()
            .onPaymentMethodChanged(QueueModel.PaymentMethod.valueOf(view.getTag().toString()));
      }
    }
  }

  public void setInputtedPaymentMethod(@NonNull QueueModel.PaymentMethod paymentMethod) {
    Objects.requireNonNull(paymentMethod);

    // Unselect all buttons other than given payment method.
    for (QueueModel.PaymentMethod payment : QueueModel.PaymentMethod.values()) {
      final MaterialButton button =
          this._fragment.fragmentBinding().getRoot().findViewWithTag(payment.toString());

      if (payment == paymentMethod) this._selectButton(button);
      else this._unselectButton(button);
    }
  }

  public void setEnabledButtons(@NonNull Set<QueueModel.PaymentMethod> paymentMethods) {
    Objects.requireNonNull(paymentMethods);

    for (QueueModel.PaymentMethod paymentMethod : QueueModel.PaymentMethod.values()) {
      final MaterialButton button =
          this._fragment.fragmentBinding().getRoot().findViewWithTag(paymentMethod.toString());

      final Drawable[] buttonIcons = button.getCompoundDrawables();
      final ColorStateList buttonColor =
          ContextCompat.getColorStateList(
              this._fragment.requireContext(), R.color.selector_textview);

      DrawableCompat.setTintList(buttonIcons[0], buttonColor);
      button.setEnabled(paymentMethods.contains(paymentMethod));
      // Index 0 is payment method icon, index 2 is checkmark icon.
      button.setCompoundDrawables(buttonIcons[0], null, buttonIcons[2], null);
    }
  }

  public void setVisible(boolean isVisible) {
    final int visibility = isVisible ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().paymentMethodTitle.setVisibility(visibility);
    this._fragment.fragmentBinding().paymentMethodCashButton.setVisibility(visibility);
    this._fragment.fragmentBinding().paymentMethodAccountBalanceButton.setVisibility(visibility);
  }

  private void _selectButton(@NonNull MaterialButton button) {
    Objects.requireNonNull(button);

    button.setCompoundDrawablesWithIntrinsicBounds(
        button.getCompoundDrawables()[0], // Payment method icon.
        null,
        ContextCompat.getDrawable(this._fragment.requireContext(), R.drawable.icon_check_20),
        null);
    button.setChecked(true);
  }

  private void _unselectButton(@NonNull MaterialButton button) {
    Objects.requireNonNull(button);

    button.setCompoundDrawablesWithIntrinsicBounds(
        button.getCompoundDrawables()[0], // Payment method icon.
        null,
        null,
        null);
    button.setChecked(false);
  }
}
