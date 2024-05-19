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
