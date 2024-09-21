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

package com.robifr.ledger.ui.searchcustomer.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardWideBinding;
import com.robifr.ledger.ui.customer.CustomerCardAction;
import com.robifr.ledger.ui.customer.CustomerListAction;
import com.robifr.ledger.ui.customer.recycler.CustomerListHolder;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerCardAction;
import java.util.Objects;

public class SearchCustomerListHolder<
        T extends CustomerListAction & CustomerCardAction & SearchCustomerCardAction>
    extends CustomerListHolder<T> implements View.OnClickListener {
  public SearchCustomerListHolder(@NonNull CustomerCardWideBinding binding, @NonNull T action) {
    super(binding, action);

    if (this._action.isSelectionEnabled()) {
      this._cardBinding.normalCard.menuButton.setVisibility(View.GONE);
    }
  }

  @Override
  public void bind(@NonNull CustomerModel customer) {
    this._boundCustomer = Objects.requireNonNull(customer);

    if (this._action.isSelectionEnabled()) {
      this._card.setNormalCardCustomer(customer);
      this.setCardExpanded(false);
    } else {
      super.bind(customer);
    }
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    if (this._action.isSelectionEnabled()) {
      switch (view.getId()) {
        case R.id.cardView -> this._action.onCustomerSelected(this._boundCustomer);
      }
    } else {
      super.onClick(view);
    }
  }
}
