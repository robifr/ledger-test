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

package com.robifr.ledger.ui.queue.filter;

import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.robifr.ledger.data.QueueFilters;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.QueueDialogFilterBinding;
import com.robifr.ledger.ui.queue.QueueFragment;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class QueueFilterStatus implements ChipGroup.OnCheckedStateChangeListener {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public QueueFilterStatus(
      @NonNull QueueFragment fragment,
      @NonNull QueueDialogFilterBinding dialogBinding,
      @NonNull BottomSheetDialog dialog) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = Objects.requireNonNull(dialogBinding);
    this._dialog = Objects.requireNonNull(dialog);

    this._dialogBinding.filterStatus.chipGroup.setOnCheckedStateChangeListener(this);
  }

  @Override
  public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
    Objects.requireNonNull(group);
    Objects.requireNonNull(checkedIds);

    final HashSet<QueueModel.Status> status = new HashSet<>();

    checkedIds.forEach(
        id -> status.add(QueueModel.Status.valueOf(group.findViewById(id).getTag().toString())));
    this._fragment.queueViewModel().filterView().onStatusChanged(status);
  }

  /**
   * @param status {@link QueueFilters#filteredStatus()}
   */
  public void setFilteredStatus(@NonNull Set<QueueModel.Status> status) {
    Objects.requireNonNull(status);

    for (int i = 0; i < this._dialogBinding.filterStatus.chipGroup.getChildCount(); i++) {
      final Chip chip = (Chip) this._dialogBinding.filterStatus.chipGroup.getChildAt(i);

      // Remove listener to prevent unintended updates to both view model
      // and the chip itself when manually set status.
      this._dialogBinding.filterStatus.chipGroup.setOnCheckedStateChangeListener(null);
      chip.setChecked(status.contains(QueueModel.Status.valueOf(chip.getTag().toString())));
      this._dialogBinding.filterStatus.chipGroup.setOnCheckedStateChangeListener(this);
    }
  }
}
