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
                .setTitleText(R.string.createQueue_date_selectDate)
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
