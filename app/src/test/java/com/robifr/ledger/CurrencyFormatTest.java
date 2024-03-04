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

package com.robifr.ledger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class CurrencyFormatTest {
  @NonNull private final BigDecimal _amount = BigDecimal.valueOf(10_000.50);

  // Pair of language (first) and country (second).
  @NonNull private final Pair<String, String> _us = new Pair<>("en", "US");
  @NonNull private final Pair<String, String> _uk = new Pair<>("en", "GB");
  @NonNull private final Pair<String, String> _france = new Pair<>("fr", "FR");
  @NonNull private final Pair<String, String> _german = new Pair<>("de", "DE");
  @NonNull private final Pair<String, String> _indonesia = new Pair<>("id", "ID");
  @NonNull private final Pair<String, String> _japan = new Pair<>("ja", "JP");

  @Test
  public void format() {
    assertAll( // spotless:off
        () -> assertEquals("$10,000.5", CurrencyFormat.format(this._amount, this._us.first, this._us.second), "Wrong format for US currency"),
        () -> assertEquals("£10,000.5", CurrencyFormat.format(this._amount, this._uk.first, this._uk.second), "Wrong format for UK currency"),
        () -> assertEquals("10\u202F000,5\u00A0€", CurrencyFormat.format(this._amount, this._france.first, this._france.second), "Wrong format for France currency"),
        () -> assertEquals("10.000,5\u00A0€", CurrencyFormat.format(this._amount, this._german.first, this._german.second), "Wrong format for German currency"),
        () -> assertEquals("Rp10.000,5", CurrencyFormat.format(this._amount, this._indonesia.first, this._indonesia.second), "Wrong format for Indonesia currency"),
        () -> assertEquals("￥10,000.5", CurrencyFormat.format(this._amount, this._japan.first, this._japan.second), "Wrong format for Japan currency")
    ); // spotless:on
  }

  @Test
  public void parse() {
    final String decimalSeparator =
        CurrencyFormat.decimalSeparator(this._us.first, this._us.second);

    assertAll( // spotless:off
        () -> assertEquals(BigDecimal.ZERO, CurrencyFormat.parse("", this._us.first, this._us.second), "Parse to zero when there's only an empty string"),
        () -> assertEquals(BigDecimal.ZERO, CurrencyFormat.parse(" ", this._us.first, this._us.second), "Parse to zero when there's only a blank string"),
        () -> assertEquals(BigDecimal.ZERO, CurrencyFormat.parse("-", this._us.first, this._us.second), "Parse to zero when there's only a minus sign"),
        () -> assertEquals(BigDecimal.ZERO, CurrencyFormat.parse(decimalSeparator, this._us.first, this._us.second), "Parse to zero when there's only a decimal separator"),
        () -> assertEquals(BigDecimal.ZERO, CurrencyFormat.parse("-" + decimalSeparator, this._us.first, this._us.second), "Parse to zero when only '-" + decimalSeparator + "' presented"),
        () -> assertEquals(BigDecimal.ZERO, CurrencyFormat.parse("--1", this._us.first, this._us.second), "Parse to zero when there are multiple minus sign"),
        () -> assertEquals(this._amount, CurrencyFormat.parse(this._amount.toString() + "0", this._us.first, this._us.second), "Remove trailing zero when parsing"),

        () -> assertEquals(this._amount, CurrencyFormat.parse("$10,000.5", this._us.first, this._us.second), "Wrong parsing from US currency"),
        () -> assertEquals(this._amount, CurrencyFormat.parse("£10,000.5", this._uk.first, this._uk.second), "Wrong parsing from UK currency"),
        () -> assertEquals(this._amount, CurrencyFormat.parse("10\u202F000,5\u00A0€", this._france.first, this._france.second), "Wrong parsing from France currency"),
        () -> assertEquals(this._amount, CurrencyFormat.parse("10.000,5\u00A0€", this._german.first, this._german.second), "Wrong parsing from German currency"),
        () -> assertEquals(this._amount, CurrencyFormat.parse("Rp10.000,5", this._indonesia.first, this._indonesia.second), "Wrong parsing from Indonesia currency"),
        () -> assertEquals(this._amount, CurrencyFormat.parse("￥10,000.5", this._japan.first, this._japan.second), "Wrong parsing from Japan currency")
    ); // spotless:on
  }

  @Test
  public void symbolPosition() {
    assertAll( // spotless:off
        () -> assertTrue(CurrencyFormat.isSymbolAtStart(this._us.first, this._us.second), "US dollar symbol is at the start of the string"),
        () -> assertTrue(CurrencyFormat.isSymbolAtStart(this._uk.first, this._uk.second), "UK pound symbol is at the start of the string"),
        () -> assertFalse(CurrencyFormat.isSymbolAtStart(this._france.first, this._france.second), "France euro symbol is at the end of the string"),
        () -> assertFalse(CurrencyFormat.isSymbolAtStart(this._german.first, this._german.second), "German euro symbol is at the start of the string"),
        () -> assertTrue(CurrencyFormat.isSymbolAtStart(this._indonesia.first, this._indonesia.second), "Indonesia rupiah symbol is at the start of the string"),
        () -> assertTrue(CurrencyFormat.isSymbolAtStart(this._japan.first, this._japan.second), "Japan yen symbol is at the start of the string")
    ); // spotless:on
  }
}
