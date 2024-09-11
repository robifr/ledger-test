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
import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.DashboardCardSummaryBinding;
import com.robifr.ledger.databinding.DashboardCardSummaryListItemBinding;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardSummaryViewModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

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

  @NonNull
  public Chart chart() {
    return this._chart;
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

  public void setTotalUncompletedQueues(int amount) {
    this._fragment
        .fragmentBinding()
        .summary
        .uncompletedQueuesCard
        .amount
        .setText(Integer.toString(amount));
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
  public void displayMostActiveCustomersList(@NonNull Map<CustomerModel, Long> customers) {
    Objects.requireNonNull(customers);

    this._fragment.fragmentBinding().summary.chart.setVisibility(View.GONE);
    this._fragment.fragmentBinding().summary.listContainer.setVisibility(View.VISIBLE);
    this._fragment.fragmentBinding().summary.listContainer.removeAllViews();

    for (Map.Entry<CustomerModel, Long> customer : customers.entrySet()) {
      final DashboardCardSummaryListItemBinding listItemBinding =
          DashboardCardSummaryListItemBinding.inflate(
              this._fragment.getLayoutInflater(),
              this._fragment.fragmentBinding().summary.listContainer,
              false);
      listItemBinding.title.setText(customer.getKey().name());
      listItemBinding.description.setVisibility(View.GONE);
      listItemBinding.amount.setText(customer.getValue().toString());
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
      }
    }
  }
}
