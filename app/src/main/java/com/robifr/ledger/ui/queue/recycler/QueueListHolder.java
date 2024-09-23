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

package com.robifr.ledger.ui.queue.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.QueueCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.queue.QueueCardAction;
import com.robifr.ledger.ui.queue.QueueCardWideComponent;
import com.robifr.ledger.ui.queue.QueueListAction;
import java.util.Objects;

public class QueueListHolder<T extends QueueListAction & QueueCardAction>
    extends RecyclerViewHolder<QueueModel, T> implements View.OnClickListener {
  @NonNull private final QueueCardWideBinding _cardBinding;
  @NonNull private final QueueCardWideComponent _card;
  @NonNull private final QueueListMenu _menu;
  @Nullable private QueueModel _boundQueue;

  public QueueListHolder(@NonNull QueueCardWideBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._cardBinding = Objects.requireNonNull(binding);
    this._card = new QueueCardWideComponent(this.itemView.getContext(), this._cardBinding);
    this._menu = new QueueListMenu(this);

    this._cardBinding.cardView.setOnClickListener(this);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
    this._cardBinding.expandedCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull QueueModel queue) {
    this._boundQueue = Objects.requireNonNull(queue);

    final int paymentMethodVisibility =
        this._boundQueue.status() == QueueModel.Status.COMPLETED ? View.VISIBLE : View.GONE;
    this._cardBinding.expandedCard.paymentMethodTitle.setVisibility(paymentMethodVisibility);
    this._cardBinding.expandedCard.paymentMethod.setVisibility(paymentMethodVisibility);

    // Prevent reused view holder card from being expanded.
    final boolean shouldCardExpanded =
        this._action.expandedQueueIndex() != -1
            && this._boundQueue.equals(
                this._action.queues().get(this._action.expandedQueueIndex()));

    this._card.reset();
    this._card.setNormalCardQueue(this._boundQueue);
    this.setCardExpanded(shouldCardExpanded);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.cardView -> {
        final int expandedQueueIndex =
            this._cardBinding.expandedCard.getRoot().getVisibility() != View.VISIBLE
                ? this._action.queues().indexOf(this._boundQueue)
                : -1;
        this._action.onExpandedQueueIndexChanged(expandedQueueIndex);
      }
    }
  }

  @NonNull
  public QueueModel boundQueue() {
    return Objects.requireNonNull(this._boundQueue);
  }

  public void setCardExpanded(boolean isExpanded) {
    Objects.requireNonNull(this._boundQueue);

    this._card.setCardExpanded(isExpanded);
    // Only fill the view when it's shown on screen.
    if (isExpanded) this._card.setExpandedCardQueue(this._boundQueue);
  }
}
