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
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.R;
import com.robifr.ledger.assetbinding.JsInterface;
import com.robifr.ledger.assetbinding.chart.ChartData;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardRevenueViewModel;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardSummaryViewModel;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardViewModel;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DashboardViewModelHandler {
  @NonNull private final DashboardFragment _fragment;
  @NonNull private final DashboardViewModel _viewModel;

  public DashboardViewModelHandler(
      @NonNull DashboardFragment fragment, @NonNull DashboardViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel.date().observe(this._fragment.getViewLifecycleOwner(), this::_onDate);

    this._viewModel
        .summaryView()
        .displayedChart()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSummaryDisplayedChart);
    this._viewModel
        .summaryView()
        .totalQueuesChartModel()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSummaryTotalQueuesChartModel);
    this._viewModel
        .summaryView()
        .totalQueues()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSummaryTotalQueues);
    this._viewModel
        .summaryView()
        .uncompletedQueuesChartModel()
        .observe(
            this._fragment.getViewLifecycleOwner(), this::_onSummaryUncompletedQueuesChartModel);
    this._viewModel
        .summaryView()
        .totalUncompletedQueues()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSummaryTotalUncompletedQueues);
    this._viewModel
        .summaryView()
        .mostActiveCustomers()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSummaryMostActiveCustomers);
    this._viewModel
        .summaryView()
        .totalActiveCustomers()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSummaryTotalActiveCustomers);
    this._viewModel
        .summaryView()
        .totalProductsSold()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSummaryTotalProductsSold);

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

  private void _onSnackbarMessage(@NonNull StringResources stringRes) {
    Objects.requireNonNull(stringRes);

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onDate(@NonNull QueueDate date) {
    Objects.requireNonNull(date);

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

  private void _onSummaryDisplayedChart(@NonNull DashboardSummary.OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    this._fragment.summaryOverview().selectCard(overviewType);
    this._fragment.summaryOverview().chart().load();
  }

  private void _onSummaryTotalQueuesChartModel(
      @NonNull DashboardSummaryViewModel.TotalQueuesChartModel model) {
    Objects.requireNonNull(model);

    this._fragment.fragmentBinding().summary.listContainer.setVisibility(View.GONE);
    this._fragment.fragmentBinding().summary.chart.setVisibility(View.VISIBLE);
    this._fragment
        .summaryOverview()
        .chart()
        .displayBarChart(
            model.xAxisDomain(),
            model.yAxisDomain(),
            model.data(),
            MaterialColors.getColor(
                this._fragment.requireContext(),
                com.google.android.material.R.attr.colorPrimary,
                0));
  }

  private void _onSummaryTotalQueues(int amount) {
    this._fragment.summaryOverview().setTotalQueues(amount);
    this._fragment.summaryOverview().chart().load();
  }

  private void _onSummaryUncompletedQueuesChartModel(
      @NonNull DashboardSummaryViewModel.UncompletedQueuesChartModel model) {
    Objects.requireNonNull(model);

    final int titleFontSize =
        JsInterface.dpToCssPx(
            this._fragment.requireContext(),
            this._fragment.getResources().getDimensionPixelSize(R.dimen.text_medium));
    final int oldestDateFontSize =
        JsInterface.dpToCssPx(
            this._fragment.requireContext(),
            this._fragment.getResources().getDimensionPixelSize(R.dimen.text_mediumlarge));
    final String oldestDate =
        model.oldestDate() != null
            ? model
                .oldestDate()
                .format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                        .withLocale(new Locale("id", "ID")))
            : null;
    final String textInCenter =
        model.oldestDate() != null
            ? String.format(
                this._fragment.getString(R.string.args_svg_oldest_queue_x),
                titleFontSize,
                oldestDateFontSize,
                oldestDate)
            : null;

    this._fragment.fragmentBinding().summary.listContainer.setVisibility(View.GONE);
    this._fragment.fragmentBinding().summary.chart.setVisibility(View.VISIBLE);
    this._fragment
        .summaryOverview()
        .chart()
        .displayDonutChart(
            model.data().stream()
                .map(
                    data ->
                        new ChartData.Single<>(this._fragment.getString(data.key()), data.value()))
                .collect(Collectors.toList()),
            model.colors().stream()
                .map(this._fragment.requireContext()::getColor)
                .collect(Collectors.toList()),
            textInCenter);
  }

  private void _onSummaryTotalUncompletedQueues(int amount) {
    this._fragment.summaryOverview().setTotalUncompletedQueues(amount);
  }

  private void _onSummaryMostActiveCustomers(@NonNull Map<CustomerModel, Long> customers) {
    Objects.requireNonNull(customers);

    this._fragment.summaryOverview().displayMostActiveCustomersList(customers);
  }

  private void _onSummaryTotalActiveCustomers(int amount) {
    this._fragment.summaryOverview().setTotalActiveCustomers(amount);
  }

  private void _onSummaryTotalProductsSold(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment.summaryOverview().setTotalProductsSold(amount);
  }

  private void _onBalanceTotalBalance(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment.balanceOverview().setTotalBalance(amount);
  }

  private void _onBalanceCustomersWithBalance(int amount) {
    this._fragment.balanceOverview().setTotalCustomersWithBalance(amount);
  }

  private void _onBalanceTotalDebt(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment.balanceOverview().setTotalDebt(amount);
  }

  private void _onBalanceCustomersWithDebt(int amount) {
    this._fragment.balanceOverview().setTotalCustomersWithDebt(amount);
  }

  private void _onRevenueDisplayedChart(@NonNull DashboardRevenue.OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    this._fragment.revenueOverview().selectCard(overviewType);
    this._fragment.revenueOverview().chart().load();
  }

  private void _onRevenueChartModel(@NonNull DashboardRevenueViewModel.IncomeChartModel model) {
    Objects.requireNonNull(model);

    this._fragment
        .revenueOverview()
        .chart()
        .displayStackedBarChartWithLargeValue(
            model.xAxisDomain(),
            model.yAxisDomain(),
            model.data(),
            model.colors().stream()
                .map(this._fragment.requireContext()::getColor)
                .collect(Collectors.toList()),
            model.groupInOrder().stream().map(Enum::toString).collect(Collectors.toSet()));
  }

  private void _onRevenueReceivedIncome(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment.revenueOverview().setTotalReceivedIncome(amount);
    this._fragment.revenueOverview().chart().load();
  }

  private void _onRevenueProjectedIncome(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment.revenueOverview().setTotalProjectedIncome(amount);
    this._fragment.revenueOverview().chart().load();
  }
}
