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

package com.robifr.ledger.ui.queue;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.queue.recycler.QueueListHolder;
import com.robifr.ledger.ui.queue.viewmodel.QueueViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class QueueViewModelHandler {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueViewModel _viewModel;

  public QueueViewModelHandler(@NonNull QueueFragment fragment, @NonNull QueueViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel.queues().observe(this._fragment.getViewLifecycleOwner(), this::_onQueues);
    this._viewModel
        .expandedQueueIndex()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onExpandedQueueIndex);

    this._viewModel
        .filterView()
        .inputtedIsNullCustomerShown()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onNullCustomerShown);
    this._viewModel
        .filterView()
        .inputtedStatus()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilteredStatus);
    this._viewModel
        .filterView()
        .inputtedDate()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilteredDate);
    this._viewModel
        .filterView()
        .inputtedMinTotalPriceText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilteredMinTotalPriceText);
    this._viewModel
        .filterView()
        .inputtedMaxTotalPriceText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilteredMaxTotalPriceText);
  }

  private void _onSnackbarMessage(@NonNull StringResources stringRes) {
    Objects.requireNonNull(stringRes);

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onQueues(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    this._fragment.adapter().notifyDataSetChanged();
  }

  private void _onExpandedQueueIndex(int index) {
    // Shrink all cards.
    for (int i = 0; i < this._fragment.fragmentBinding().recyclerView.getChildCount(); i++) {
      final RecyclerView.ViewHolder viewHolder =
          this._fragment
              .fragmentBinding()
              .recyclerView
              .getChildViewHolder(this._fragment.fragmentBinding().recyclerView.getChildAt(i));

      if (viewHolder instanceof QueueListHolder<?> holder) holder.setCardExpanded(false);
    }

    // Expand the selected card.
    if (index != -1) {
      final RecyclerView.ViewHolder viewHolder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (viewHolder instanceof QueueListHolder<?> holder) holder.setCardExpanded(true);
    }
  }

  private void _onNullCustomerShown(boolean isShown) {
    this._fragment.filter().filterCustomer().setNullCustomerShown(isShown);
  }

  private void _onFilteredStatus(@NonNull Set<QueueModel.Status> status) {
    Objects.requireNonNull(status);

    this._fragment.filter().filterStatus().setFilteredStatus(status);
  }

  private void _onFilteredDate(@NonNull QueueDate date) {
    Objects.requireNonNull(date);

    this._fragment.filter().filterDate().setFilteredDate(date);
  }

  private void _onFilteredMinTotalPriceText(@NonNull String minTotalPrice) {
    Objects.requireNonNull(minTotalPrice);

    this._fragment.filter().filterTotalPrice().setFilteredMinTotalPriceText(minTotalPrice);
  }

  private void _onFilteredMaxTotalPriceText(@NonNull String maxTotalPrice) {
    Objects.requireNonNull(maxTotalPrice);

    this._fragment.filter().filterTotalPrice().setFilteredMaxTotalPriceText(maxTotalPrice);
  }
}
