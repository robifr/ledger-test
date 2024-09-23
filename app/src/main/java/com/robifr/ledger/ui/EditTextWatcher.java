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

package com.robifr.ledger.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.NonNull;
import java.util.Objects;

/**
 * Let's say the full string is {@code "$123"}. User made a change from {@code "2"} to {@code "6"}.
 * <br>
 * {@link EditTextWatcher#_changedTextBefore} is {@code "2"} <br>
 * {@link EditTextWatcher#_changedTextAfter} is {@code "6"} <br>
 * {@link EditTextWatcher#_unchangedTextLeft} is {@code "$1"} <br>
 * {@link EditTextWatcher#_unchangedTextRight} is {@code "3"} <br>
 * <br>
 * Now combine into a new text, <br>
 * {@link EditTextWatcher#_unchangedTextLeft} <br>
 * + {@link EditTextWatcher#_changedTextAfter} <br>
 * + {@link EditTextWatcher#_unchangedTextRight}. <br>
 * Which becomes {@code "$163"}.
 */
public class EditTextWatcher implements TextWatcher {
  @NonNull protected final EditText _view;
  protected boolean _isEditing = false;
  protected boolean _isBackspaceClicked = false;
  protected int _oldCursorPosition = 0;

  /**
   * Part of the text before changes applied.
   *
   * @see EditTextWatcher
   */
  @NonNull protected String _changedTextBefore = "";

  /**
   * Part of the text after changes applied.
   *
   * @see EditTextWatcher
   */
  @NonNull protected String _changedTextAfter = "";

  /**
   * Unchanged text which is placed before changed part.
   *
   * @see EditTextWatcher
   */
  @NonNull protected String _unchangedTextLeft = "";

  /**
   * Unchanged text which is placed after changed part.
   *
   * @see EditTextWatcher
   */
  @NonNull protected String _unchangedTextRight = "";

  public EditTextWatcher(@NonNull EditText view) {
    this._view = Objects.requireNonNull(view);
  }

  @Override
  public void beforeTextChanged(@NonNull CharSequence seq, int start, int count, int after) {
    Objects.requireNonNull(seq);

    this._unchangedTextLeft = seq.subSequence(0, start).toString();
    this._changedTextBefore = seq.subSequence(start, start + count).toString();
    this._unchangedTextRight = seq.subSequence(start + count, seq.length()).toString();
    this._isBackspaceClicked = after < count;
    this._oldCursorPosition = this._view.getSelectionStart();
  }

  @Override
  public void onTextChanged(@NonNull CharSequence seq, int start, int before, int count) {
    Objects.requireNonNull(seq);

    this._changedTextAfter = seq.subSequence(start, start + count).toString();
  }

  @Override
  public void afterTextChanged(@NonNull Editable editable) {
    Objects.requireNonNull(editable);
  }

  @NonNull
  public String oldText() {
    return this._unchangedTextLeft + this._changedTextBefore + this._unchangedTextRight;
  }

  @NonNull
  public String newText() {
    return this._unchangedTextLeft + this._changedTextAfter + this._unchangedTextRight;
  }
}
