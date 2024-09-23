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
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.robifr.ledger.R;
import com.robifr.ledger.assetbinding.JsInterface;
import com.robifr.ledger.assetbinding.chart.ChartData;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.DashboardCardSummaryBinding;
import com.robifr.ledger.databinding.DashboardCardSummaryListItemBinding;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardSummaryViewModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DashboardSummary implements View.OnClickListener {
  public enum OverviewType {
    TOTAL_QUEUES,
    UNCOMPLETED_QUEUES,
    ACTIVE_CUSTOMERS,
    PRODUCTS_SOLD
  }

  @NonNull private final DashboardFragment _fragment;
  @NonNull private final Chart _chart;

  public DashboardSummary(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._chart =
        new Chart(
            this._fragment.requireContext(),
            this._fragment.fragmentBinding().summary.chart,
            new LocalWebView());

    final DashboardCardSummaryBinding cardBinding = this._fragment.fragmentBinding().summary;
    cardBinding.totalQueuesCardView.setOnClickListener(this);
    cardBinding.totalQueuesCard.icon.setImageResource(R.drawable.icon_assignment);
    cardBinding.totalQueuesCard.title.setText(R.string.text_total_queues);
    cardBinding.uncompletedQueuesCardView.setOnClickListener(this);
    cardBinding.uncompletedQueuesCard.icon.setImageResource(R.drawable.icon_assignment_late);
    cardBinding.uncompletedQueuesCard.title.setText(R.string.text_uncompleted_queues);
    cardBinding.activeCustomersCardView.setOnClickListener(this);
    cardBinding.activeCustomersCard.icon.setImageResource(R.drawable.icon_person);
    cardBinding.activeCustomersCard.title.setText(R.string.text_active_customers);
    cardBinding.productsSoldCardView.setOnClickListener(this);
    cardBinding.productsSoldCard.icon.setImageResource(R.drawable.icon_sell);
    cardBinding.productsSoldCard.title.setText(R.string.text_products_sold);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.totalQueuesCardView,
              R.id.uncompletedQueuesCardView,
              R.id.activeCustomersCardView,
              R.id.productsSoldCardView ->
          this._fragment
              .dashboardViewModel()
              .summaryView()
              .onDisplayedChartChanged(OverviewType.valueOf(view.getTag().toString()));
    }
  }

  public void loadChart() {
    this._chart.load();
  }

  public void selectCard(@NonNull OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    final DashboardCardSummaryBinding cardBinding = this._fragment.fragmentBinding().summary;
    // There should be only one card getting selected.
    cardBinding.totalQueuesCardView.setSelected(false);
    cardBinding.uncompletedQueuesCardView.setSelected(false);
    cardBinding.activeCustomersCardView.setSelected(false);
    cardBinding.productsSoldCardView.setSelected(false);

    switch (overviewType) {
      case TOTAL_QUEUES -> cardBinding.totalQueuesCardView.setSelected(true);
      case UNCOMPLETED_QUEUES -> cardBinding.uncompletedQueuesCardView.setSelected(true);
      case ACTIVE_CUSTOMERS -> cardBinding.activeCustomersCardView.setSelected(true);
      case PRODUCTS_SOLD -> cardBinding.productsSoldCardView.setSelected(true);
    }
  }

  public void setTotalQueues(int amount) {
    this._fragment
        .fragmentBinding()
        .summary
        .totalQueuesCard
        .amount
        .setText(Integer.toString(amount));
  }

  public void displayTotalQueuesChart(
      @NonNull DashboardSummaryViewModel.TotalQueuesChartModel model) {
    Objects.requireNonNull(model);

    TransitionManager.endTransitions(this._fragment.fragmentBinding().getRoot());
    TransitionManager.beginDelayedTransition(
        this._fragment.fragmentBinding().getRoot(), new ChangeBounds());
    this._fragment.fragmentBinding().summary.listContainer.setVisibility(View.GONE);
    this._fragment.fragmentBinding().summary.chart.setVisibility(View.VISIBLE);
    this._chart.displayBarChart(
        model.xAxisDomain(),
        model.yAxisDomain(),
        model.data(),
        MaterialColors.getColor(
            this._fragment.requireContext(), com.google.android.material.R.attr.colorPrimary, 0));
  }

  public void setTotalUncompletedQueues(int amount) {
    this._fragment
        .fragmentBinding()
        .summary
        .uncompletedQueuesCard
        .amount
        .setText(Integer.toString(amount));
  }

  public void displayUncompletedQueuesChart(
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

    TransitionManager.endTransitions(this._fragment.fragmentBinding().getRoot());
    TransitionManager.beginDelayedTransition(
        this._fragment.fragmentBinding().getRoot(), new ChangeBounds());
    this._fragment.fragmentBinding().summary.listContainer.setVisibility(View.GONE);
    this._fragment.fragmentBinding().summary.chart.setVisibility(View.VISIBLE);
    this._chart.displayDonutChart(
        model.data().stream()
            .map(data -> new ChartData.Single<>(this._fragment.getString(data.key()), data.value()))
            .collect(Collectors.toList()),
        model.colors().stream()
            .map(this._fragment.requireContext()::getColor)
            .collect(Collectors.toList()),
        textInCenter);
  }

  public void setTotalActiveCustomers(int amount) {
    this._fragment
        .fragmentBinding()
        .summary
        .activeCustomersCard
        .amount
        .setText(Integer.toString(amount));
  }

  /**
   * @see DashboardSummaryViewModel#mostActiveCustomers()
   */
  public void displayMostActiveCustomersList(@NonNull Map<CustomerModel, Integer> customers) {
    Objects.requireNonNull(customers);

    TransitionManager.endTransitions(this._fragment.fragmentBinding().getRoot());
    TransitionManager.beginDelayedTransition(
        this._fragment.fragmentBinding().getRoot(), new ChangeBounds());
    this._fragment.fragmentBinding().summary.chart.setVisibility(View.GONE);
    this._fragment.fragmentBinding().summary.listContainer.setVisibility(View.VISIBLE);
    this._fragment.fragmentBinding().summary.listContainer.removeAllViews();

    for (Map.Entry<CustomerModel, Integer> customer : customers.entrySet()) {
      final String amountText =
          this._fragment
              .getResources()
              .getQuantityString(R.plurals.args_x_queue, customer.getValue(), customer.getValue());

      final DashboardCardSummaryListItemBinding listItemBinding =
          DashboardCardSummaryListItemBinding.inflate(
              this._fragment.getLayoutInflater(),
              this._fragment.fragmentBinding().summary.listContainer,
              false);
      listItemBinding.title.setText(customer.getKey().name());
      listItemBinding.description.setVisibility(View.GONE);
      listItemBinding.amount.setText(
          HtmlCompat.fromHtml(amountText, HtmlCompat.FROM_HTML_MODE_LEGACY));
      listItemBinding.image.shapeableImage.setShapeAppearanceModel(
          ShapeAppearanceModel.builder(
                  this._fragment.requireContext(),
                  com.google.android.material.R.style.Widget_MaterialComponents_ShapeableImageView,
                  R.style.Shape_Round)
              .build());
      listItemBinding.image.text.setText(
          customer
              .getKey()
              .name()
              .trim()
              .substring(0, Math.min(1, customer.getKey().name().trim().length())));

      this._fragment.fragmentBinding().summary.listContainer.addView(listItemBinding.getRoot());
    }
  }

  public void setTotalProductsSold(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .summary
        .productsSoldCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID", ""));
  }

  /**
   * @see DashboardSummaryViewModel#mostProductsSold()
   */
  public void displayMostProductsSoldList(@NonNull Map<ProductModel, BigDecimal> products) {
    Objects.requireNonNull(products);

    TransitionManager.endTransitions(this._fragment.fragmentBinding().getRoot());
    TransitionManager.beginDelayedTransition(
        this._fragment.fragmentBinding().getRoot(), new ChangeBounds());
    this._fragment.fragmentBinding().summary.chart.setVisibility(View.GONE);
    this._fragment.fragmentBinding().summary.listContainer.setVisibility(View.VISIBLE);
    this._fragment.fragmentBinding().summary.listContainer.removeAllViews();

    for (Map.Entry<ProductModel, BigDecimal> product : products.entrySet()) {
      final String amountText =
          this._fragment
              .getResources()
              .getQuantityString(
                  R.plurals.args_x_item,
                  product.getValue().intValue(),
                  CurrencyFormat.format(product.getValue(), "id", "ID", ""));

      final DashboardCardSummaryListItemBinding listItemBinding =
          DashboardCardSummaryListItemBinding.inflate(
              this._fragment.getLayoutInflater(),
              this._fragment.fragmentBinding().summary.listContainer,
              false);
      listItemBinding.title.setText(product.getKey().name());
      listItemBinding.description.setVisibility(View.GONE);
      listItemBinding.amount.setText(
          HtmlCompat.fromHtml(amountText, HtmlCompat.FROM_HTML_MODE_LEGACY));
      listItemBinding.image.shapeableImage.setShapeAppearanceModel(
          ShapeAppearanceModel.builder(
                  this._fragment.requireContext(),
                  com.google.android.material.R.style.Widget_MaterialComponents_ShapeableImageView,
                  R.style.Shape_Card)
              .build());
      listItemBinding.image.text.setText(
          product
              .getKey()
              .name()
              .trim()
              .substring(0, Math.min(1, product.getKey().name().trim().length())));

      this._fragment.fragmentBinding().summary.listContainer.addView(listItemBinding.getRoot());
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
                      DashboardSummary.this._fragment.requireContext()))
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

      final DashboardSummaryViewModel summaryViewModel =
          DashboardSummary.this._fragment.dashboardViewModel().summaryView();

      switch (summaryViewModel.displayedChart().getValue()) {
        case TOTAL_QUEUES -> summaryViewModel.onDisplayTotalQueuesChart();
        case UNCOMPLETED_QUEUES -> summaryViewModel.onDisplayUncompletedQueuesChart();
        case ACTIVE_CUSTOMERS -> summaryViewModel.onDisplayMostActiveCustomers();
        case PRODUCTS_SOLD -> summaryViewModel.onDisplayMostProductsSold();
      }
    }
  }
}
