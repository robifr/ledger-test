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

import android.content.Context;
import androidx.annotation.NonNull;
import com.robifr.ledger.R;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

public class CurrencyFormat {
  public static final int MINIMUM_FRACTION_DIGITS = 0;
  public static final int MAXIMUM_FRACTION_DIGITS = 5;

  private CurrencyFormat() {}

  public static String format(@NonNull BigDecimal amount, @NonNull String languageTag) {
    return CurrencyFormat.format(amount, languageTag, CurrencyFormat.symbol(languageTag));
  }

  /**
   * @return Formatted amount into local specific currency.
   */
  @NonNull
  public static String format(
      @NonNull BigDecimal amount, @NonNull String languageTag, @NonNull String symbol) {
    Objects.requireNonNull(amount);
    Objects.requireNonNull(languageTag);
    Objects.requireNonNull(symbol);

    final DecimalFormat format =
        (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.forLanguageTag(languageTag));
    final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

    symbols.setCurrencySymbol(symbol);
    format.setRoundingMode(RoundingMode.DOWN);
    format.setMinimumFractionDigits(CurrencyFormat.MINIMUM_FRACTION_DIGITS);
    format.setMaximumFractionDigits(CurrencyFormat.MAXIMUM_FRACTION_DIGITS);
    format.setDecimalFormatSymbols(symbols);
    return format.format(amount);
  }

  /**
   * @see #formatWithUnit(Context, BigDecimal, String, String)
   */
  @NonNull
  public static String formatWithUnit(
      @NonNull Context context, @NonNull BigDecimal amount, @NonNull String languageTag) {
    return CurrencyFormat.formatWithUnit(
        context, amount, languageTag, CurrencyFormat.symbol(languageTag));
  }

  /**
   * @return Formatted amount into local specific currency with an appropriate suffix (such as K for
   *     thousands or M for millions) appended at the end of the string.
   */
  @NonNull
  public static String formatWithUnit(
      @NonNull Context context,
      @NonNull BigDecimal amount,
      @NonNull String languageTag,
      @NonNull String symbol) {
    Objects.requireNonNull(context);
    Objects.requireNonNull(amount);
    Objects.requireNonNull(languageTag);
    Objects.requireNonNull(symbol);

    final BigDecimal thousand = BigDecimal.valueOf(1000);
    final BigDecimal million = BigDecimal.valueOf(1_000_000);
    final BigDecimal billion = BigDecimal.valueOf(1_000_000_000);
    final BigDecimal trillion = BigDecimal.valueOf(1_000_000_000_000L);

    // Convert negative amount to positive to handle where negative amounts
    // can't be formatted due to division.
    final String negativePrefix = amount.compareTo(BigDecimal.ZERO) < 0 ? "-" : "";
    final BigDecimal positiveAmount = amount.abs();

    if (positiveAmount.compareTo(thousand) < 0) {
      return negativePrefix + CurrencyFormat.format(positiveAmount, languageTag, symbol);

    } else if (positiveAmount.compareTo(million) < 0) {
      return negativePrefix
          + CurrencyFormat.format(
              positiveAmount.divide(thousand, 1, RoundingMode.DOWN), languageTag, symbol)
          + context.getString(R.string.symbol_thousand);

    } else if (positiveAmount.compareTo(billion) < 0) {
      return negativePrefix
          + CurrencyFormat.format(
              positiveAmount.divide(million, 1, RoundingMode.DOWN), languageTag, symbol)
          + context.getString(R.string.symbol_million);

    } else if (positiveAmount.compareTo(trillion) < 0) {
      return negativePrefix
          + CurrencyFormat.format(
              positiveAmount.divide(billion, 1, RoundingMode.DOWN), languageTag, symbol)
          + context.getString(R.string.symbol_billion);
    }

    return negativePrefix
        + CurrencyFormat.format(
            positiveAmount.divide(trillion, 1, RoundingMode.DOWN), languageTag, symbol)
        + context.getString(R.string.symbol_trillion);
  }

  /**
   * @return Parsed amount from local specific currency.
   */
  @NonNull
  public static BigDecimal parse(@NonNull String amount, @NonNull String languageTag)
      throws ParseException {
    Objects.requireNonNull(amount);
    Objects.requireNonNull(languageTag);

    final DecimalFormat format =
        (DecimalFormat) NumberFormat.getNumberInstance(Locale.forLanguageTag(languageTag));
    format.setParseBigDecimal(true);

    final String decimalSeparator = CurrencyFormat.decimalSeparator(languageTag);
    String amountToParse = amount.replaceAll("[^\\d\\-\\" + decimalSeparator + "]", "");

    // Edge case.
    if (amountToParse.isBlank()
        || amountToParse.equals(decimalSeparator)
        || amountToParse.equals("-")
        || amountToParse.equals("-" + decimalSeparator)
        || Strings.countOccurrence(amountToParse, "-") > 1) {
      amountToParse = "0";
    }

    return ((BigDecimal) format.parse(amountToParse)).stripTrailingZeros();
  }

  @NonNull
  public static String symbol(@NonNull String languageTag) {
    Objects.requireNonNull(languageTag);

    return new DecimalFormatSymbols(Locale.forLanguageTag(languageTag)).getCurrencySymbol();
  }

  @NonNull
  public static String groupingSeparator(@NonNull String languageTag) {
    Objects.requireNonNull(languageTag);

    final char separator =
        new DecimalFormatSymbols(Locale.forLanguageTag(languageTag)).getGroupingSeparator();
    return Character.toString(separator);
  }

  @NonNull
  public static String decimalSeparator(@NonNull String languageTag) {
    Objects.requireNonNull(languageTag);

    final char separator =
        new DecimalFormatSymbols(Locale.forLanguageTag(languageTag)).getDecimalSeparator();
    return Character.toString(separator);
  }

  public static int countDecimalPlace(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    return Math.max(0, amount.stripTrailingZeros().scale());
  }

  public static boolean isSymbolAtStart(@NonNull String languageTag) {
    Objects.requireNonNull(languageTag);

    final DecimalFormat format =
        (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.forLanguageTag(languageTag));
    return format.toLocalizedPattern().indexOf('\u00A4') == 0;
  }
}
