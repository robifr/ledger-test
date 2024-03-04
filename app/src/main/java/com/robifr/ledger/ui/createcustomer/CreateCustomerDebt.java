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

package com.robifr.ledger.ui.createcustomer;

import androidx.annotation.NonNull;
import com.robifr.ledger.R;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class CreateCustomerDebt {
  @NonNull private final CreateCustomerFragment _fragment;

  public CreateCustomerDebt(@NonNull CreateCustomerFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  public void setInputtedDebt(@NonNull BigDecimal debt) {
    Objects.requireNonNull(debt);

    final int textColor =
        debt.compareTo(BigDecimal.ZERO) < 0
            // Red for negative debt.
            ? this._fragment.requireContext().getColor(R.color.red)
            : this._fragment.requireContext().getColor(R.color.text_disabled);

    this._fragment.fragmentBinding().debt.setText(CurrencyFormat.format(debt, "id", "ID"));
    this._fragment.fragmentBinding().debt.setTextColor(textColor);
  }
}
