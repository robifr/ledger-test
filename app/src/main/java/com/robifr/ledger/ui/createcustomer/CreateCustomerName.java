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

package com.robifr.ledger.ui.createcustomer;

import android.text.Editable;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.ui.EditTextWatcher;
import java.util.Objects;

public class CreateCustomerName {
  @NonNull private final CreateCustomerFragment _fragment;
  @NonNull private final NameTextWatcher _nameTextWatcher;

  public CreateCustomerName(@NonNull CreateCustomerFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._nameTextWatcher = new NameTextWatcher(this._fragment.fragmentBinding().name);

    this._fragment.fragmentBinding().name.addTextChangedListener(this._nameTextWatcher);
  }

  public void setInputtedNameText(@NonNull String name) {
    Objects.requireNonNull(name);

    final String currentText = this._fragment.fragmentBinding().name.getText().toString();
    if (currentText.equals(name)) return;

    // Remove listener to prevent any sort of formatting although there isn't.
    this._fragment.fragmentBinding().name.removeTextChangedListener(this._nameTextWatcher);
    this._fragment.fragmentBinding().name.setText(name);
    this._fragment.fragmentBinding().name.setSelection(name.length());
    this._fragment.fragmentBinding().name.addTextChangedListener(this._nameTextWatcher);
  }

  public void setError(@Nullable String message) {
    this._fragment.fragmentBinding().nameLayout.setError(message);
  }

  private class NameTextWatcher extends EditTextWatcher {
    public NameTextWatcher(@NonNull EditText view) {
      super(view);
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
      super.afterTextChanged(editable);
      CreateCustomerName.this._fragment.createCustomerViewModel().onNameTextChanged(this.newText());
    }
  }
}
