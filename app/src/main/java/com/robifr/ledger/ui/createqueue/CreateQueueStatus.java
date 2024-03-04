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

package com.robifr.ledger.ui.createqueue;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.CreateQueueDialogStatusBinding;
import java.util.Objects;

public class CreateQueueStatus implements RadioGroup.OnCheckedChangeListener {
  @NonNull private final CreateQueueFragment _fragment;
  @NonNull private final BottomSheetDialog _dialog;
  @NonNull private final CreateQueueDialogStatusBinding _dialogBinding;

  public CreateQueueStatus(@NonNull CreateQueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);
    this._dialogBinding =
        CreateQueueDialogStatusBinding.inflate(this._fragment.getLayoutInflater());

    this._dialog.setContentView(this._dialogBinding.getRoot());
    this._fragment.fragmentBinding().status.setOnClickListener(editText -> this._openDialog());
  }

  @Override
  public void onCheckedChanged(@NonNull RadioGroup group, int radioId) {
    Objects.requireNonNull(group);

    final QueueModel.Status inputtedStatus =
        this._fragment.createQueueViewModel().inputtedStatus().getValue();
    if (inputtedStatus == null) return;

    switch (group.getId()) {
      case R.id.radioGroup -> {
        final QueueModel.Status status =
            QueueModel.Status.valueOf(group.findViewById(radioId).getTag().toString());

        this._fragment.createQueueViewModel().onStatusChanged(status);
        this._dialog.dismiss();
      }
    }
  }

  public void setInputtedStatus(@NonNull QueueModel.Status status) {
    Objects.requireNonNull(status);

    this._fragment
        .fragmentBinding()
        .status
        .setText(this._fragment.getString(status.resourceString()));
  }

  private void _openDialog() {
    // Setting up initial state radio, invisible icon and background tint.
    for (QueueModel.Status status : QueueModel.Status.values()) {
      this._unselectRadio(this._dialogBinding.radioGroup.findViewWithTag(status.toString()));
    }

    final QueueModel.Status inputtedStatus =
        this._fragment.createQueueViewModel().inputtedStatus().getValue();

    if (inputtedStatus != null) {
      final RadioButton initialRadio =
          this._dialogBinding.radioGroup.findViewWithTag(inputtedStatus.toString());

      this._dialogBinding.radioGroup.check(initialRadio.getId());
      this._selectRadio(initialRadio);
    }

    this._dialogBinding.radioGroup.setOnCheckedChangeListener(this);
    this._dialog.show();
  }

  private void _selectRadio(@NonNull RadioButton radio) {
    Objects.requireNonNull(radio);

    final TypedValue backgroundColor = new TypedValue();
    this._fragment
        .requireActivity()
        .getTheme()
        .resolveAttribute(androidx.appcompat.R.attr.colorAccent, backgroundColor, true);

    final Drawable leftIcon =
        Objects.requireNonNull(this._fragment.requireContext().getDrawable(R.drawable.icon_check));
    leftIcon.setAlpha(255); // Bring back icon to be visible.

    radio.setBackgroundTintList(
        ColorStateList.valueOf(
            // Use background tint to maintain ripple and rounded corner of drawable shape.
            this._fragment.requireContext().getColor(backgroundColor.resourceId)));
    radio.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, null, null);
  }

  private void _unselectRadio(@NonNull RadioButton radio) {
    Objects.requireNonNull(radio);

    final TypedValue backgroundColor = new TypedValue();
    this._fragment
        .requireActivity()
        .getTheme()
        .resolveAttribute(com.google.android.material.R.attr.colorSurface, backgroundColor, true);

    final Drawable leftIcon =
        Objects.requireNonNull(this._fragment.requireContext().getDrawable(R.drawable.icon_check));
    leftIcon.setAlpha(0); // Hide icon, so that we can reserve its space width.

    radio.setBackgroundTintList(
        ColorStateList.valueOf(
            // Use background tint to maintain ripple and rounded corner of drawable shape.
            this._fragment.requireContext().getColor(backgroundColor.resourceId)));
    radio.setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, null, null);
  }
}
