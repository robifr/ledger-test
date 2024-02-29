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

package com.robifr.ledger.ui.search_product.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ListableListTextBinding;
import com.robifr.ledger.databinding.ProductCardBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.search_product.SearchProductFragment;
import com.robifr.ledger.util.Enums;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SearchProductAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
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

  @NonNull private final SearchProductFragment _fragment;

  public SearchProductAdapter(@NonNull SearchProductFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  @Override
  @NonNull
  public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    final ViewType type =
        Objects.requireNonNull(Enums.valueOf(viewType, ViewType.class, ViewType::value));
    final LayoutInflater inflater = this._fragment.getLayoutInflater();

    return switch (type) {
      case HEADER ->
          new SearchProductHeaderHolder(
              this._fragment, ListableListTextBinding.inflate(inflater, parent, false));

        // Defaults to `ViewType#LIST`.
      default ->
          new SearchProductListHolder(
              this._fragment,
              ProductCardBinding.inflate(this._fragment.getLayoutInflater(), parent, false));
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    if (holder instanceof SearchProductHeaderHolder headerHolder) {
      headerHolder.bind(Optional.empty());

    } else if (holder instanceof SearchProductListHolder listHolder) {
      final List<ProductModel> products =
          this._fragment.searchProductViewModel().products().getValue();

      // -1 offset because header holder.
      if (products != null) listHolder.bind(products.get(index - 1));
    }
  }

  @Override
  public int getItemCount() {
    final List<ProductModel> products =
        this._fragment.searchProductViewModel().products().getValue();
    return products != null ? products.size() + 1 : 0; // +1 offset because header holder.
  }

  @Override
  public int getItemViewType(int index) {
    return switch (index) {
      case 0 -> ViewType.HEADER.value();
      default -> ViewType.LIST.value();
    };
  }
}
