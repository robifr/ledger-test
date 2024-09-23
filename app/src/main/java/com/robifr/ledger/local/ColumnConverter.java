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
