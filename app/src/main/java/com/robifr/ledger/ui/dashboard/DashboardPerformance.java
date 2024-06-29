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

public class DashboardPerformance {
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
    cardBinding.income.icon.setImageResource(R.drawable.icon_paid);
    cardBinding.income.title.setText(R.string.text_income);
    cardBinding.orderedProducts.icon.setImageResource(R.drawable.icon_orders);
    cardBinding.orderedProducts.title.setText(R.string.text_ordered_products);
  }

  public void loadIncomeChart() {
    this._fragment
        .fragmentBinding()
        .performance
        .chart
        .loadUrl("https://appassets.androidplatform.net/assets/chart.html");
    this._fragment.fragmentBinding().performance.income.cardView.setSelected(true);
    this._fragment.fragmentBinding().performance.orderedProducts.cardView.setSelected(false);
  }

  public void setTotalIncome(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
    Objects.requireNonNull(queueInfo);

    final String totalText =
        this._fragment
            .getResources()
            .getQuantityString(R.plurals.args_from_x_queues, queueInfo.size(), queueInfo.size());
    final BigDecimal amount =
        queueInfo.stream()
            .flatMap(queue -> queue.productOrders().stream())
            .map(ProductOrderModel::totalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    this._fragment
        .fragmentBinding()
        .performance
        .totalQueue
        .setText(HtmlCompat.fromHtml(totalText, HtmlCompat.FROM_HTML_MODE_LEGACY));
    this._fragment
        .fragmentBinding()
        .performance
        .income
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID"));
  }

  private void _displayChart(@NonNull List<QueueWithProductOrdersInfo> queueInfo) {
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
    final Map<String, Double> queueDateWithTotalPriceInPercent =
        ChartUtil.toPercentageData(queueDateWithTotalPrice, LinkedHashMap::new);

    final List<String> xAxisDomain = new ArrayList<>(queueDateWithTotalPrice.keySet());
    final List<String> yAxisDomain = ChartUtil.toPercentageLinearDomain(queueDateWithTotalPrice);

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
    final String chartRender =
        BarChartBinding.render("chartBinding", queueDateWithTotalPriceInPercent);

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
      if (queueInfo != null) DashboardPerformance.this._displayChart(queueInfo);
    }
  }
}
