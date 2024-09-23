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

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.CreateQueueDialogProductOrderBinding;
import com.robifr.ledger.ui.CurrencyTextWatcher;
import com.robifr.ledger.ui.selectproduct.SelectProductFragment;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class CreateQueueMakeProductOrder
    implements View.OnClickListener,
        DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener {
  @NonNull private final CreateQueueFragment _fragment;
  @NonNull private final CreateQueueDialogProductOrderBinding _dialogBinding;
  @NonNull private final AlertDialog _dialog;
  @NonNull private final QuantityTextWatcher _quantityTextWatcher;
  @NonNull private final DiscountTextWatcher _discountTextWatcher;

  public CreateQueueMakeProductOrder(@NonNull CreateQueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding =
        CreateQueueDialogProductOrderBinding.inflate(this._fragment.getLayoutInflater());
    this._dialog =
        new MaterialAlertDialogBuilder(this._fragment.requireContext())
            .setView(this._dialogBinding.getRoot())
            .setNegativeButton(this._fragment.getString(R.string.text_cancel), this)
            .setPositiveButton(this._fragment.getString(R.string.text_add), this)
            .create();
    this._quantityTextWatcher = new QuantityTextWatcher(this._dialogBinding.quantity, "id", "ID");
    this._discountTextWatcher = new DiscountTextWatcher(this._dialogBinding.discount, "id", "ID");

    this._dialog.setContentView(this._dialogBinding.getRoot());
    this._dialog.setOnDismissListener(this);
    this._dialogBinding.product.setOnClickListener(this);
    this._dialogBinding.quantity.addTextChangedListener(this._quantityTextWatcher);
    this._dialogBinding.discount.addTextChangedListener(this._discountTextWatcher);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.product -> {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(
            SelectProductFragment.Arguments.INITIAL_SELECTED_PRODUCT_PARCELABLE.key(),
            this._fragment
                .createQueueViewModel()
                .makeProductOrderView()
                .inputtedProduct()
                .getValue()
                .orElse(null));

        Navigation.findNavController(this._fragment.fragmentBinding().getRoot())
            .navigate(R.id.selectProductFragment, bundle);
        this._dialog.hide();
      }
    }
  }

  @Override
  public void onClick(@NonNull DialogInterface dialog, int buttonType) {
    Objects.requireNonNull(dialog);

    switch (buttonType) {
      case DialogInterface.BUTTON_POSITIVE -> {
        this._fragment.createQueueViewModel().makeProductOrderView().onSave();
        this._dialog.dismiss();
      }

      case DialogInterface.BUTTON_NEGATIVE -> this._dialog.dismiss();
    }
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    Objects.requireNonNull(dialog);

    this._fragment.createQueueViewModel().makeProductOrderView().onReset();
  }

  public void setInputtedProduct(@Nullable ProductModel product) {
    SpannableString text = new SpannableString("");

    if (product != null) {
      final String productName = product.name() + "\n";
      final String productPrice =
          CurrencyFormat.format(BigDecimal.valueOf(product.price()), "id", "ID");

      text = new SpannableString(productName + productPrice);
      // Set product price text smaller than its name.
      text.setSpan(
          new AbsoluteSizeSpan(
              this._fragment.getResources().getDimensionPixelSize(R.dimen.text_small)),
          productName.length(),
          productName.length() + productPrice.length(),
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      // Coloring product price text with gray.
      text.setSpan(
          new ForegroundColorSpan(this._fragment.requireContext().getColor(R.color.text_disabled)),
          productName.length(),
          productName.length() + productPrice.length(),
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    this._dialogBinding.product.setText(text);
  }

  public void setInputtedQuantityText(@NonNull String quantity) {
    Objects.requireNonNull(quantity);

    final String currentText = this._dialogBinding.quantity.getText().toString();
    if (currentText.equals(quantity)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.quantity.removeTextChangedListener(this._quantityTextWatcher);
    this._dialogBinding.quantity.setText(quantity);
    this._dialogBinding.quantity.setSelection(quantity.length());
    this._dialogBinding.quantity.addTextChangedListener(this._quantityTextWatcher);
  }

  public void setInputtedDiscountText(@NonNull String discount) {
    Objects.requireNonNull(discount);

    final String currentText = this._dialogBinding.discount.getText().toString();
    if (currentText.equals(discount)) return;

    // Remove listener to prevent any sort of formatting.
    this._dialogBinding.discount.removeTextChangedListener(this._discountTextWatcher);
    this._dialogBinding.discount.setText(discount);
    this._dialogBinding.discount.setSelection(discount.length());
    this._dialogBinding.discount.addTextChangedListener(this._discountTextWatcher);
  }

  public void setInputtedTotalPrice(@NonNull BigDecimal totalPrice) {
    Objects.requireNonNull(totalPrice);

    this._dialogBinding.totalPrice.setText(CurrencyFormat.format(totalPrice, "id", "ID"));
  }

  public void openCreateDialog() {
    this._dialogBinding.title.setText(this._fragment.getString(R.string.text_add_product_order));
    this._dialog.show();

    final boolean isProductInputted =
        this._fragment
            .createQueueViewModel()
            .makeProductOrderView()
            .inputtedProduct()
            .getValue()
            .isPresent();

    // Only invoke `AlertDialog#getButton()` after `AlertDialog.show()` called, otherwise NPE.
    final Button positiveButton = this._dialog.getButton(DialogInterface.BUTTON_POSITIVE);
    positiveButton.setText(this._fragment.getString(R.string.text_add));
    positiveButton.setEnabled(isProductInputted);
  }

  public void openEditDialog() {
    this.openCreateDialog();
    this._dialogBinding.title.setText(this._fragment.getString(R.string.text_edit_product_order));

    // Only invoke `AlertDialog#getButton()` after `AlertDialog.show()` called, otherwise NPE.
    final Button positiveButton = this._dialog.getButton(DialogInterface.BUTTON_POSITIVE);
    positiveButton.setText(this._fragment.getString(R.string.text_save));
  }

  private class QuantityTextWatcher extends CurrencyTextWatcher {
    public QuantityTextWatcher(
        @NonNull EditText view, @NonNull String language, @NonNull String country) {
      super(view, language, country);
      this._isSymbolHidden = true;
      this._maximumAmount = BigDecimal.valueOf(10_000L);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);
      CreateQueueMakeProductOrder.this
          ._fragment
          .createQueueViewModel()
          .makeProductOrderView()
          .onQuantityTextChanged(this.newText());
    }
  }

  private class DiscountTextWatcher extends CurrencyTextWatcher {
    public DiscountTextWatcher(
        @NonNull EditText view, @NonNull String language, @NonNull String country) {
      super(view, language, country);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);
      CreateQueueMakeProductOrder.this
          ._fragment
          .createQueueViewModel()
          .makeProductOrderView()
          .onDiscountTextChanged(this.newText());
    }
  }
}
