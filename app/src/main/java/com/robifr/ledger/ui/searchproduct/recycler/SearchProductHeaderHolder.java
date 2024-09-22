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

package com.robifr.ledger.ui.searchproduct.recycler;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.ListableListTextBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductListAction;
import java.util.Objects;
import java.util.Optional;

public class SearchProductHeaderHolder<T extends ProductListAction>
    extends RecyclerViewHolder<Optional<Void>, T> {
  @NonNull private final ListableListTextBinding _textBinding;

  public SearchProductHeaderHolder(@NonNull ListableListTextBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._textBinding = Objects.requireNonNull(binding);
  }

  @Override
  public void bind(@NonNull Optional<Void> ignore) {
    final int totalProducts = this._action.products().size();
    final String text =
        this.itemView
            .getContext()
            .getResources()
            .getQuantityString(R.plurals.args_found_x_product, totalProducts, totalProducts);

    this._textBinding.text.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));
  }
}
