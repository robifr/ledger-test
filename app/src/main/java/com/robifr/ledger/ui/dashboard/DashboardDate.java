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

package com.robifr.ledger.ui.dashboard;

import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.databinding.DashboardDialogDateBinding;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

public class DashboardDate implements RadioGroup.OnCheckedChangeListener {
  @NonNull private final DashboardFragment _fragment;
  @NonNull private final DashboardDialogDateBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public DashboardDate(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = DashboardDialogDateBinding.inflate(this._fragment.getLayoutInflater());
    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);

    this._dialog.setContentView(this._dialogBinding.getRoot());
  }

  @Override
  public void onCheckedChanged(@NonNull RadioGroup group, int radioId) {
    Objects.requireNonNull(group);

    switch (group.getId()) {
      case R.id.radioGroup -> {
        final QueueDate.Range selectedDate =
            QueueDate.Range.valueOf(group.findViewById(radioId).getTag().toString());

        this._fragment.dashboardViewModel().onDateChanged(QueueDate.withRange(selectedDate));
        this._dialog.dismiss();
      }
    }
  }

  public void openDialog() {
    final QueueDate date = this._fragment.dashboardViewModel().date().getValue();

    // Remove listener to prevent callback being called during `clearCheck()`.
    this._dialogBinding.radioGroup.setOnCheckedChangeListener(null);
    this._dialogBinding.radioGroup.clearCheck();
    this._dialogBinding.radioGroup.setOnCheckedChangeListener(this);
    this._dialogBinding.customButton.setOnClickListener(view -> this._openDialogPicker());

    // Custom range uses classic button. They aren't supposed to get selected.
    if (date.range() != QueueDate.Range.CUSTOM) {
      this._dialogBinding.radioGroup.check(
          this._dialogBinding.radioGroup.findViewWithTag(date.range().toString()).getId());
    }

    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }

  private void _openDialogPicker() {
    final MaterialDatePicker<Pair<Long, Long>> picker =
        MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(com.google.android.material.R.style.ThemeOverlay_Material3_MaterialCalendar)
            .setTitleText(this._fragment.getString(R.string.text_select_date_range))
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build();

    picker.show(
        this._fragment.requireActivity().getSupportFragmentManager(), this.getClass().toString());
    picker.addOnPositiveButtonClickListener(
        date -> {
          this._fragment
              .dashboardViewModel()
              .onDateChanged(
                  QueueDate.withCustomRange(
                      Instant.ofEpochMilli(date.first).atZone(ZoneId.systemDefault()),
                      Instant.ofEpochMilli(date.second).atZone(ZoneId.systemDefault())));
          this._dialog.dismiss();
        });
  }
}
