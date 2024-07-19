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
    cardBinding.projectedIncomeCard.title.setText(R.string.text_projected_income);
    cardBinding.projectedIncomeCard.description.setText(R.string.text_from_any_queues);
    cardBinding.receivedIncomeCardView.setOnClickListener(this);
    cardBinding.receivedIncomeCard.icon.setImageResource(R.drawable.icon_paid);
    cardBinding.receivedIncomeCard.legendColor.setCardBackgroundColor(
        this._fragment
            .requireContext()
            .getColor(OverviewType.RECEIVED_INCOME.selectedResourceColor()));
    cardBinding.receivedIncomeCard.title.setText(R.string.text_received_income);
    cardBinding.receivedIncomeCard.description.setText(R.string.text_from_completed_queues);
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

  @NonNull
  public Chart chart() {
    return this._chart;
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
