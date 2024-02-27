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

package com.robifr.ledger.ui.searchable.product.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.RecyclerViewAdapterOld;
import com.robifr.ledger.ui.RecyclerViewHolderOld;
import com.robifr.ledger.util.Enums;
import java.util.Objects;

public class SearchProductAdapter extends RecyclerViewAdapterOld<ProductModel> {
  private enum ViewType {
    HORIZONTAL(0),
    VERTICAL(1);

    private final int _value;

    private ViewType(int value) {
      this._value = value;
    }

    public int value() {
      return this._value;
    }
  }

  private ViewType _currentViewType = ViewType.HORIZONTAL;

  public SearchProductAdapter(@NonNull AppCompatActivity activity) {
    super(activity);
  }

  @Override
  @NonNull
  public RecyclerViewHolderOld onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    final LayoutInflater inflater = this._activity.getLayoutInflater();
    final ViewType type =
        Objects.requireNonNull(Enums.valueOf(viewType, ViewType.class, ViewType::value));

    return switch (type) {
      case VERTICAL ->
          new SearchProductVerticalHolder(
              this._activity, this, inflater.inflate(R.layout.product_card, parent, false));

        // Defaults to `ViewType#HORIZONTAL`.
      default ->
          new SearchProductHorizontalHolder(
              this._activity,
              this,
              inflater.inflate(R.layout.searchable_narrow_card, parent, false));
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolderOld holder, int index) {
    Objects.requireNonNull(holder);

    holder.bind(index);
  }

  @Override
  public int getItemViewType(int index) {
    return this._currentViewType.value();
  }

  public ViewType currentViewType() {
    return this._currentViewType;
  }

  public void setCurrentViewType(ViewType viewType) {
    this._currentViewType = viewType;
  }
}
