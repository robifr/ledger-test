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
import com.robifr.ledger.ui.selectproduct.SelectProductAction;
import com.robifr.ledger.ui.selectproduct.SelectProductFragment;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SelectProductAdapter extends RecyclerView.Adapter<RecyclerViewHolder<?, ?>>
    implements ProductListAction, SelectProductAction {
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

    } else if (holder instanceof SelectProductListHolder<?> listHolder) {
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
  public int expandedProductIndex() {
    return 0;
  }

  @Override
  public void onExpandedProductIndexChanged(int index) {}

  @NonNull
  @Override
  public List<Long> initialSelectedProductIds() {
    final ProductModel initialSelectedProduct =
        this._fragment.selectProductViewModel().initialSelectedProduct();
    return initialSelectedProduct != null && initialSelectedProduct.id() != null
        ? List.of(initialSelectedProduct.id())
        : List.of();
  }

  @Override
  public void onProductSelected(@Nullable ProductModel product) {
    this._fragment.selectProductViewModel().onProductSelected(product);
  }
}
