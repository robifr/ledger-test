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

package com.robifr.ledger.ui;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Objects;

/**
 * @param <I> Item to bind.
 * @param <A> Possible action.
 */
public abstract class RecyclerViewHolder<I, A> extends RecyclerView.ViewHolder {
  @NonNull protected final A _action;

  public RecyclerViewHolder(@NonNull View itemView, @NonNull A action) {
    super(itemView);
    this._action = Objects.requireNonNull(action);
  }

  @NonNull
  public A action() {
    return this._action;
  }

  public abstract void bind(@NonNull I item);
}
