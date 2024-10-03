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

package com.robifr.ledger.ui.queue.filter;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.display.QueueFilters;
import com.robifr.ledger.databinding.QueueDialogFilterBinding;
import com.robifr.ledger.ui.queue.QueueFragment;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class QueueFilterDate implements ChipGroup.OnCheckedStateChangeListener {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public QueueFilterDate(
      @NonNull QueueFragment fragment,
      @NonNull QueueDialogFilterBinding dialogBinding,
      @NonNull BottomSheetDialog dialog) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = Objects.requireNonNull(dialogBinding);
    this._dialog = Objects.requireNonNull(dialog);

    this._dialogBinding.filterDate.chipGroup.setOnCheckedStateChangeListener(this);
    this._dialogBinding.filterDate.customDateButton.setOnClickListener(button -> this.openDialog());
  }

  @Override
  public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
    Objects.requireNonNull(group);
    Objects.requireNonNull(checkedIds);

    for (Integer id : checkedIds) {
      final QueueDate.Range selectedDate =
          QueueDate.Range.valueOf(group.findViewById(id).getTag().toString());

      this._fragment.queueViewModel().filterView().onDateChanged(QueueDate.withRange(selectedDate));
      break; // Chip group should only be allowed to select single chip at time.
    }
  }

  /**
   * @param date {@link QueueFilters#filteredDate()}
   */
  public void setFilteredDate(@NonNull QueueDate date) {
    Objects.requireNonNull(date);

    // Remove listener to prevent unintended updates to both view model and the chip itself
    // when manually set the date, like `QueueDate.Range#CUSTOM`.
    this._dialogBinding.filterDate.chipGroup.setOnCheckedStateChangeListener(null);
    this._dialogBinding
        .filterDate
        .chipGroup
        .<Chip>findViewWithTag(date.range().toString())
        .setChecked(true);
    this._dialogBinding.filterDate.chipGroup.setOnCheckedStateChangeListener(this);

    final DateTimeFormatter format = DateTimeFormatter.ofPattern("d MMM yyyy");
    final Chip customRangeChip =
        this._dialogBinding.filterDate.chipGroup.findViewWithTag(QueueDate.Range.CUSTOM.toString());
    final int customRangeVisibility =
        date.range() == QueueDate.Range.CUSTOM ? View.VISIBLE : View.GONE;

    // Hide custom range chip when it's not being selected, and show otherwise.
    customRangeChip.setVisibility(customRangeVisibility);
    customRangeChip.setText(
        this._fragment.getString(
            R.string.queuefilter_date_selecteddate_chip,
            date.dateStart().format(format),
            date.dateEnd().format(format)));
  }

  public void openDialog() {
    final MaterialDatePicker<Pair<Long, Long>> picker =
        MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(com.google.android.material.R.style.ThemeOverlay_Material3_MaterialCalendar)
            .setTitleText(this._fragment.getString(R.string.text_select_date_range))
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build();

    picker.show(
        this._fragment.requireActivity().getSupportFragmentManager(), this.getClass().toString());
    picker.addOnPositiveButtonClickListener(
        date ->
            this._fragment
                .queueViewModel()
                .filterView()
                .onDateChanged(
                    QueueDate.withCustomRange(
                        Instant.ofEpochMilli(date.first).atZone(ZoneId.systemDefault()),
                        Instant.ofEpochMilli(date.second).atZone(ZoneId.systemDefault()))));
  }
}
