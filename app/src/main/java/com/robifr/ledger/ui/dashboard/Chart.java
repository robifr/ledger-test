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

import android.content.Context;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.webkit.WebViewClientCompat;
import com.google.android.material.color.MaterialColors;
import com.robifr.ledger.R;
import com.robifr.ledger.assetbinding.JsInterface;
import com.robifr.ledger.assetbinding.chart.ChartBinding;
import com.robifr.ledger.assetbinding.chart.ChartLayoutBinding;
import com.robifr.ledger.assetbinding.chart.ChartScaleBinding;
import com.robifr.ledger.ui.LocalWebChrome;
import java.util.Objects;

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
    this._webView.setWebChromeClient(new LocalWebChrome()); // To show log from the JavaScript.
    this._webView.setBackgroundColor( // Background color can't be set from xml.
        MaterialColors.getColor(this._context, com.google.android.material.R.attr.colorSurface, 0));
  }

  public void load() {
    this._webView.loadUrl("https://appassets.androidplatform.net/assets/chart/index.html");
  }

  public void displayBarChart(@NonNull CartesianChartModel<String, String> model) {
    Objects.requireNonNull(model);

    final ViewGroup.MarginLayoutParams margin =
        (ViewGroup.MarginLayoutParams) this._webView.getLayoutParams();
    final int fontSize =
        JsInterface.dpToCssPx(
            this._context, this._context.getResources().getDimension(R.dimen.text_small));

    final String layoutBinding =
        ChartLayoutBinding.init(
            JsInterface.dpToCssPx(this._context, this._webView.getWidth()),
            JsInterface.dpToCssPx(this._context, this._webView.getHeight()),
            JsInterface.dpToCssPx(this._context, margin.topMargin),
            JsInterface.dpToCssPx(this._context, margin.bottomMargin) + fontSize,
            JsInterface.dpToCssPx(this._context, margin.leftMargin + 80), // Prevent text truncated.
            JsInterface.dpToCssPx(this._context, margin.rightMargin),
            fontSize,
            MaterialColors.getColor(
                this._context, com.google.android.material.R.attr.colorSurface, 0));
    final String xScaleBinding =
        ChartScaleBinding.createBandScale(
            "layoutBinding", ChartScaleBinding.AxisPosition.BOTTOM, model.xAxisDomain(), false);
    final String yScaleBinding =
        ChartScaleBinding.createPercentageLinearScale(
            "layoutBinding", ChartScaleBinding.AxisPosition.LEFT, model.yAxisDomain());
    final String chartRender =
        ChartBinding.renderBarChart(
            "layoutBinding", "xScaleBinding", "yScaleBinding", model.data());

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
}
