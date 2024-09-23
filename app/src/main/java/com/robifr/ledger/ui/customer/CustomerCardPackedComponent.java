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

package com.robifr.ledger.ui.customer;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardPackedBinding;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class CustomerCardPackedComponent {
  @NonNull private final Context _context;
  @NonNull private final CustomerCardPackedBinding _binding;

  public CustomerCardPackedComponent(
      @NonNull Context context, @NonNull CustomerCardPackedBinding binding) {
    this._context = Objects.requireNonNull(context);
    this._binding = Objects.requireNonNull(binding);

    this._binding.image.shapeableImage.setShapeAppearanceModel(
        ShapeAppearanceModel.builder(
                this._context,
                com.google.android.material.R.style.Widget_MaterialComponents_ShapeableImageView,
                R.style.Shape_Round)
            .build());
  }

  public void setCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._setName(customer.name());
    this._setBalance(customer.balance());
  }

  private void _setName(@NonNull String name) {
    Objects.requireNonNull(name);

    this._binding.name.setText(name);
    this._binding.image.text.setText(name.trim().substring(0, Math.min(1, name.trim().length())));
  }

  private void _setBalance(long balance) {
    this._binding.balance.setText(CurrencyFormat.format(BigDecimal.valueOf(balance), "id", "ID"));
  }
}
