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

package com.robifr.ledger.ui.createqueue;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.selectcustomer.SelectCustomerFragment;
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
        final Bundle bundle = new Bundle();
        bundle.putParcelable(
            SelectCustomerFragment.Arguments.INITIAL_SELECTED_CUSTOMER_PARCELABLE.key(),
            this._fragment.createQueueViewModel().inputtedCustomer().getValue().orElse(null));

        Navigation.findNavController(this._fragment.fragmentBinding().getRoot())
            .navigate(R.id.selectCustomerFragment, bundle);
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
