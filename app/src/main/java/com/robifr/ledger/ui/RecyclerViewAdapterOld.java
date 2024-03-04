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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class RecyclerViewAdapterOld<T>
    extends RecyclerView.Adapter<RecyclerViewHolderOld> {
  protected final AppCompatActivity _activity;
  protected final ArrayList<T> _list = new ArrayList<>();

  public RecyclerViewAdapterOld(@NonNull AppCompatActivity activity) {
    this._activity = Objects.requireNonNull(activity);
  }

  public List<T> list() {
    return this._list;
  }

  @Override
  public int getItemCount() {
    return this._list.size();
  }
}
