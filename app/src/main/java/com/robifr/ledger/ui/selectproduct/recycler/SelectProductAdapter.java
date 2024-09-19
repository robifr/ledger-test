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

package com.robifr.ledger.ui.selectproduct.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ListableListSelectedItemBinding;
import com.robifr.ledger.databinding.ProductCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductListAction;
import com.robifr.ledger.ui.selectproduct.SelectProductCardAction;
import com.robifr.ledger.ui.selectproduct.SelectProductFragment;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SelectProductAdapter extends RecyclerView.Adapter<RecyclerViewHolder>
    implements ProductListAction, SelectProductCardAction {
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

  @NonNull private final SelectProductFragment _fragment;

  public SelectProductAdapter(@NonNull SelectProductFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  @Override
  @NonNull
  public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    final ViewType type =
        Arrays.stream(ViewType.values())
            .filter(e -> e.value() == viewType)
            .findFirst()
            .orElse(ViewType.LIST);
    final LayoutInflater inflater = this._fragment.getLayoutInflater();

    return switch (type) {
      case HEADER ->
          new SelectProductHeaderHolder<>(
              ListableListSelectedItemBinding.inflate(inflater, parent, false), this);

        // Defaults to `ViewType#LIST`.
      default ->
          new SelectProductListHolder<>(
              ProductCardWideBinding.inflate(inflater, parent, false), this);
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    if (holder instanceof SelectProductHeaderHolder<?> headerHolder) {
      headerHolder.bind(
          Optional.ofNullable(this._fragment.selectProductViewModel().initialSelectedProduct()));

    } else if (holder instanceof SelectProductListHolder listHolder) {
      // -1 offset because header holder.
      listHolder.bind(this._fragment.selectProductViewModel().products().getValue().get(index - 1));
    }
  }

  @Override
  public int getItemCount() {
    // +1 offset because header holder.
    return this._fragment.selectProductViewModel().products().getValue().size() + 1;
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
  public List<ProductModel> products() {
    return this._fragment.selectProductViewModel().products().getValue();
  }

  @Override
  @Nullable
  public ProductModel initialSelectedProduct() {
    return this._fragment.selectProductViewModel().initialSelectedProduct();
  }

  @Override
  public void onProductSelected(@Nullable ProductModel product) {
    this._fragment.selectProductViewModel().onProductSelected(product);
  }
}
