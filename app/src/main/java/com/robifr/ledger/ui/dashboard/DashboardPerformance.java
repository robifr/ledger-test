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
import com.robifr.ledger.data.model.QueueWithProductOrdersInfo;
import com.robifr.ledger.databinding.DashboardCardPerformanceBinding;
import com.robifr.ledger.ui.LocalWebChrome;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardPerformance implements View.OnClickListener {
  public enum OverviewType {
    PROJECTED_INCOME,
    RECEIVED_INCOME,
    TOTAL_QUEUE,
    PRODUCTS_SOLD
  }

  @NonNull private final DashboardFragment _fragment;

  public DashboardPerformance(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    final DashboardCardPerformanceBinding cardBinding =
        this._fragment.fragmentBinding().performance;
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
    cardBinding.totalQueueCardView.setOnClickListener(this);
    cardBinding.totalQueueCard.icon.setImageResource(R.drawable.icon_assignment);
    cardBinding.totalQueueCard.title.setText(R.string.text_total_queue);
    cardBinding.totalQueueCard.description.setVisibility(View.GONE);
    cardBinding.productsSoldCardView.setOnClickListener(this);
    cardBinding.productsSoldCard.icon.setImageResource(R.drawable.icon_sell);
    cardBinding.productsSoldCard.title.setText(R.string.text_products_sold);
    cardBinding.productsSoldCard.description.setVisibility(View.GONE);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.projectedIncomeCardView,
              R.id.receivedIncomeCardView,
              R.id.totalQueueCardView,
              R.id.productsSoldCardView ->
          this._fragment
              .dashboardViewModel()
              .performanceView()
              .onDisplayedChartChanged(OverviewType.valueOf(view.getTag().toString()));
    }
  }

  public void selectCard(@NonNull OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    final DashboardCardPerformanceBinding cardBinding =
        this._fragment.fragmentBinding().performance;
    // There should be only one card getting selected.
    cardBinding.projectedIncomeCardView.setSelected(false);
    cardBinding.receivedIncomeCardView.setSelected(false);
    cardBinding.totalQueueCardView.setSelected(false);
    cardBinding.productsSoldCardView.setSelected(false);

    switch (overviewType) {
      case PROJECTED_INCOME -> cardBinding.projectedIncomeCardView.setSelected(true);
      case RECEIVED_INCOME -> cardBinding.receivedIncomeCardView.setSelected(true);
      case TOTAL_QUEUE -> cardBinding.totalQueueCardView.setSelected(true);
      case PRODUCTS_SOLD -> cardBinding.productsSoldCardView.setSelected(true);
    }
  }

  public void loadChart() {
    this._fragment
        .fragmentBinding()
        .performance
        .chart
        .loadUrl("https://appassets.androidplatform.net/assets/chart.html");
  }

  public void setTotalQueue(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    this._fragment
        .fragmentBinding()
        .performance
        .totalQueueCard
        .amount
        .setText(Integer.toString(queueInfo.size()));
  }

  public void setTotalProjectedIncome(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    final BigDecimal amount =
        queueInfo.stream()
            .flatMap(queue -> queue.productOrders().stream())
            .map(ProductOrderModel::totalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    this._fragment
        .fragmentBinding()
        .performance
        .projectedIncomeCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID"));
  }

  public void setTotalReceivedIncome(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    final BigDecimal amount =
        queueInfo.stream()
            // Received income are from the completed queues.
            .filter(queue -> queue.status() == QueueModel.Status.COMPLETED)
            .flatMap(queue -> queue.productOrders().stream())
            .map(ProductOrderModel::totalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    this._fragment
        .fragmentBinding()
        .performance
        .receivedIncomeCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID"));
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
        .productsSoldCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID", "")); // Format the decimal point.
  }

  public void displayChart(@NonNull ChartModel model) {
    Objects.requireNonNull(model);

    final ViewGroup.MarginLayoutParams margin =
        (ViewGroup.MarginLayoutParams)
            this._fragment.fragmentBinding().performance.chart.getLayoutParams();
    final int fontSize =
        JsInterface.dpToCssPx(
            this._fragment.requireContext(),
            this._fragment.getResources().getDimension(R.dimen.text_small));

    final String layoutBinding =
        ChartLayoutBinding.init(
            JsInterface.dpToCssPx(
                this._fragment.requireContext(),
                this._fragment.fragmentBinding().performance.chart.getWidth()),
            JsInterface.dpToCssPx(
                this._fragment.requireContext(),
                this._fragment.fragmentBinding().performance.chart.getHeight()),
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
        .performance
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
                      DashboardPerformance.this._fragment.requireContext()))
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

      final DashboardPerformance.OverviewType displayedChart =
          DashboardPerformance.this
              ._fragment
              .dashboardViewModel()
              .performanceView()
              .displayedChart()
              .getValue();
      if (displayedChart == null) return;

      switch (displayedChart) {
        case PROJECTED_INCOME ->
            DashboardPerformance.this
                ._fragment
                .dashboardViewModel()
                .performanceView()
                .onDisplayProjectedIncomeChart();
        case RECEIVED_INCOME ->
            DashboardPerformance.this
                ._fragment
                .dashboardViewModel()
                .performanceView()
                .onDisplayReceivedIncomeChart();
        case TOTAL_QUEUE ->
            DashboardPerformance.this
                ._fragment
                .dashboardViewModel()
                .performanceView()
                .onDisplayTotalQueueChart();
        case PRODUCTS_SOLD ->
            DashboardPerformance.this
                ._fragment
                .dashboardViewModel()
                .performanceView()
                .onDisplayProductsSoldChart();
      }
    }
  }
}
