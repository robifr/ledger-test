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

package com.robifr.ledger.ui.create_product;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.ui.EditTextWatcher;
import java.util.Objects;

public class CreateProductName {
  @NonNull private final CreateProductFragment _fragment;
  @NonNull private final NameTextWatcher _nameTextWatcher;

  public CreateProductName(@NonNull CreateProductFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._nameTextWatcher = new NameTextWatcher(this._fragment.fragmentBinding().name);

    this._fragment.fragmentBinding().name.addTextChangedListener(this._nameTextWatcher);
  }

  public void setInputtedNameText(@Nullable String name) {
    final String currentText = this._fragment.fragmentBinding().name.getText().toString();
    if (currentText.equals(name)) return;

    final int cursorPosition = name != null ? name.length() : 0;

    // Remove listener to prevent any sort of formatting although there isn't.
    this._fragment.fragmentBinding().name.removeTextChangedListener(this._nameTextWatcher);
    this._fragment.fragmentBinding().name.setText(name);
    this._fragment.fragmentBinding().name.setSelection(cursorPosition);
    this._fragment.fragmentBinding().name.addTextChangedListener(this._nameTextWatcher);
  }

  public void setError(@NonNull String message, boolean isEnabled) {
    Objects.requireNonNull(message);

    this._fragment.fragmentBinding().nameLayout.setError(message);
    this._fragment.fragmentBinding().nameLayout.setErrorEnabled(isEnabled);
  }

  private class NameTextWatcher extends EditTextWatcher {
    public NameTextWatcher(@NonNull EditText view) {
      super(view);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);
      CreateProductName.this._fragment.createProductViewModel().onNameTextChanged(this.newText());
    }
  }
}
