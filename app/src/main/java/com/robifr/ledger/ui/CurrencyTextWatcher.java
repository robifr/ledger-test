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

package com.robifr.ledger.ui;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.robifr.ledger.util.CurrencyFormat;
import com.robifr.ledger.util.Strings;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Objects;

public class CurrencyTextWatcher extends EditTextWatcher {
  @NonNull protected final String _language;
  @NonNull protected final String _country;
  @NonNull protected BigDecimal _maximumAmount = BigDecimal.valueOf(Integer.MAX_VALUE);
  protected boolean _isSymbolHidden = false;

  public CurrencyTextWatcher(
      @NonNull EditText view, @NonNull String language, @NonNull String country) {
    super(view);
    this._language = Objects.requireNonNull(language);
    this._country = Objects.requireNonNull(country);
  }

  public void afterTextChanged(@NonNull Editable editable) {
    // WARNING! Never ever mess with this method unless you know what you do.
    //      This method is the center of all edge cases demon.

    super.afterTextChanged(editable);

    if (this._isEditing) return; // Prevent infinite callback.
    this._isEditing = true;

    final String groupingSeparator =
        CurrencyFormat.groupingSeparator(this._language, this._country);
    final String decimalSeparator = CurrencyFormat.decimalSeparator(this._language, this._country);
    final String symbol =
        this._isSymbolHidden ? "" : CurrencyFormat.symbol(this._language, this._country);

    final boolean isSymbolAtStart = CurrencyFormat.isSymbolAtStart(this._language, this._country);
    final boolean isDeletingGrouping =
        this._isBackspaceClicked && this._changedTextBefore.equals(groupingSeparator);

    final String oldText = this.oldText();
    final String unformattedText =
        isDeletingGrouping
            // When deleting grouping separator, delete a number on the left side too.
            ? oldText.substring(0, Math.max(0, this._unchangedTextLeft.length() - 1))
                + this._unchangedTextRight
            : this._unchangedTextLeft + this._changedTextAfter + this._unchangedTextRight;
    // Call `EditText#getSelectionStart()` to get current cursor position before setting a new
    // text. Setting a new text will cause cursor position reset to zero.
    final int currentCursorPosition = this._view.getSelectionStart();

    BigDecimal oldAmount = BigDecimal.ZERO;
    BigDecimal newAmount = BigDecimal.ZERO;

    try {
      if (!oldText.isBlank()) {
        oldAmount = CurrencyFormat.parse(oldText, this._language, this._country);
      }

      if (!unformattedText.isBlank()) {
        newAmount = CurrencyFormat.parse(unformattedText, this._language, this._country);
      }

    } catch (ParseException e) {
      // Freeze text.
      this._view.setText(oldText);
      this._view.setSelection(this._oldCursorPosition);
      this._isEditing = false;
      return;
    }

    final String newTextFormatted =
        CurrencyFormat.format(newAmount, this._language, this._country, symbol);
    // Clear field when unformatted text doesn't have any digit on it.
    String newText =
        Strings.countOccurrenceRegex(unformattedText, "\\d") == 0 ? "" : newTextFormatted;

    // Don't use `BigDecimal#scale()` in order to get length of decimal place,
    // Cause there's a case like 1.1000 will be counted as 1
    final int oldTextDecimalPlace =
        oldText.contains(decimalSeparator)
            ? oldText
                .substring(oldText.indexOf(decimalSeparator) + 1)
                .replaceAll("[^\\d]", "")
                .length()
            : 0;
    final int unformattedTextDecimalPlace =
        unformattedText.contains(decimalSeparator)
            ? unformattedText
                .substring(unformattedText.indexOf(decimalSeparator) + 1)
                .replaceAll("[^\\d]", "")
                .length()
            : 0;

    final boolean isNewAmountBiggerThanMax = newAmount.compareTo(this._maximumAmount) > 0;
    final boolean isOldAmountIntegerZero =
        oldAmount.toBigInteger().compareTo(BigInteger.valueOf(0)) == 0;
    final boolean isDecimalPlaceLongerThanMax =
        unformattedTextDecimalPlace > CurrencyFormat.MAXIMUM_FRACTION_DIGITS;
    final boolean isNewAmountDigitPlaceZero = Strings.countOccurrenceRegex(newText, "\\d") == 0;
    final boolean isOldAmountDigitPlaceZero = Strings.countOccurrenceRegex(oldText, "\\d") == 0;

    final boolean isInputtingComma =
        !this._isBackspaceClicked && this._changedTextAfter.equals(decimalSeparator);
    final boolean isInputtingMultipleComma =
        isInputtingComma && Strings.countOccurrence(oldText, decimalSeparator) >= 1;
    final boolean isDeletingComma =
        this._isBackspaceClicked && this._changedTextBefore.equals(decimalSeparator);
    final boolean isEditingDecimalDigit =
        this._unchangedTextLeft.contains(decimalSeparator)
            && !this._changedTextBefore.contains(decimalSeparator)
            && !this._changedTextAfter.contains(decimalSeparator);
    final boolean isInputtingLeadingZero =
        this._changedTextAfter.equals("0")
            && (isOldAmountIntegerZero
                || Strings.countOccurrenceRegex(this._unchangedTextLeft, "\\d") == 0)
            && !isEditingDecimalDigit;
    final boolean isDeletingLeadingZero =
        this._changedTextBefore.equals("0")
            && (isOldAmountIntegerZero
                || Strings.countOccurrenceRegex(this._unchangedTextLeft, "\\d") == 0)
            && !isEditingDecimalDigit;
    final boolean isInputtingNegativeSign =
        !this._isBackspaceClicked && this._changedTextAfter.equals("-");
    final boolean isInputtingMultipleNegativeSign =
        isInputtingNegativeSign && Strings.countOccurrence(oldText, "-") >= 1;
    final boolean isDeletingSymbol =
        this._isBackspaceClicked && symbol.contains(this._changedTextBefore);

    final String unformattedTextBeforeCursor =
        unformattedText.substring(0, Math.min(currentCursorPosition, unformattedText.length()));
    final String newTextBeforeCursor =
        // FIXME: There's an edge case when deleting 4 on $1,234|,567,890 (the bar is cursor)
        //    will results $123,|567,890 (cursor should be placed after 3). Could be fixed
        //    with subtracting current cursor position by 1, but will cause another issue.
        //
        // Shouldn't be a big issue for now. Deleting grouping separator
        // already subtract the position itself (look at `cursorOffset` variable).
        newText.substring(0, Math.min(currentCursorPosition, newText.length()));
    final int unformattedTextDigitBeforeCursor =
        Strings.countOccurrenceRegex(unformattedTextBeforeCursor, "\\d");
    final int unformattedTextGroupingBeforeCursor =
        Strings.countOccurrence(unformattedTextBeforeCursor, groupingSeparator);
    final int newTextDigitBeforeCursor = Strings.countOccurrenceRegex(newTextBeforeCursor, "\\d");
    final int newTextGroupingBeforeCursor =
        Strings.countOccurrence(newTextBeforeCursor, groupingSeparator);

    // Offsetting happen because of the new text BEFORE current cursor position being formatted
    // with added or removed grouping separator. We just need to compare occurrence
    // of those grouping separators on both unformatted and new text.
    // Don't compare new text with old one, cause it just doesn't work.
    final int cursorOffset =
        isDeletingGrouping
            ? newTextGroupingBeforeCursor - unformattedTextGroupingBeforeCursor - 1 // Edge case.
            : newTextGroupingBeforeCursor - unformattedTextGroupingBeforeCursor;
    int cursorPosition =
        Math.max(0, Math.min(currentCursorPosition + cursorOffset, newText.length()));

    if (isDeletingComma) {
      // Deleting comma on $0.|12 will results $1|2
      // And deleting comma on $0.| will results $|0
      if (isOldAmountIntegerZero && oldTextDecimalPlace != 0) {
        cursorPosition -= 1;

        // Way too much edge cases in this two statements below, and i can't explain it any better.
        // Basically we do check how many length of digits are there BEFORE current cursor,
        // because as i said before about cursor offsetting, its happen when the new text
        // before current cursor getting formatted by added or removed grouping separator.
        //
        // So we just need to compare to see whether length of digits from unformatted text
        // are same with the one from new text. Then adjust position to what we need.

      } else if (unformattedTextDigitBeforeCursor < newTextDigitBeforeCursor
          || unformattedTextDigitBeforeCursor > newTextDigitBeforeCursor) {
        cursorPosition = currentCursorPosition + 1;

      } else if (unformattedTextDigitBeforeCursor == newTextDigitBeforeCursor) {
        cursorPosition = currentCursorPosition;
      }
    }

    // Case when inputting or deleting leading zero
    if ((isInputtingLeadingZero && isOldAmountDigitPlaceZero)
        || (isDeletingLeadingZero && isNewAmountDigitPlaceZero)) {
      newText = isDeletingLeadingZero ? newText : newTextFormatted;
      cursorPosition = currentCursorPosition;

      this._view.setText(newText);

      // Revert back to old text, aka. freeze it.
    } else if (isInputtingMultipleComma
        || isInputtingMultipleNegativeSign
        || isDeletingSymbol
        || (isInputtingLeadingZero && !isOldAmountDigitPlaceZero)
        || (isDeletingLeadingZero && !isNewAmountDigitPlaceZero)
        // Allow user to do delete operation when the text in database
        // was originally saved more than maximum limit.
        // Until the value get lesser than the limit, we will freeze it again.
        || (isNewAmountBiggerThanMax && !oldText.isEmpty() && !this._isBackspaceClicked)
        // Only freeze decimal amount when editing on the decimal digits itself
        // while hitting maximum limit. Cause there's a case like,
        // e.g. Adding comma on $1,234,567,890 between 1 and grouping separator (,)
        //    will freeze them while it shouldn't.
        || (isEditingDecimalDigit && isDecimalPlaceLongerThanMax)) {
      // Freeze cursor position.
      cursorPosition =
          this._isBackspaceClicked ? currentCursorPosition + 1 : currentCursorPosition - 1;

      this._view.setText(oldText);

      // When editing decimal digit or inputting a comma, take the new formatted text before comma
      // and append the new decimal digit in the latter. So that inputted trailing zero will not be
      // removed.
    } else if ((isEditingDecimalDigit || isInputtingComma)
        && !isInputtingMultipleComma
        && !isNewAmountBiggerThanMax
        && !isDecimalPlaceLongerThanMax) {
      final String newTextBeforeComma =
          newTextFormatted.contains(decimalSeparator)
              ? newTextFormatted.substring(0, newTextFormatted.indexOf(decimalSeparator))
              // When inputting comma as a suffix, it should be placed at the last digit of amount.
              // Cause some language uses suffix as their currency symbol.
              : newTextFormatted.substring(
                  0, Strings.lastIndexOfRegex(newTextFormatted, "\\d") + 1);
      final String newTextAfterComma =
          isDecimalPlaceLongerThanMax
              ? newTextFormatted
                  .substring(newTextFormatted.indexOf(decimalSeparator) + 1)
                  .replaceAll("\\" + groupingSeparator, "")
              // There's a case when user inputted $1,0 it will be formatted as $1
              : unformattedText
                  .substring(unformattedText.indexOf(decimalSeparator) + 1)
                  .replaceAll("\\" + groupingSeparator, "");
      newText = newTextBeforeComma + decimalSeparator + newTextAfterComma;

      // Edge case when comma inputted, everything will be reformatted.
      // Setting cursor right next to comma is the most easiest one to fix it.
      if (isInputtingComma) cursorPosition = newText.indexOf(decimalSeparator) + 1;
      else cursorPosition = currentCursorPosition;

      this._view.setText(newText);

      // Append negative sign when they're inputted, while the old text is empty.
    } else if (isInputtingNegativeSign && oldText.isBlank() && !this._isSymbolHidden) {
      newText = "-" + symbol;
      cursorPosition = isSymbolAtStart ? newText.indexOf(symbol) + 1 : newText.indexOf(symbol);

      this._view.setText(newText);

    } else {
      this._view.setText(newText);
    }

    // When adding first amount digit, offset it to the next of symbol position.
    if (oldText.isBlank() && isSymbolAtStart) cursorPosition += symbol.length();

    this._view.setSelection(Math.max(0, Math.min(cursorPosition, this._view.getText().length())));
    this._isEditing = false;
  }

  public void setMaximumAmount(@NonNull BigDecimal maxAmount) {
    this._maximumAmount = Objects.requireNonNull(maxAmount);
  }

  public void setSymbolHidden(boolean isSymbolHidden) {
    this._isSymbolHidden = isSymbolHidden;
  }
}
