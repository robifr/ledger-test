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

package com.robifr.ledger.ui.customer.recycler;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.CustomerCardDialogMenuBinding;
import com.robifr.ledger.ui.editcustomer.EditCustomerFragment;
import java.util.Objects;

public class CustomerListMenu implements View.OnClickListener {
  @NonNull private final CustomerListHolder<?> _holder;
  @NonNull private final CustomerCardDialogMenuBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public CustomerListMenu(@NonNull CustomerListHolder<?> holder) {
    this._holder = Objects.requireNonNull(holder);
    this._dialogBinding =
        CustomerCardDialogMenuBinding.inflate(
            LayoutInflater.from(this._holder.itemView.getContext()));
    this._dialog =
        new BottomSheetDialog(this._holder.itemView.getContext(), R.style.BottomSheetDialog);

    this._dialog.setContentView(this._dialogBinding.getRoot());
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.editButton -> {
        final Long customerId = this._holder.boundCustomer().id();
        if (customerId == null) return;

        final Bundle bundle = new Bundle();
        bundle.putLong(
            EditCustomerFragment.Arguments.INITIAL_CUSTOMER_ID_TO_EDIT_LONG.key(), customerId);

        Navigation.findNavController(this._holder.itemView)
            .navigate(R.id.editCustomerFragment, bundle);
        this._dialog.dismiss();
      }

      case R.id.deleteButton -> {
        this._holder.action().onDeleteCustomer(this._holder.boundCustomer());
        this._dialog.dismiss();
      }
    }
  }

  public void openDialog() {
    this._dialogBinding.editButton.setOnClickListener(this);
    this._dialogBinding.deleteButton.setOnClickListener(this);
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }
}
