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

package com.robifr.ledger.ui.customer.recycler;

import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.ListableListTextBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.customer.CustomerListAction;
import java.util.Objects;
import java.util.Optional;

public class CustomerHeaderHolder<T extends CustomerListAction>
    extends RecyclerViewHolder<Optional<Void>, T> {
  @NonNull private final ListableListTextBinding _textBinding;

  public CustomerHeaderHolder(@NonNull ListableListTextBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._textBinding = Objects.requireNonNull(binding);

    this._textBinding.text.setTextSize(
        TypedValue.COMPLEX_UNIT_PX,
        this.itemView.getContext().getResources().getDimension(R.dimen.text_small));
  }

  @Override
  public void bind(@NonNull Optional<Void> ignore) {
    final int totalCustomers = this._action.customers().size();
    final String text =
        this.itemView
            .getContext()
            .getResources()
            .getQuantityString(
                R.plurals.args_displaying_x_customer, totalCustomers, totalCustomers);

    this._textBinding.text.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));
  }
}
