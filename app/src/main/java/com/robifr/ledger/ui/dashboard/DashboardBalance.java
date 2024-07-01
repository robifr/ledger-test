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

package com.robifr.ledger.ui.dashboard;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import com.robifr.ledger.R;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class DashboardBalance {
  @NonNull private final DashboardFragment _fragment;

  public DashboardBalance(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  public void setTotalBalance(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .balance
        .totalBalance
        .setText(CurrencyFormat.format(amount, "id", "ID"));
  }

  public void setTotalCustomersWithBalance(int amount) {
    final String totalText =
        this._fragment
            .getResources()
            .getQuantityString(R.plurals.args_from_x_customers, amount, amount);

    this._fragment
        .fragmentBinding()
        .balance
        .totalCustomersWithBalanceTitle
        .setText(HtmlCompat.fromHtml(totalText, HtmlCompat.FROM_HTML_MODE_LEGACY));
  }

  public void setTotalDebt(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    final int amountTextColor =
        amount.compareTo(BigDecimal.ZERO) < 0
            // Negative debt will be shown red.
            ? this._fragment.requireContext().getColor(R.color.red)
            : this._fragment.requireContext().getColor(R.color.text_enabled);

    this._fragment
        .fragmentBinding()
        .balance
        .totalDebt
        .setText(CurrencyFormat.format(amount, "id", "ID"));
    this._fragment.fragmentBinding().balance.totalDebt.setTextColor(amountTextColor);
  }

  public void setTotalCustomersWithDebt(int amount) {
    final String totalText =
        this._fragment
            .getResources()
            .getQuantityString(R.plurals.args_from_x_customers, amount, amount);

    this._fragment
        .fragmentBinding()
        .balance
        .totalCustomersWithDebtTitle
        .setText(HtmlCompat.fromHtml(totalText, HtmlCompat.FROM_HTML_MODE_LEGACY));
  }
}
