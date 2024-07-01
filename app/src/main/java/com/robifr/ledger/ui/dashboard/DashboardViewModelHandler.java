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
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.data.model.QueueWithProductOrdersInfo;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardViewModel;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
    this._viewModel.date().observe(this._fragment.getViewLifecycleOwner(), this::_onDate);
    this._viewModel
        .customersWithBalance()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onCustomersWithBalance);
    this._viewModel
        .customersWithDebt()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onCustomersWithDebt);
    this._viewModel
        .queuesWithProductOrders()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onQueuesWithProductOrders);

    this._viewModel
        .revenueView()
        .displayedChart()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onRevenueDisplayedChart);
    this._viewModel
        .revenueView()
        .chartModel()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onRevenueChartModel);
  }

  private void _onSnackbarMessage(@Nullable StringResources stringRes) {
    if (stringRes == null) return;

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onDate(@Nullable QueueDate date) {
    if (date == null) return;

    final DateTimeFormatter format =
        DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("id", "ID"));
    final String text =
        date.range() == QueueDate.Range.CUSTOM
            ? this._fragment.getString(
                date.range().resourceString(),
                date.dateStart().format(format),
                date.dateEnd().format(format))
            : this._fragment.getString(date.range().resourceString());

    this._fragment.fragmentBinding().dateChip.setText(text);
  }

  private void _onCustomersWithBalance(@Nullable List<CustomerBalanceInfo> balanceInfo) {
    this._fragment
        .balanceOverview()
        .setTotalBalance(Objects.requireNonNullElse(balanceInfo, List.of()));
  }

  private void _onCustomersWithDebt(@Nullable List<CustomerDebtInfo> debtInfo) {
    this._fragment.balanceOverview().setTotalDebt(Objects.requireNonNullElse(debtInfo, List.of()));
  }

  private void _onQueuesWithProductOrders(@Nullable List<QueueWithProductOrdersInfo> queueInfo) {
    if (queueInfo == null) return;

    this._fragment.revenueOverview().setTotalReceivedIncome(queueInfo);
    this._fragment.revenueOverview().setTotalProjectedIncome(queueInfo);
    this._fragment.revenueOverview().setTotalQueue(queueInfo);
    this._fragment.revenueOverview().setTotalProductsSold(queueInfo);
    this._fragment.revenueOverview().loadChart();
  }

  private void _onRevenueDisplayedChart(@Nullable DashboardRevenue.OverviewType overviewType) {
    if (overviewType == null) return;

    this._fragment.revenueOverview().selectCard(overviewType);
    this._fragment.revenueOverview().loadChart();
  }

  private void _onRevenueChartModel(@Nullable DashboardRevenue.ChartModel model) {
    if (model != null) this._fragment.revenueOverview().displayChart(model);
  }
}
