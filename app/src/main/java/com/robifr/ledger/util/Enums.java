/**
 * Copyright (c) 2022-present Robi
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
import androidx.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public class Enums {
  private Enums() {}

  /**
   * Retrieve enum by its value, comparing the provided enum method functional interface.
   *
   * <pre>{@code
   * final int flagValue = ...
   * Enums.valueOf(flagValue, Flags.class, Flags::flagValue);
   * }</pre>
   *
   * @param value Enum value to be compared with {@code extractor}.
   * @param cls Enum class.
   * @param extractor Enum method to be compared with {@code value}
   */
  @Nullable
  public static <V, E extends Enum<E>> E valueOf(V value, Class<E> cls, Function<E, V> extractor) {
    Objects.requireNonNull(value);
    Objects.requireNonNull(cls);
    Objects.requireNonNull(extractor);
    Objects.requireNonNull(cls.getEnumConstants());

    for (E item : cls.getEnumConstants()) {
      if (extractor.apply(item).equals(value)) {
        return item;
      }
    }

    return null;
  }

  /** Retrieve enum by its exact constant name. */
  @Nullable
  public static <E extends Enum<E>> E nameOf(@Nullable String name, @NonNull Class<E> cls) {
    Objects.requireNonNull(cls);
    Objects.requireNonNull(cls.getEnumConstants());

    return Arrays.stream(cls.getEnumConstants())
        .filter(enumerate -> enumerate.toString().equals(name))
        .findFirst()
        .orElse(null);
  }
}
