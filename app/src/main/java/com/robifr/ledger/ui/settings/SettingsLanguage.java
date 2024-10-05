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

package com.robifr.ledger.ui.settings;

import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.LanguageOption;
import com.robifr.ledger.databinding.SettingsDialogLanguageBinding;
import com.robifr.ledger.databinding.SettingsGeneralBinding;
import java.util.Objects;

public class SettingsLanguage implements RadioGroup.OnCheckedChangeListener {
  @NonNull private final SettingsFragment _fragment;
  @NonNull private final SettingsGeneralBinding _generalBinding;
  @NonNull private final SettingsDialogLanguageBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public SettingsLanguage(@NonNull SettingsFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._generalBinding = SettingsGeneralBinding.bind(fragment.fragmentBinding().getRoot());
    this._dialogBinding = SettingsDialogLanguageBinding.inflate(fragment.getLayoutInflater());
    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);

    this._dialog.setContentView(this._dialogBinding.getRoot());
    this._generalBinding.languageLayout.setOnClickListener(view -> this._openDialog());
  }

  @Override
  public void onCheckedChanged(@NonNull RadioGroup group, int radioId) {
    Objects.requireNonNull(group);

    switch (group.getId()) {
      case R.id.radioGroup -> {
        this._fragment
            .settingsViewModel()
            .onLanguageChanged(
                LanguageOption.valueOf(group.findViewById(radioId).getTag().toString()));
        this._dialog.dismiss();
      }
    }
  }

  public void setLanguageUsed(@NonNull LanguageOption language) {
    Objects.requireNonNull(language);

    this._generalBinding.language.setText(language.resourceString());
  }

  private void _openDialog() {
    final LanguageOption language = this._fragment.settingsViewModel().languageUsed().getValue();

    this._dialogBinding.radioGroup.check(
        this._dialogBinding.radioGroup.findViewWithTag(language.toString()).getId());
    this._dialogBinding.radioGroup.setOnCheckedChangeListener(this);
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }
}
