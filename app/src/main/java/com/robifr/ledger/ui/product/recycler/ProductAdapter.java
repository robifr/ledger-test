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

package com.robifr.ledger.ui.product.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ListableListTextBinding;
import com.robifr.ledger.databinding.ProductCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductCardAction;
import com.robifr.ledger.ui.product.ProductFragment;
import com.robifr.ledger.ui.product.ProductListAction;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerViewHolder<?, ?>>
    implements ProductListAction, ProductCardAction {
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

  @NonNull private final ProductFragment _fragment;

  public ProductAdapter(@NonNull ProductFragment fragment) {
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
          new ProductHeaderHolder<>(ListableListTextBinding.inflate(inflater, parent, false), this);

        // Defaults to `ViewType#LIST`.
      default ->
          new ProductListHolder<>(ProductCardWideBinding.inflate(inflater, parent, false), this);
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    if (holder instanceof ProductHeaderHolder<?> headerHolder) {
      headerHolder.bind(Optional.empty());

    } else if (holder instanceof ProductListHolder<?> listHolder) {
      // -1 offset because header holder.
      listHolder.bind(this._fragment.productViewModel().products().getValue().get(index - 1));
    }
  }

  @Override
  public int getItemCount() {
    // +1 offset because header holder.
    return this._fragment.productViewModel().products().getValue().size() + 1;
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
    return this._fragment.productViewModel().products().getValue();
  }

  @Override
  public int expandedProductIndex() {
    return this._fragment.productViewModel().expandedProductIndex().getValue();
  }

  @Override
  public void onExpandedProductIndexChanged(int index) {
    this._fragment.productViewModel().onExpandedProductIndexChanged(index);
  }

  @Override
  public void onDeleteProduct(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    this._fragment.productViewModel().onDeleteProduct(product);
  }
}
