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
import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import com.google.android.material.color.MaterialColors;
import com.robifr.ledger.R;
import com.robifr.ledger.assetbinding.JsInterface;
import com.robifr.ledger.assetbinding.chart.BarChartBinding;
import com.robifr.ledger.assetbinding.chart.ChartAxisBinding;
import com.robifr.ledger.assetbinding.chart.ChartLayoutBinding;
import com.robifr.ledger.assetbinding.chart.ChartUtil;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueWithProductOrdersInfo;
import com.robifr.ledger.databinding.DashboardCardPerformanceBinding;
import com.robifr.ledger.ui.LocalWebChrome;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardPerformance implements View.OnClickListener {
  public enum OverviewType {
    PROJECTED_INCOME,
    ORDERED_PRODUCTS
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
    cardBinding.orderedProductsCardView.setOnClickListener(this);
    cardBinding.orderedProductsCard.icon.setImageResource(R.drawable.icon_orders);
    cardBinding.orderedProductsCard.title.setText(R.string.text_ordered_products);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.projectedIncomeCardView, R.id.orderedProductsCardView -> {
        final OverviewType selectedOverview = OverviewType.valueOf(view.getTag().toString());

        this._fragment.dashboardViewModel().onDisplayedPerformanceChartChanged(selectedOverview);
        this.selectCard(selectedOverview);
      }
    }
  }

  public void selectCard(@NonNull OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    final DashboardCardPerformanceBinding cardBinding =
        this._fragment.fragmentBinding().performance;
    // There should be only one card getting selected.
    cardBinding.projectedIncomeCardView.setSelected(false);
    cardBinding.orderedProductsCardView.setSelected(false);

    switch (overviewType) {
      case PROJECTED_INCOME -> cardBinding.projectedIncomeCardView.setSelected(true);
      case ORDERED_PRODUCTS -> cardBinding.orderedProductsCardView.setSelected(true);
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

    final String totalText =
        this._fragment
            .getResources()
            .getQuantityString(R.plurals.args_from_x_queues, queueInfo.size(), queueInfo.size());

    this._fragment
        .fragmentBinding()
        .performance
        .totalQueue
        .setText(HtmlCompat.fromHtml(totalText, HtmlCompat.FROM_HTML_MODE_LEGACY));
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

  public void setTotalOrderedProducts(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    final long amount = queueInfo.stream().mapToLong(queue -> queue.productOrders().size()).sum();

    this._fragment
        .fragmentBinding()
        .performance
        .orderedProductsCard
        .amount
        .setText(Long.toString(amount));
  }

  private void _displayProjectedIncomeChart(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    final QueueDate date = this._fragment.dashboardViewModel().date().getValue();
    if (date == null) return;

    final Map<ZonedDateTime, BigDecimal> unformattedQueueDateWithTotalPrice = new LinkedHashMap<>();

    for (QueueWithProductOrdersInfo queue : queueInfo) {
      for (ProductOrderModel productOrder : queue.productOrders()) {
        unformattedQueueDateWithTotalPrice.merge(
            queue.date().atZone(ZoneId.systemDefault()),
            productOrder.totalPrice(),
            BigDecimal::add);
      }
    }

    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queueInfo.stream()
                .map(QueueWithProductOrdersInfo::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final Map<String, BigDecimal> queueDateWithTotalPrice =
        ChartUtil.toDateTimeData(
            unformattedQueueDateWithTotalPrice, new Pair<>(startDate, date.dateEnd()));

    final List<String> xAxisDomain = new ArrayList<>(queueDateWithTotalPrice.keySet());
    // Convert to percent because D3.js can't handle big decimal.
    final List<String> yAxisDomain = ChartUtil.toPercentageLinearDomain(queueDateWithTotalPrice);

    this._displayChart(
        xAxisDomain,
        yAxisDomain,
        ChartUtil.toPercentageData(queueDateWithTotalPrice, LinkedHashMap::new));
  }

  private void _displayOrderedProductsChart(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    final QueueDate date = this._fragment.dashboardViewModel().date().getValue();
    if (date == null) return;

    final Map<ZonedDateTime, BigDecimal> unformattedQueueDateWithTotalOrders =
        new LinkedHashMap<>();

    for (QueueWithProductOrdersInfo queue : queueInfo) {
      unformattedQueueDateWithTotalOrders.merge(
          queue.date().atZone(ZoneId.systemDefault()),
          BigDecimal.valueOf(queue.productOrders().size()),
          BigDecimal::add);
    }

    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queueInfo.stream()
                .map(QueueWithProductOrdersInfo::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final Map<String, BigDecimal> queueDateWithTotalOrders =
        ChartUtil.toDateTimeData(
            unformattedQueueDateWithTotalOrders, new Pair<>(startDate, date.dateEnd()));

    final List<String> xAxisDomain = new ArrayList<>(queueDateWithTotalOrders.keySet());
    // Convert to percent because D3.js can't handle big decimal.
    final List<String> yAxisDomain = ChartUtil.toPercentageLinearDomain(queueDateWithTotalOrders);

    this._displayChart(
        xAxisDomain,
        yAxisDomain,
        ChartUtil.toPercentageData(queueDateWithTotalOrders, LinkedHashMap::new));
  }

  private void _displayChart(
      @NonNull List<String> xAxisDomain,
      @NonNull List<String> yAxisDomain,
      @NonNull Map<String, Double> data) {
    Objects.requireNonNull(xAxisDomain);
    Objects.requireNonNull(yAxisDomain);
    Objects.requireNonNull(data);

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
            "layoutBinding", ChartAxisBinding.Position.BOTTOM, xAxisDomain, false);
    final String yAxisBinding =
        ChartAxisBinding.withPercentageLinearScale(
            "layoutBinding", ChartAxisBinding.Position.LEFT, yAxisDomain);
    final String chartBinding =
        BarChartBinding.init("layoutBinding", "xAxisBinding", "yAxisBinding");
    final String chartRender = BarChartBinding.render("chartBinding", data);

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

      final List<QueueWithProductOrdersInfo> queueInfo =
          DashboardPerformance.this
              ._fragment
              .dashboardViewModel()
              .queuesWithProductOrders()
              .getValue();
      final DashboardPerformance.OverviewType displayedPerformanceChart =
          DashboardPerformance.this
              ._fragment
              .dashboardViewModel()
              .displayedPerformanceChart()
              .getValue();
      if (queueInfo == null || displayedPerformanceChart == null) return;

      switch (displayedPerformanceChart) {
        case PROJECTED_INCOME -> DashboardPerformance.this._displayProjectedIncomeChart(queueInfo);
        case ORDERED_PRODUCTS -> DashboardPerformance.this._displayOrderedProductsChart(queueInfo);
      }
    }
  }
}
