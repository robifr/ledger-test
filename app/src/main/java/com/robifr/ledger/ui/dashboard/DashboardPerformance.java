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
import com.robifr.ledger.data.model.QueueWithProductOrdersInfo;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DashboardPerformance {
  @NonNull private final DashboardFragment _fragment;

  public DashboardPerformance(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  public void setTotalQueue(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    this._fragment
        .fragmentBinding()
        .performance
        .totalQueue
        .setText(Integer.toString(queueInfo.size()));
  }

  public void setTotalProductsSold(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    final BigDecimal amount =
        queueInfo.stream()
            .flatMap(queue -> queue.productOrders().stream())
            .map(productOrder -> BigDecimal.valueOf(productOrder.quantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    this._fragment
        .fragmentBinding()
        .performance
        .productsSold
        .setText(CurrencyFormat.format(amount, "id", "ID", ""));// Format the decimal point.
  }
}
