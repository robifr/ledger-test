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

import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

    this._dialogBinding.radioGroup.check(
        this._dialogBinding.radioGroup.findViewWithTag(inputtedStatus.toString()).getId());
    this._dialogBinding.radioGroup.setOnCheckedChangeListener(this);
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }
}
