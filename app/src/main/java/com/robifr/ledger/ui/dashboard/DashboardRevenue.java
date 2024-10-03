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

package com.robifr.ledger.ui.dashboard;

import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.DashboardCardRevenueBinding;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardRevenueViewModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Collectors;

public class DashboardRevenue implements View.OnClickListener {
  public enum OverviewType {
    PROJECTED_INCOME(R.color.secondary, R.color.secondary_disabled),
    RECEIVED_INCOME(R.color.primary, R.color.secondary);

    @ColorRes private final int _selectedResourceColor;
    @ColorRes private final int _unselectedResourceColor;

    private OverviewType(
        @ColorRes int selectedResourceColor, @ColorRes int unselectedResourceColor) {
      this._selectedResourceColor = selectedResourceColor;
      this._unselectedResourceColor = unselectedResourceColor;
    }

    @ColorRes
    public int selectedResourceColor() {
      return this._selectedResourceColor;
    }

    @ColorRes
    public int unselectedResourceColor() {
      return this._unselectedResourceColor;
    }
  }

  @NonNull private final DashboardFragment _fragment;
  @NonNull private final Chart _chart;

  public DashboardRevenue(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._chart =
        new Chart(
            this._fragment.requireContext(),
            this._fragment.fragmentBinding().revenue.chart,
            new LocalWebView());

    final DashboardCardRevenueBinding cardBinding = this._fragment.fragmentBinding().revenue;
    cardBinding.projectedIncomeCardView.setOnClickListener(this);
    cardBinding.projectedIncomeCard.icon.setImageResource(R.drawable.icon_trending_up);
    cardBinding.projectedIncomeCard.legendColor.setCardBackgroundColor(
        this._fragment
            .requireContext()
            .getColor(OverviewType.PROJECTED_INCOME.selectedResourceColor()));
    cardBinding.projectedIncomeCard.title.setText(R.string.dashboard_projectedIncome);
    cardBinding.projectedIncomeCard.description.setText(
        R.string.dashboard_projectedIncome_description);
    cardBinding.receivedIncomeCardView.setOnClickListener(this);
    cardBinding.receivedIncomeCard.icon.setImageResource(R.drawable.icon_paid);
    cardBinding.receivedIncomeCard.legendColor.setCardBackgroundColor(
        this._fragment
            .requireContext()
            .getColor(OverviewType.RECEIVED_INCOME.selectedResourceColor()));
    cardBinding.receivedIncomeCard.title.setText(R.string.dashboard_receivedIncome);
    cardBinding.receivedIncomeCard.description.setText(
        R.string.dashboard_receivedIncome_description);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.projectedIncomeCardView, R.id.receivedIncomeCardView ->
          this._fragment
              .dashboardViewModel()
              .revenueView()
              .onDisplayedChartChanged(OverviewType.valueOf(view.getTag().toString()));
    }
  }

  public void loadChart() {
    this._chart.load();
  }

  public void selectCard(@NonNull OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    final DashboardCardRevenueBinding cardBinding = this._fragment.fragmentBinding().revenue;
    // There should be only one card getting selected.
    cardBinding.projectedIncomeCardView.setSelected(false);
    cardBinding.receivedIncomeCardView.setSelected(false);

    switch (overviewType) {
      case PROJECTED_INCOME -> cardBinding.projectedIncomeCardView.setSelected(true);
      case RECEIVED_INCOME -> cardBinding.receivedIncomeCardView.setSelected(true);
    }
  }

  public void setTotalProjectedIncome(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .revenue
        .projectedIncomeCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID"));
  }

  public void setTotalReceivedIncome(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .revenue
        .receivedIncomeCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID"));
  }

  public void displayRevenueChart(@NonNull DashboardRevenueViewModel.IncomeChartModel model) {
    Objects.requireNonNull(model);

    this._chart.displayStackedBarChartWithLargeValue(
        model.xAxisDomain(),
        model.yAxisDomain(),
        model.data(),
        model.colors().stream()
            .map(this._fragment.requireContext()::getColor)
            .collect(Collectors.toList()),
        model.groupInOrder().stream().map(Enum::toString).collect(Collectors.toSet()));
  }

  private class LocalWebView extends WebViewClientCompat {
    @NonNull private final WebViewAssetLoader _assetLoader;

    public LocalWebView() {
      this._assetLoader =
          new WebViewAssetLoader.Builder()
              .addPathHandler(
                  "/assets/",
                  new WebViewAssetLoader.AssetsPathHandler(
                      DashboardRevenue.this._fragment.requireContext()))
              .build();
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(
        @NonNull WebView view, @NonNull WebResourceRequest request) {
      Objects.requireNonNull(view);
      Objects.requireNonNull(request);

      return this._assetLoader.shouldInterceptRequest(request.getUrl());
    }

    @Override
    public void onPageFinished(@NonNull WebView view, @NonNull String url) {
      Objects.requireNonNull(view);
      Objects.requireNonNull(url);

      final DashboardRevenueViewModel revenueViewModel =
          DashboardRevenue.this._fragment.dashboardViewModel().revenueView();

      switch (revenueViewModel.displayedChart().getValue()) {
        case PROJECTED_INCOME -> revenueViewModel.onDisplayProjectedIncomeChart();
        case RECEIVED_INCOME -> revenueViewModel.onDisplayReceivedIncomeChart();
      }
    }
  }
}
