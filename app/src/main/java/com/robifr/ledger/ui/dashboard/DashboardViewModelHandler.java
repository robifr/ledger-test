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
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardViewModel;
import java.math.BigDecimal;
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
        .balanceView()
        .totalBalance()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onBalanceTotalBalance);
    this._viewModel
        .balanceView()
        .totalCustomersWithBalance()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onBalanceCustomersWithBalance);
    this._viewModel
        .balanceView()
        .totalDebt()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onBalanceTotalDebt);
    this._viewModel
        .balanceView()
        .totalCustomersWithDebt()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onBalanceCustomersWithDebt);

    this._viewModel
        .performanceView()
        .totalQueue()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onTotalQueue);
    this._viewModel
        .performanceView()
        .totalQueueAverage()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onTotalQueueAverage);
    this._viewModel
        .performanceView()
        .totalActiveCustomers()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onTotalActiveCustomers);
    this._viewModel
        .performanceView()
        .totalActiveCustomersAverage()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onTotalActiveCustomersAverage);
    this._viewModel
        .performanceView()
        .totalProductsSold()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onTotalProductsSold);
    this._viewModel
        .performanceView()
        .totalProductsSoldAverage()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onTotalProductsSoldAverage);

    this._viewModel
        .revenueView()
        .displayedChart()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onRevenueDisplayedChart);
    this._viewModel
        .revenueView()
        .chartModel()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onRevenueChartModel);
    this._viewModel
        .revenueView()
        .receivedIncome()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onRevenueReceivedIncome);
    this._viewModel
        .revenueView()
        .projectedIncome()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onRevenueProjectedIncome);
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

  private void _onQueues(@Nullable List<QueueModel> queues) {
    if (queues != null) this._fragment.revenueOverview().loadChart();
  }

  private void _onBalanceTotalBalance(@Nullable BigDecimal amount) {
    if (amount != null) this._fragment.balanceOverview().setTotalBalance(amount);
  }

  private void _onBalanceCustomersWithBalance(@Nullable Integer amount) {
    if (amount != null) this._fragment.balanceOverview().setTotalCustomersWithBalance(amount);
  }

  private void _onBalanceTotalDebt(@Nullable BigDecimal amount) {
    if (amount != null) this._fragment.balanceOverview().setTotalDebt(amount);
  }

  private void _onBalanceCustomersWithDebt(@Nullable Integer amount) {
    if (amount != null) this._fragment.balanceOverview().setTotalCustomersWithDebt(amount);
  }

  private void _onTotalQueue(@Nullable Integer amount) {
    if (amount != null) this._fragment.performanceOverview().setTotalQueue(amount);
  }

  private void _onTotalQueueAverage(@Nullable BigDecimal amount) {
    if (amount != null) this._fragment.performanceOverview().setTotalQueueAverage(amount);
  }

  private void _onTotalActiveCustomers(@Nullable Integer amount) {
    if (amount != null) this._fragment.performanceOverview().setTotalActiveCustomers(amount);
  }

  private void _onTotalActiveCustomersAverage(@Nullable BigDecimal amount) {
    if (amount != null) this._fragment.performanceOverview().setTotalActiveCustomersAverage(amount);
  }

  private void _onTotalProductsSold(@Nullable BigDecimal amount) {
    if (amount != null) this._fragment.performanceOverview().setTotalProductsSold(amount);
  }

  private void _onTotalProductsSoldAverage(@Nullable BigDecimal amount) {
    if (amount != null) this._fragment.performanceOverview().setTotalProductsSoldAverage(amount);
  }

  private void _onRevenueDisplayedChart(@Nullable DashboardRevenue.OverviewType overviewType) {
    if (overviewType == null) return;

    this._fragment.revenueOverview().selectCard(overviewType);
    this._fragment.revenueOverview().loadChart();
  }

  private void _onRevenueChartModel(@Nullable DashboardRevenue.ChartModel model) {
    if (model != null) this._fragment.revenueOverview().displayChart(model);
  }

  private void _onRevenueReceivedIncome(@Nullable BigDecimal amount) {
    if (amount == null) return;

    this._fragment.revenueOverview().setTotalReceivedIncome(amount);
    this._fragment.revenueOverview().loadChart();
  }

  private void _onRevenueProjectedIncome(@Nullable BigDecimal amount) {
    if (amount == null) return;

    this._fragment.revenueOverview().setTotalProjectedIncome(amount);
    this._fragment.revenueOverview().loadChart();
  }
}
