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

package com.robifr.ledger.repository;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.display.LanguageOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SettingsRepository {
  @NonNull private static final String _NAME = "com.robifr.ledger.settingsprefs";
  @NonNull private static final String _KEY_LANGUAGE_USED = "language_used";
  @Nullable private static SettingsRepository _instance;
  @NonNull private final SharedPreferences _sharedPreferences;

  private SettingsRepository(@NonNull SharedPreferences sharedPreferences) {
    this._sharedPreferences = Objects.requireNonNull(sharedPreferences);
  }

  @NonNull
  public static synchronized SettingsRepository instance(@NonNull Context context) {
    Objects.requireNonNull(context);

    return SettingsRepository._instance =
        SettingsRepository._instance == null
            ? new SettingsRepository(
                context
                    .getApplicationContext()
                    .getSharedPreferences(SettingsRepository._NAME, Context.MODE_PRIVATE))
            : SettingsRepository._instance;
  }

  @NonNull
  public LanguageOption languageUsed() {
    final String languagePrefs =
        this._sharedPreferences.getString(
            SettingsRepository._KEY_LANGUAGE_USED, LanguageOption.ENGLISH_US.languageTag());
    return Arrays.stream(LanguageOption.values())
        .filter(e -> e.languageTag().equals(languagePrefs))
        .findFirst()
        .orElse(LanguageOption.ENGLISH_US);
  }

  public CompletableFuture<Boolean> saveLanguageUsed(@NonNull LanguageOption language) {
    Objects.requireNonNull(language);

    return CompletableFuture.supplyAsync(
        () ->
            this._sharedPreferences
                .edit()
                .putString(SettingsRepository._KEY_LANGUAGE_USED, language.languageTag())
                .commit());
  }
}
