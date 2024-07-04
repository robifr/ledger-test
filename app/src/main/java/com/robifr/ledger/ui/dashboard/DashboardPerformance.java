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
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.DashboardCardPerformanceBinding;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class DashboardPerformance {
  @NonNull private final DashboardFragment _fragment;

  public DashboardPerformance(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    final DashboardCardPerformanceBinding cardBinding =
        this._fragment.fragmentBinding().performance;
    cardBinding.totalQueue.icon.setImageResource(R.drawable.icon_assignment);
    cardBinding.totalQueue.title.setText(R.string.text_total_queue);
    cardBinding.activeCustomers.icon.setImageResource(R.drawable.icon_person);
    cardBinding.activeCustomers.title.setText(R.string.text_active_customers);
    cardBinding.productsSold.icon.setImageResource(R.drawable.icon_sell);
    cardBinding.productsSold.title.setText(R.string.text_products_sold);
  }

  public void setTotalQueue(int amount) {
    this._fragment
        .fragmentBinding()
        .performance
        .totalQueue
        .totalAmount
        .setText(Integer.toString(amount));
  }

  public void setTotalQueueAverage(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .performance
        .totalQueue
        .totalAverage
        .setText(CurrencyFormat.format(amount, "id", "ID", ""));
  }

  public void setTotalActiveCustomers(int amount) {
    this._fragment
        .fragmentBinding()
        .performance
        .activeCustomers
        .totalAmount
        .setText(Integer.toString(amount));
  }

  public void setTotalActiveCustomersAverage(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .performance
        .activeCustomers
        .totalAverage
        .setText(CurrencyFormat.format(amount, "id", "ID", ""));
  }

  public void setTotalProductsSold(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .performance
        .productsSold
        .totalAmount
        .setText(CurrencyFormat.format(amount, "id", "ID", ""));
  }

  public void setTotalProductsSoldAverage(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .performance
        .productsSold
        .totalAverage
        .setText(CurrencyFormat.format(amount, "id", "ID", ""));
  }
}
