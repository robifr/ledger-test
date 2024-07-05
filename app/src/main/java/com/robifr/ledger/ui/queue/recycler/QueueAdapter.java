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

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.databinding.ListableListTextBinding;
import com.robifr.ledger.databinding.QueueCardWideBinding;
import com.robifr.ledger.ui.queue.QueueFragment;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class QueueAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    final ViewType type =
        Arrays.stream(ViewType.values())
            .filter(e -> e.value() == viewType)
            .findFirst()
            .orElse(ViewType.LIST);
    final LayoutInflater inflater = this._fragment.getLayoutInflater();

    return switch (type) {
      case HEADER ->
          new QueueHeaderHolder(
              this._fragment, ListableListTextBinding.inflate(inflater, parent, false));

        // Defaults to `ViewType#LIST`.
      default ->
          new QueueListHolder(
              this._fragment, QueueCardWideBinding.inflate(inflater, parent, false));
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    if (holder instanceof QueueHeaderHolder headerHolder) {
      headerHolder.bind(Optional.empty());

    } else if (holder instanceof QueueListHolder listHolder) {
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
}
