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

package com.robifr.ledger.ui.queue;

import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.data.QueueSortMethod;
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
    final QueueSortMethod sortMethod = this._fragment.queueViewModel().sortMethod().getValue();
    if (sortMethod == null) return;

    for (QueueSortMethod.SortBy sortBy : QueueSortMethod.SortBy.values()) {
      // Don't use `RadioGroup.OnCheckedChangeListener` interface,
      // cause that wouldn't work when user re-select same radio to revert sort order.
      this._dialogBinding.radioGroup.findViewWithTag(sortBy.toString()).setOnClickListener(this);
    }

    final RadioButton initialRadio =
        this._dialogBinding.radioGroup.findViewWithTag(sortMethod.sortBy().toString());

    this._dialogBinding.radioGroup.check(initialRadio.getId());
    this._updateRadioIcon(initialRadio, sortMethod.isAscending());
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
