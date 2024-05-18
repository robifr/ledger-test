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

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardViewModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DashboardViewModelHandler {
  @NonNull private final DashboardFragment _fragment;
  @NonNull private final DashboardViewModel _viewModel;

  public DashboardViewModelHandler(
      @NonNull DashboardFragment fragment, @NonNull DashboardViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .snackbarMessage()
        .observe(this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onSnackbarMessage));

    this._viewModel
        .balanceView()
        .customersWithBalance()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onCustomersWithBalance);
    this._viewModel
        .balanceView()
        .customersWithDebt()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onCustomersWithDebt);
  }

  private void _onSnackbarMessage(@Nullable StringResources stringRes) {
    if (stringRes == null) return;

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onCustomersWithBalance(@Nullable List<CustomerBalanceInfo> balanceInfo) {
    final int total = balanceInfo != null ? balanceInfo.size() : 0;
    final String totalText =
        this._fragment
            .getResources()
            .getQuantityString(R.plurals.args_from_x_customers, total, total);
    final long amount =
        balanceInfo != null
            ? balanceInfo.stream().mapToLong(CustomerBalanceInfo::balance).sum()
            : 0L;

    this._fragment
        .fragmentBinding()
        .balance
        .totalCustomersWithBalanceTitle
        .setText(HtmlCompat.fromHtml(totalText, HtmlCompat.FROM_HTML_MODE_LEGACY));
    this._fragment
        .fragmentBinding()
        .balance
        .totalBalance
        .setText(CurrencyFormat.format(BigDecimal.valueOf(amount), "id", "ID"));
  }

  private void _onCustomersWithDebt(@Nullable List<CustomerDebtInfo> debtInfo) {
    final int total = debtInfo != null ? debtInfo.size() : 0;
    final String totalText =
        this._fragment
            .getResources()
            .getQuantityString(R.plurals.args_from_x_customers, total, total);

    final BigDecimal amount =
        debtInfo != null
            ? debtInfo.stream().map(CustomerDebtInfo::debt).reduce(BigDecimal.ZERO, BigDecimal::add)
            : BigDecimal.ZERO;
    final int amountTextColor =
        amount.compareTo(BigDecimal.ZERO) < 0
            // Negative debt will be shown red.
            ? this._fragment.requireContext().getColor(R.color.red)
            : this._fragment.requireContext().getColor(R.color.text_enabled);

    this._fragment
        .fragmentBinding()
        .balance
        .totalCustomersWithDebtTitle
        .setText(HtmlCompat.fromHtml(totalText, HtmlCompat.FROM_HTML_MODE_LEGACY));
    this._fragment
        .fragmentBinding()
        .balance
        .totalDebt
        .setText(CurrencyFormat.format(amount, "id", "ID"));
    this._fragment.fragmentBinding().balance.totalDebt.setTextColor(amountTextColor);
  }
}
