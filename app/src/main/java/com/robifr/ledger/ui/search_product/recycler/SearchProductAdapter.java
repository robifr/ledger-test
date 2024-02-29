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

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ProductCardBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.search_product.SearchProductFragment;
import java.util.List;
import java.util.Objects;

public class SearchProductAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
  @NonNull private final SearchProductFragment _fragment;

  public SearchProductAdapter(@NonNull SearchProductFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  @Override
  @NonNull
  public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    return new SearchProductListHolder(
        this._fragment,
        ProductCardBinding.inflate(this._fragment.getLayoutInflater(), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    final List<ProductModel> products =
        this._fragment.searchProductViewModel().products().getValue();
    if (products == null) return;

    if (holder instanceof SearchProductListHolder listHolder) listHolder.bind(products.get(index));
  }

  @Override
  public int getItemCount() {
    final List<ProductModel> products =
        this._fragment.searchProductViewModel().products().getValue();
    return products != null ? products.size() : 0;
  }
}
