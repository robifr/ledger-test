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

package com.robifr.ledger.ui.queue.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.QueueCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.queue.QueueCardExpandedComponent;
import com.robifr.ledger.ui.queue.QueueCardNormalComponent;
import com.robifr.ledger.ui.queue.QueueFragment;
import java.util.List;
import java.util.Objects;

public class QueueListHolder extends RecyclerViewHolder<QueueModel>
    implements View.OnClickListener {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueCardWideBinding _cardBinding;
  @NonNull private final QueueCardNormalComponent _normalCard;
  @NonNull private final QueueCardExpandedComponent _expandedCard;
  @NonNull private final QueueListMenu _menu;
  @Nullable private QueueModel _boundQueue;

  public QueueListHolder(@NonNull QueueFragment fragment, @NonNull QueueCardWideBinding binding) {
    super(binding.getRoot());
    this._fragment = Objects.requireNonNull(fragment);
    this._cardBinding = Objects.requireNonNull(binding);
    this._normalCard =
        new QueueCardNormalComponent(this._fragment.requireContext(), this._cardBinding.normalCard);
    this._expandedCard =
        new QueueCardExpandedComponent(
            this._fragment.requireContext(), this._cardBinding.expandedCard);
    this._menu = new QueueListMenu(this._fragment, this);

    this._cardBinding.cardView.setOnClickListener(this);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
    this._cardBinding.expandedCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull QueueModel queue) {
    this._boundQueue = Objects.requireNonNull(queue);

    this._normalCard.setQueue(this._boundQueue);
    this._expandedCard.reset();

    final int paymentMethodVisibility =
        this._boundQueue.status() == QueueModel.Status.COMPLETED ? View.VISIBLE : View.GONE;
    this._cardBinding.expandedCard.paymentMethodTitle.setVisibility(paymentMethodVisibility);
    this._cardBinding.expandedCard.paymentMethod.setVisibility(paymentMethodVisibility);

    // Prevent reused view holder to expand the card
    // if current bound queue is different with selected expanded card.
    final List<QueueModel> queues = this._fragment.queueViewModel().queues().getValue();
    final int expandedQueueIndex =
        Objects.requireNonNullElse(
            this._fragment.queueViewModel().expandedQueueIndex().getValue(), -1);
    final boolean shouldCardExpanded =
        expandedQueueIndex != -1
            && queues != null
            && this._boundQueue.equals(queues.get(expandedQueueIndex));

    this.setCardExpanded(shouldCardExpanded);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.cardView -> {
        final List<QueueModel> queues = this._fragment.queueViewModel().queues().getValue();
        if (queues == null) return;

        // Only expand when it shrank.
        final int expandedQueueIndex =
            this._cardBinding.expandedCard.getRoot().getVisibility() != View.VISIBLE
                ? queues.indexOf(this._boundQueue)
                : -1;
        this._fragment.queueViewModel().onExpandedQueueIndexChanged(expandedQueueIndex);
      }
    }
  }

  @NonNull
  public QueueModel boundQueue() {
    return Objects.requireNonNull(this._boundQueue);
  }

  public void setCardExpanded(boolean isExpanded) {
    Objects.requireNonNull(this._boundQueue);

    final int normalCardVisibility = isExpanded ? View.GONE : View.VISIBLE;
    final int expandedCardVisibility = isExpanded ? View.VISIBLE : View.GONE;

    this._cardBinding.normalCard.getRoot().setVisibility(normalCardVisibility);
    this._cardBinding.expandedCard.getRoot().setVisibility(expandedCardVisibility);

    // Only fill the view when it's shown on screen.
    if (isExpanded) this._expandedCard.setQueue(this._boundQueue);
  }
}
