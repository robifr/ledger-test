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

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.customer.CustomerAction;
import com.robifr.ledger.ui.customer.CustomerCardWideComponent;
import com.robifr.ledger.ui.customer.CustomerListAction;
import java.util.Objects;

public class CustomerListHolder<T extends CustomerListAction & CustomerAction>
    extends RecyclerViewHolder<CustomerModel, T> implements View.OnClickListener {
  @NonNull protected final CustomerCardWideBinding _cardBinding;
  @NonNull protected final CustomerCardWideComponent _card;
  @NonNull protected final CustomerListMenu _menu;
  @Nullable protected CustomerModel _boundCustomer;

  public CustomerListHolder(@NonNull CustomerCardWideBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._cardBinding = Objects.requireNonNull(binding);
    this._card = new CustomerCardWideComponent(this.itemView.getContext(), this._cardBinding);
    this._menu = new CustomerListMenu(this);

    this._cardBinding.cardView.setOnClickListener(this);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
    this._cardBinding.expandedCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull CustomerModel customer) {
    this._boundCustomer = Objects.requireNonNull(customer);
    // Prevent reused view holder card from being expanded.
    final boolean shouldCardExpanded =
        this._action.expandedCustomerIndex() != -1
            && this._boundCustomer.equals(
                this._action.customers().get(this._action.expandedCustomerIndex()));

    this._card.reset();
    this._card.setNormalCardCustomer(this._boundCustomer);
    this.setCardExpanded(shouldCardExpanded);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.cardView -> {
        final int expandedCustomerIndex =
            this._cardBinding.expandedCard.getRoot().getVisibility() != View.VISIBLE
                ? this._action.customers().indexOf(this._boundCustomer)
                : -1;
        this._action.onExpandedCustomerIndexChanged(expandedCustomerIndex);
      }
    }
  }

  @NonNull
  public CustomerModel boundCustomer() {
    return Objects.requireNonNull(this._boundCustomer);
  }

  public void setCardExpanded(boolean isExpanded) {
    Objects.requireNonNull(this._boundCustomer);

    this._card.setCardExpanded(isExpanded);
    // Only fill the view when it's shown on screen.
    if (isExpanded) this._card.setExpandedCardCustomer(this._boundCustomer);
  }
}
