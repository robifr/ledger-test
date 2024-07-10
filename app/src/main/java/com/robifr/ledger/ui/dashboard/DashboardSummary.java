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
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.DashboardCardSummaryBinding;
import com.robifr.ledger.ui.LocalWebChrome;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class DashboardSummary implements View.OnClickListener {
  public enum OverviewType {
    TOTAL_QUEUES,
    UNCOMPLETED_QUEUES,
    ACTIVE_CUSTOMERS,
    PRODUCTS_SOLD
  }

  @NonNull private final DashboardFragment _fragment;

  public DashboardSummary(@NonNull DashboardFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    final DashboardCardSummaryBinding cardBinding = this._fragment.fragmentBinding().summary;
    cardBinding.chart.getSettings().setSupportZoom(false);
    cardBinding.chart.getSettings().setBuiltInZoomControls(false);
    cardBinding.chart.getSettings().setAllowFileAccess(false);
    cardBinding.chart.getSettings().setJavaScriptEnabled(true);
    cardBinding.chart.setWebChromeClient(new LocalWebChrome());
    cardBinding.chart.setBackgroundColor( // Background color can't be set from xml.
        MaterialColors.getColor(
            this._fragment.requireContext(), com.google.android.material.R.attr.colorSurface, 0));
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

  public void loadChart() {}

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

  public void setTotalProductsSold(@NonNull BigDecimal amount) {
    Objects.requireNonNull(amount);

    this._fragment
        .fragmentBinding()
        .summary
        .productsSoldCard
        .amount
        .setText(CurrencyFormat.format(amount, "id", "ID", ""));
  }
}
