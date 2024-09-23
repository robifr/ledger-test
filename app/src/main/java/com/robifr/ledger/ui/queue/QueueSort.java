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

package com.robifr.ledger.ui.queue;

import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.QueueSortMethod;
import com.robifr.ledger.databinding.QueueDialogSortByBinding;
import java.util.Objects;

public class QueueSort implements RadioButton.OnClickListener {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueDialogSortByBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public QueueSort(@NonNull QueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = QueueDialogSortByBinding.inflate(fragment.getLayoutInflater());
    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);

    this._dialog.setContentView(this._dialogBinding.getRoot());
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.customerNameRadioButton, R.id.dateRadioButton, R.id.totalPriceRadioButton -> {
        this._fragment
            .queueViewModel()
            .onSortMethodChanged(QueueSortMethod.SortBy.valueOf(view.getTag().toString()));
        this._dialog.dismiss();
      }
    }
  }

  public void openDialog() {
    for (QueueSortMethod.SortBy sortBy : QueueSortMethod.SortBy.values()) {
      // Don't use `RadioGroup.OnCheckedChangeListener` interface,
      // cause that wouldn't work when user re-select same radio to revert sort order.
      this._dialogBinding.radioGroup.findViewWithTag(sortBy.toString()).setOnClickListener(this);
    }

    final QueueSortMethod sortMethod = this._fragment.queueViewModel().sortMethod().getValue();
    final RadioButton initialRadio =
        this._dialogBinding.radioGroup.findViewWithTag(sortMethod.sortBy().toString());

    this._dialogBinding.radioGroup.check(initialRadio.getId());
    this._updateRadioIcon(initialRadio, sortMethod.isAscending());
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }

  private void _updateRadioIcon(@NonNull RadioButton radio, boolean isAscending) {
    Objects.requireNonNull(radio);

    final int icon = isAscending ? R.drawable.icon_arrow_upward : R.drawable.icon_arrow_downward;
    final StateListDrawable radioIcon = new StateListDrawable();

    radioIcon.addState(
        new int[] {android.R.attr.state_checked},
        this._fragment.requireContext().getDrawable(icon));
    radioIcon.addState(
        new int[] {},
        this._fragment.requireContext().getDrawable(R.drawable.icon_radio_check_hideable));
    radio.setCompoundDrawablesWithIntrinsicBounds(radioIcon, null, null, null);
  }
}
