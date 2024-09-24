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

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.ListableListTextBinding;
import com.robifr.ledger.databinding.QueueCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.queue.QueueAction;
import com.robifr.ledger.ui.queue.QueueFragment;
import com.robifr.ledger.ui.queue.QueueListAction;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class QueueAdapter extends RecyclerView.Adapter<RecyclerViewHolder<?, ?>>
    implements QueueListAction, QueueAction {
  private enum ViewType {
    HEADER(0),
    LIST(1);

    private final int _value;

    private ViewType(int value) {
      this._value = value;
    }

    public int value() {
      return this._value;
    }
  }

  @NonNull private final QueueFragment _fragment;

  public QueueAdapter(@NonNull QueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  @Override
  @NonNull
  public RecyclerViewHolder<?, ?> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    final ViewType type =
        Arrays.stream(ViewType.values())
            .filter(e -> e.value() == viewType)
            .findFirst()
            .orElse(ViewType.LIST);
    final LayoutInflater inflater = this._fragment.getLayoutInflater();

    return switch (type) {
      case HEADER ->
          new QueueHeaderHolder<>(ListableListTextBinding.inflate(inflater, parent, false), this);

        // Defaults to `ViewType#LIST`.
      default -> new QueueListHolder<>(QueueCardWideBinding.inflate(inflater, parent, false), this);
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    if (holder instanceof QueueHeaderHolder<?> headerHolder) {
      headerHolder.bind(Optional.empty());

    } else if (holder instanceof QueueListHolder<?> listHolder) {
      // -1 offset because header holder.
      listHolder.bind(this._fragment.queueViewModel().queues().getValue().get(index - 1));
    }
  }

  @Override
  public int getItemCount() {
    // +1 offset because header holder.
    return this._fragment.queueViewModel().queues().getValue().size() + 1;
  }

  @Override
  public int getItemViewType(int index) {
    return switch (index) {
      case 0 -> ViewType.HEADER.value();
      default -> ViewType.LIST.value();
    };
  }

  @Override
  @NonNull
  public List<QueueModel> queues() {
    return this._fragment.queueViewModel().queues().getValue();
  }

  @Override
  public int expandedQueueIndex() {
    return this._fragment.queueViewModel().expandedQueueIndex().getValue();
  }

  @Override
  public void onExpandedQueueIndexChanged(int index) {
    this._fragment.queueViewModel().onExpandedQueueIndexChanged(index);
  }

  @Override
  public void onDeleteQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._fragment.queueViewModel().onDeleteQueue(queue);
  }
}
