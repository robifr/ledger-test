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
    final QueueModel.Status inputtedStatus =
        this._fragment.createQueueViewModel().inputtedStatus().getValue();

    if (inputtedStatus != null) {
      this._dialogBinding.radioGroup.check(
          this._dialogBinding.radioGroup.findViewWithTag(inputtedStatus.toString()).getId());
    }

    this._dialogBinding.radioGroup.setOnCheckedChangeListener(this);
    this._dialog.show();
  }
}
