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

package com.robifr.ledger.ui.settings.viewmodel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.data.display.LanguageOption;
import com.robifr.ledger.repository.SettingsRepository;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
  @NonNull private final SettingsRepository _settingsRepository;
  @NonNull private final SafeMutableLiveData<LanguageOption> _languageUsed;

  @Inject
  public SettingsViewModel(@NonNull SettingsRepository settingsRepository) {
    this._settingsRepository = Objects.requireNonNull(settingsRepository);
    this._languageUsed = new SafeMutableLiveData<>(this._settingsRepository.languageUsed());
  }

  @NonNull
  public SafeLiveData<LanguageOption> languageUsed() {
    return this._languageUsed;
  }

  public void onLanguageChanged(@NonNull LanguageOption language) {
    Objects.requireNonNull(language);

    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(language.languageTag()));
    this._settingsRepository.saveLanguageUsed(language);
    this._languageUsed.setValue(language);
  }
}
