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

import android.content.Context;
import android.webkit.WebView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewClientCompat;
import com.google.android.material.color.MaterialColors;
import com.robifr.ledger.R;
import com.robifr.ledger.assetbinding.JsInterface;
import com.robifr.ledger.assetbinding.chart.ChartBinding;
import com.robifr.ledger.assetbinding.chart.ChartData;
import com.robifr.ledger.assetbinding.chart.ChartLayoutBinding;
import com.robifr.ledger.assetbinding.chart.ChartScaleBinding;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Chart {
  @NonNull private final Context _context;
  @NonNull private final WebView _webView;

  public Chart(
      @NonNull Context context,
      @NonNull WebView webView,
      @NonNull WebViewClientCompat webViewClient) {
    Objects.requireNonNull(webViewClient);

    this._context = Objects.requireNonNull(context);
    this._webView = Objects.requireNonNull(webView);

    this._webView.getSettings().setSupportZoom(false);
    this._webView.getSettings().setBuiltInZoomControls(false);
    this._webView.getSettings().setAllowFileAccess(false);
    this._webView.getSettings().setJavaScriptEnabled(true);
    this._webView.addJavascriptInterface(new JsInterface(this._context), JsInterface.NAME);
    this._webView.setWebViewClient(webViewClient);
    this._webView.setBackgroundColor( // Background color can't be set from xml.
        MaterialColors.getColor(this._context, com.google.android.material.R.attr.colorSurface, 0));
  }

  public void load() {
    this._webView.loadUrl("https://appassets.androidplatform.net/assets/chart/index.html");
  }

  /**
   * @see ChartBinding#renderBarChart
   */
  public <K, V> void displayBarChart(
      @NonNull List<String> xAxisDomain,
      @NonNull List<Double> yAxisDomain,
      @NonNull List<ChartData.Single<K, V>> data,
      @ColorInt int color) {
    Objects.requireNonNull(xAxisDomain);
    Objects.requireNonNull(yAxisDomain);
    Objects.requireNonNull(data);

    final int fontSize =
        JsInterface.dpToCssPx(
            this._context, this._context.getResources().getDimension(R.dimen.text_small));

    final String layoutBinding =
        ChartLayoutBinding.init(
            JsInterface.dpToCssPx(this._context, this._webView.getWidth()),
            JsInterface.dpToCssPx(this._context, this._webView.getHeight()),
            0,
            0,
            0,
            0,
            fontSize,
            MaterialColors.getColor(
                this._context, com.google.android.material.R.attr.colorSurface, 0));
    final String xScaleBinding =
        ChartScaleBinding.createBandScale(
            ChartScaleBinding.AxisPosition.BOTTOM, xAxisDomain, false);
    final String yScaleBinding =
        ChartScaleBinding.createLinearScale(ChartScaleBinding.AxisPosition.LEFT, yAxisDomain);
    final String chartRender =
        ChartBinding.renderBarChart("layoutBinding", "xScaleBinding", "yScaleBinding", data, color);

    this._webView.evaluateJavascript(
        String.format(
            """
            (() => { // Wrap in a function to avoid variable redeclaration.
              const layoutBinding = %s;
              const xScaleBinding = %s;
              const yScaleBinding = %s;

              %s;
            })();
            """,
            layoutBinding, xScaleBinding, yScaleBinding, chartRender),
        null);
  }

  /**
   * @see ChartBinding#renderStackedBarChart
   */
  public <K, V, G> void displayStackedBarChartWithLargeValue(
      @NonNull List<String> xAxisDomain,
      @NonNull List<String> yAxisDomain,
      @NonNull List<ChartData.Multiple<K, V, G>> data,
      @NonNull @ColorInt List<Integer> colors,
      @NonNull Set<String> groupInOrder) {
    Objects.requireNonNull(xAxisDomain);
    Objects.requireNonNull(yAxisDomain);
    Objects.requireNonNull(data);
    Objects.requireNonNull(colors);
    Objects.requireNonNull(groupInOrder);

    final int fontSize =
        JsInterface.dpToCssPx(
            this._context, this._context.getResources().getDimension(R.dimen.text_small));

    final String layoutBinding =
        ChartLayoutBinding.init(
            JsInterface.dpToCssPx(this._context, this._webView.getWidth()),
            JsInterface.dpToCssPx(this._context, this._webView.getHeight()),
            0,
            0,
            0,
            0,
            fontSize,
            MaterialColors.getColor(
                this._context, com.google.android.material.R.attr.colorSurface, 0));
    final String xScaleBinding =
        ChartScaleBinding.createBandScale(
            ChartScaleBinding.AxisPosition.BOTTOM, xAxisDomain, false);
    final String yScaleBinding =
        ChartScaleBinding.createPercentageLinearScale(
            ChartScaleBinding.AxisPosition.LEFT, yAxisDomain);
    final String chartRender =
        ChartBinding.renderStackedBarChart(
            "layoutBinding", "xScaleBinding", "yScaleBinding", data, colors, groupInOrder);

    this._webView.evaluateJavascript(
        String.format(
            """
            (() => { // Wrap in a function to avoid variable redeclaration.
              const layoutBinding = %s;
              const xScaleBinding = %s;
              const yScaleBinding = %s;

              %s;
            })();
            """,
            layoutBinding, xScaleBinding, yScaleBinding, chartRender),
        null);
  }

  /**
   * @see ChartBinding#renderDonutChart
   */
  public <K, V> void displayDonutChart(
      @NonNull List<ChartData.Single<K, V>> data,
      @NonNull @ColorInt List<Integer> colors,
      @Nullable String svgTextInCenter) {
    Objects.requireNonNull(data);
    Objects.requireNonNull(colors);

    final int fontSize =
        JsInterface.dpToCssPx(
            this._context, this._context.getResources().getDimension(R.dimen.text_small));

    final String layoutBinding =
        ChartLayoutBinding.init(
            JsInterface.dpToCssPx(this._context, this._webView.getWidth()),
            JsInterface.dpToCssPx(this._context, this._webView.getHeight()),
            0,
            0,
            0,
            0,
            fontSize,
            MaterialColors.getColor(
                this._context, com.google.android.material.R.attr.colorSurface, 0));
    final String chartRender =
        ChartBinding.renderDonutChart("layoutBinding", data, colors, svgTextInCenter);

    this._webView.evaluateJavascript(
        String.format(
            """
              (() => { // Wrap in a function to avoid variable redeclaration.
                const layoutBinding = %s;

                %s;
              })();
              """,
            layoutBinding, chartRender),
        null);
  }
}
