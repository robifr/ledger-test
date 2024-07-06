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

import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.robifr.ledger.R;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CreateQueueDate implements View.OnClickListener {
  @NonNull private final CreateQueueFragment _fragment;

  public CreateQueueDate(@NonNull CreateQueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._fragment.fragmentBinding().date.setOnClickListener(this);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.date -> {
        final MaterialDatePicker.Builder<Long> pickerBuilder =
            MaterialDatePicker.Builder.datePicker()
                .setTheme(
                    com.google.android.material.R.style.ThemeOverlay_Material3_MaterialCalendar)
                .setTitleText(this._fragment.getString(R.string.text_select_date))
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setCalendarConstraints(
                    new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now())
                        .build());
        pickerBuilder.setSelection(
            this._fragment
                .createQueueViewModel()
                .inputtedDate()
                .getValue()
                .toLocalDate()
                .atStartOfDay()
                .atZone(ZoneId.of("UTC")) // Material only accept UTC time.
                .toInstant()
                .toEpochMilli());

        final MaterialDatePicker<Long> picker = pickerBuilder.build();
        picker.show(this._fragment.getChildFragmentManager(), CreateQueueDate.class.toString());
        picker.addOnPositiveButtonClickListener(
            date ->
                this._fragment
                    .createQueueViewModel()
                    .onDateChanged(Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault())));
      }
    }
  }

  public void setInputtedDate(@NonNull ZonedDateTime date) {
    Objects.requireNonNull(date);

    final String formatted = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy").format(date);
    this._fragment.fragmentBinding().date.setText(formatted);
  }
}
