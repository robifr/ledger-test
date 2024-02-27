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

package com.robifr.ledger.ui.create_queue;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.BackStack;
import com.robifr.ledger.ui.select_customer.SelectCustomerFragment;
import java.util.Objects;

public class CreateQueueCustomer implements View.OnClickListener {
  @NonNull private final CreateQueueFragment _fragment;

  public CreateQueueCustomer(@NonNull CreateQueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._fragment.fragmentBinding().customerLayout.setEndIconVisible(false);
    this._fragment
        .fragmentBinding()
        .customerLayout
        .setEndIconOnClickListener(
            view -> this._fragment.createQueueViewModel().onCustomerChanged(null));
    this._fragment.fragmentBinding().customer.setOnClickListener(this);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.customer -> {
        final CustomerModel customer =
            this._fragment.createQueueViewModel().inputtedCustomer().getValue();
        final SelectCustomerFragment selectCustomerFragment =
            (SelectCustomerFragment)
                new SelectCustomerFragment.Factory(customer)
                    .instantiate(
                        this._fragment.requireContext().getClassLoader(),
                        SelectCustomerFragment.class.getName());

        if (this._fragment.requireActivity() instanceof BackStack navigation
            && navigation.currentTabStackTag() != null) {
          navigation.pushFragmentStack(
              navigation.currentTabStackTag(),
              selectCustomerFragment,
              SelectCustomerFragment.class.toString());
        }
      }
    }
  }

  public void setInputtedCustomer(@Nullable CustomerModel customer) {
    final String name = customer != null ? customer.name() : null;
    final boolean isClearIconVisible = customer != null;

    this._fragment.fragmentBinding().customer.setText(name);
    this._fragment.fragmentBinding().customerLayout.setEndIconVisible(isClearIconVisible);
  }
}
