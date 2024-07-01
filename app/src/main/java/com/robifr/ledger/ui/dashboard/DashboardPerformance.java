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
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class DashboardPerformance {
  @NonNull private final DashboardFragment _fragment;

  public DashboardPerformance(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  public void setTotalQueue(int amount) {
    this._fragment.fragmentBinding().performance.totalQueue.setText(Integer.toString(amount));
  }

  public void setTotalActiveCustomers(int amount) {
    this._fragment.fragmentBinding().performance.activeCustomers.setText(Integer.toString(amount));
  }

  public void setTotalProductsSold(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .performance
        .productsSold
        .setText(CurrencyFormat.format(amount, "id", "ID", "")); // Format the decimal point.
  }
}
