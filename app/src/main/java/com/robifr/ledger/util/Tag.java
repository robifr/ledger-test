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

package com.robifr.ledger.util;

import androidx.annotation.NonNull;
import java.util.Objects;

public class Tag {
  private Tag() {}

  @NonNull
  public static String simpleName(@NonNull Class<?> cls) {
    Objects.requireNonNull(cls);

    return cls.getEnclosingClass() != null
        ? cls.getEnclosingClass().getSimpleName() + "." + cls.getSimpleName()
        : cls.getSimpleName();
  }

  @NonNull
  public static String simpleName(@NonNull Enum<?> e) {
    Objects.requireNonNull(e);

    return Tag.simpleName(e.getClass()) + "." + e.name();
  }

  @NonNull
  public static String fullName(@NonNull Class<?> cls) {
    Objects.requireNonNull(cls);

    return cls.getEnclosingClass() != null
        ? cls.getEnclosingClass().getName() + "." + cls.getSimpleName()
        : cls.getName();
  }

  @NonNull
  public static String fullName(@NonNull Enum<?> e) {
    Objects.requireNonNull(e);

    return Tag.fullName(e.getClass()) + "." + e.name();
  }
}
