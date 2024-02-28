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

package com.robifr.ledger.ui.main.product;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.data.ProductSortMethod;
import com.robifr.ledger.databinding.ProductDialogSortByBinding;
import java.util.Objects;

public class ProductSort implements RadioButton.OnClickListener {
  @NonNull private final ProductFragment _fragment;
  @NonNull private final ProductDialogSortByBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public ProductSort(@NonNull ProductFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = ProductDialogSortByBinding.inflate(fragment.getLayoutInflater());
    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);

    this._dialog.setContentView(this._dialogBinding.getRoot());
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.nameRadioButton, R.id.priceRadioButton -> {
        this._fragment
            .productViewModel()
            .onSortMethodChanged(ProductSortMethod.SortBy.valueOf(view.getTag().toString()));
        this._dialog.dismiss();
      }
    }
  }

  public void openDialog() {
    final ProductSortMethod sortMethod = this._fragment.productViewModel().sortMethod().getValue();
    if (sortMethod == null) return;

    // Setting up initial state radio, invisible icon and background tint.
    for (ProductSortMethod.SortBy sortBy : ProductSortMethod.SortBy.values()) {
      final RadioButton radio = this._dialogBinding.radioGroup.findViewWithTag(sortBy.toString());
      // Don't use `RadioGroup#OnCheckedChangeListener` interface,
      // cause that wouldn't work when user re-select same radio to revert sort order.
      radio.setOnClickListener(this);
      this._unselectRadio(radio, sortMethod.isAscending());
    }

    final RadioButton initialRadio =
        this._dialogBinding.radioGroup.findViewWithTag(sortMethod.sortBy().toString());

    this._dialogBinding.radioGroup.check(initialRadio.getId());
    this._selectRadio(initialRadio, sortMethod.isAscending());
    this._dialog.show();
  }

  private void _selectRadio(@NonNull RadioButton radio, boolean isAscending) {
    Objects.requireNonNull(radio);

    final TypedValue backgroundColor = new TypedValue();
    this._fragment
        .requireContext()
        .getTheme()
        .resolveAttribute(androidx.appcompat.R.attr.colorAccent, backgroundColor, true);

    final int icon =
        isAscending ? R.drawable.icon_arrow_upward_20 : R.drawable.icon_arrow_downward_20;
    final Drawable leftIcon =
        Objects.requireNonNull(this._fragment.requireContext().getDrawable(icon));

    leftIcon.setAlpha(255); // Bring back icon to be visible.
    radio.setBackgroundTintList(
        ColorStateList.valueOf(
            // Use background tint to maintain ripple and rounded corner of drawable shape.
            this._fragment.requireContext().getColor(backgroundColor.resourceId)));
    radio.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, null, null);
  }

  private void _unselectRadio(@NonNull RadioButton radio, boolean isAscending) {
    Objects.requireNonNull(radio);

    final TypedValue backgroundColor = new TypedValue();
    this._fragment
        .requireContext()
        .getTheme()
        .resolveAttribute(com.google.android.material.R.attr.colorSurface, backgroundColor, true);

    final int icon =
        isAscending ? R.drawable.icon_arrow_upward_20 : R.drawable.icon_arrow_downward_20;
    final Drawable leftIcon =
        Objects.requireNonNull(this._fragment.requireContext().getDrawable(icon));

    leftIcon.setAlpha(0); // Hide icon, so that we can reserve its space width.
    radio.setBackgroundTintList(
        ColorStateList.valueOf(
            // Use background tint to maintain ripple and rounded corner of drawable shape.
            this._fragment.requireContext().getColor(backgroundColor.resourceId)));
    radio.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, null, null);
  }
}
