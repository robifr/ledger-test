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

package com.robifr.ledger.data.display;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.robifr.ledger.R;
import java.util.Objects;

public enum LanguageOption {
  ENGLISH_US("en-US", R.string.enum_languageOption_englishUs),
  INDONESIA("id-ID", R.string.enum_languageOption_indonesia);

  @NonNull private final String _languageTag;
  @StringRes private final int _resourceString;

  private LanguageOption(@NonNull String languageTag, @StringRes int resourceString) {
    this._languageTag = Objects.requireNonNull(languageTag);
    this._resourceString = resourceString;
  }

  @NonNull
  public String languageTag() {
    return this._languageTag;
  }

  @StringRes
  public int resourceString() {
    return this._resourceString;
  }
}
