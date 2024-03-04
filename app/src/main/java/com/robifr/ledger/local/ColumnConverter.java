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

package com.robifr.ledger.local;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class ColumnConverter {
  private ColumnConverter() {}

  public static class InstantConverter {
    @NonNull
    @TypeConverter
    public static Instant toInstant(@NonNull String date) {
      Objects.requireNonNull(date);

      return Instant.parse(date);
    }

    @NonNull
    @TypeConverter
    public static String fromInstant(@NonNull Instant date) {
      Objects.requireNonNull(date);

      return date.toString();
    }
  }

  public static class BigDecimalConverter {
    @NonNull
    @TypeConverter
    public static BigDecimal toBigDecimal(@NonNull String number) {
      Objects.requireNonNull(number);

      return new BigDecimal(number);
    }

    @NonNull
    @TypeConverter
    public static String fromBigDecimal(@NonNull BigDecimal number) {
      Objects.requireNonNull(number);

      return number.toString();
    }
  }

  /**
   * @apiNote This converter can't be used along with {@link TypeConverters} annotation. You have to
   *     manually convert the string before inserting them as FTS row.
   */
  public static class FtsStringConverter {
    /**
     * Adding whitespace after character except when the character is whitespace itself. Because FTS
     * will only search by prefix, so that every single character could be a prefix. e.g. Assuming
     * {@code _} is a whitespace, {@code "abc_def"} becomes {@code "a_b_c__d_e_f_"}.
     */
    @NonNull
    public static String toFtsSpacedString(@NonNull String str) {
      Objects.requireNonNull(str);

      return str.replaceAll("(?<=.)(?<![$\\s])", " ");
    }

    /**
     * Remove whitespace after character except when the character is whitespace itself. e.g.
     * Assuming {@code _} is a whitespace, {@code "a_b_c__d_e_f_"} becomes {@code "abc_def"}.
     */
    @NonNull
    public static String fromFtsSpacedString(@NonNull String str) {
      Objects.requireNonNull(str);

      return str.replaceAll("\\s(?=\\S|$)", "");
    }
  }
}
