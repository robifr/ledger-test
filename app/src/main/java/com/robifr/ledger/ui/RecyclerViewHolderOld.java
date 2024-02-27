/**
 * Copyright (c) 2022-present Robi
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Objects;

public abstract class RecyclerViewHolderOld extends RecyclerView.ViewHolder {
  protected final AppCompatActivity _activity;
  protected final RecyclerViewAdapterOld _adapter;

  public RecyclerViewHolderOld(
      @NonNull AppCompatActivity activity,
      @NonNull RecyclerViewAdapterOld adapter,
      @NonNull View view) {
    super(view);
    this._activity = Objects.requireNonNull(activity);
    this._adapter = Objects.requireNonNull(adapter);
  }

  public abstract void bind(int index);
}
