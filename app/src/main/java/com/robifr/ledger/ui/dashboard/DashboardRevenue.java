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
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import com.google.android.material.color.MaterialColors;
import com.robifr.ledger.R;
import com.robifr.ledger.assetbinding.JsInterface;
import com.robifr.ledger.assetbinding.chart.BarChartBinding;
import com.robifr.ledger.assetbinding.chart.ChartAxisBinding;
import com.robifr.ledger.assetbinding.chart.ChartLayoutBinding;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.DashboardCardRevenueBinding;
import com.robifr.ledger.ui.LocalWebChrome;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardRevenue implements View.OnClickListener {
  public enum OverviewType {
    PROJECTED_INCOME,
    RECEIVED_INCOME,
  }

  @NonNull private final DashboardFragment _fragment;

  public DashboardRevenue(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    final DashboardCardRevenueBinding cardBinding = this._fragment.fragmentBinding().revenue;
    cardBinding.chart.getSettings().setSupportZoom(false);
    cardBinding.chart.getSettings().setBuiltInZoomControls(false);
    cardBinding.chart.getSettings().setAllowFileAccess(false);
    cardBinding.chart.getSettings().setJavaScriptEnabled(true);
    cardBinding.chart.addJavascriptInterface(
        new JsInterface(this._fragment.requireContext()), JsInterface.NAME);
    cardBinding.chart.setWebViewClient(new LocalWebView());
    cardBinding.chart.setWebChromeClient(new LocalWebChrome());
    cardBinding.chart.setBackgroundColor( // Background color can't be set from xml.
        MaterialColors.getColor(
            this._fragment.requireContext(), com.google.android.material.R.attr.colorSurface, 0));
    cardBinding.projectedIncomeCardView.setOnClickListener(this);
    cardBinding.projectedIncomeCard.icon.setImageResource(R.drawable.icon_trending_up);
    cardBinding.projectedIncomeCard.title.setText(R.string.text_projected_income);
    cardBinding.projectedIncomeCard.description.setText(R.string.text_from_any_queues);
    cardBinding.receivedIncomeCardView.setOnClickListener(this);
    cardBinding.receivedIncomeCard.icon.setImageResource(R.drawable.icon_paid);
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

  public void loadChart() {
    this._fragment
        .fragmentBinding()
        .revenue
        .chart
        .loadUrl("https://appassets.androidplatform.net/assets/chart.html");
  }

  public void setTotalProjectedIncome(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final BigDecimal amount =
        queues.stream()
            .flatMap(queue -> queue.productOrders().stream())
            .map(ProductOrderModel::totalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    this._fragment
        .fragmentBinding()
        .revenue
        .projectedIncomeCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID"));
  }

  public void setTotalReceivedIncome(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final BigDecimal amount =
        queues.stream()
            // Received income are from the completed queues.
            .filter(queue -> queue.status() == QueueModel.Status.COMPLETED)
            .flatMap(queue -> queue.productOrders().stream())
            .map(ProductOrderModel::totalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    this._fragment
        .fragmentBinding()
        .revenue
        .receivedIncomeCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID"));
  }

  public void displayChart(@NonNull ChartModel model) {
    Objects.requireNonNull(model);

    final ViewGroup.MarginLayoutParams margin =
        (ViewGroup.MarginLayoutParams)
            this._fragment.fragmentBinding().revenue.chart.getLayoutParams();
    final int fontSize =
        JsInterface.dpToCssPx(
            this._fragment.requireContext(),
            this._fragment.getResources().getDimension(R.dimen.text_small));

    final String layoutBinding =
        ChartLayoutBinding.init(
            JsInterface.dpToCssPx(
                this._fragment.requireContext(),
                this._fragment.fragmentBinding().revenue.chart.getWidth()),
            JsInterface.dpToCssPx(
                this._fragment.requireContext(),
                this._fragment.fragmentBinding().revenue.chart.getHeight()),
            JsInterface.dpToCssPx(this._fragment.requireContext(), margin.topMargin),
            JsInterface.dpToCssPx(this._fragment.requireContext(), margin.bottomMargin) + fontSize,
            JsInterface.dpToCssPx(this._fragment.requireContext(), margin.leftMargin + 80),
            JsInterface.dpToCssPx(this._fragment.requireContext(), margin.rightMargin),
            fontSize,
            MaterialColors.getColor(
                this._fragment.requireContext(),
                com.google.android.material.R.attr.colorSurface,
                0));
    final String xAxisBinding =
        ChartAxisBinding.withBandScale(
            "layoutBinding", ChartAxisBinding.Position.BOTTOM, model.xAxisDomain(), false);
    final String yAxisBinding =
        ChartAxisBinding.withPercentageLinearScale(
            "layoutBinding", ChartAxisBinding.Position.LEFT, model.yAxisDomain());
    final String chartBinding =
        BarChartBinding.init("layoutBinding", "xAxisBinding", "yAxisBinding");
    final String chartRender = BarChartBinding.render("chartBinding", model.data());

    this._fragment
        .fragmentBinding()
        .revenue
        .chart
        .evaluateJavascript(
            String.format(
                """
                  (() => { // Wrap in a function to avoid variable redeclaration.
                    const layoutBinding = %s;
                    const xAxisBinding = %s;
                    const yAxisBinding = %s;
                    const chartBinding = %s;

                    %s;
                  })();
                  """,
                layoutBinding, xAxisBinding, yAxisBinding, chartBinding, chartRender),
            null);
  }

  public record ChartModel(
      @NonNull List<String> xAxisDomain,
      @NonNull List<String> yAxisDomain,
      @NonNull Map<String, Double> data) {
    public ChartModel {
      Objects.requireNonNull(xAxisDomain);
      Objects.requireNonNull(yAxisDomain);
      Objects.requireNonNull(data);
    }
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

      final DashboardRevenue.OverviewType displayedChart =
          DashboardRevenue.this
              ._fragment
              .dashboardViewModel()
              .revenueView()
              .displayedChart()
              .getValue();
      if (displayedChart == null) return;

      switch (displayedChart) {
        case PROJECTED_INCOME ->
            DashboardRevenue.this
                ._fragment
                .dashboardViewModel()
                .revenueView()
                .onDisplayProjectedIncomeChart();
        case RECEIVED_INCOME ->
            DashboardRevenue.this
                ._fragment
                .dashboardViewModel()
                .revenueView()
                .onDisplayReceivedIncomeChart();
      }
    }
  }
}
