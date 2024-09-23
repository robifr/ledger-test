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

package com.robifr.ledger.ui.customer.filter;

import android.content.DialogInterface;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.CustomerDialogFilterBinding;
import com.robifr.ledger.ui.customer.CustomerFragment;
import java.util.Objects;

public class CustomerFilter implements DialogInterface.OnDismissListener {
  @NonNull private final CustomerFragment _fragment;
  @NonNull private final CustomerDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;
  @NonNull private final CustomerFilterBalance _filterBalance;
  @NonNull private final CustomerFilterDebt _filterDebt;

  public CustomerFilter(@NonNull CustomerFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = CustomerDialogFilterBinding.inflate(this._fragment.getLayoutInflater());

    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);
    this._dialog.setContentView(this._dialogBinding.getRoot());

    this._filterBalance =
        new CustomerFilterBalance(this._fragment, this._dialogBinding, this._dialog);
    this._filterDebt = new CustomerFilterDebt(this._fragment, this._dialogBinding, this._dialog);

    this._dialog.setOnDismissListener(this);
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    Objects.requireNonNull(dialog);

    this._fragment
        .customerViewModel()
        .selectAllCustomers()
        .observe(
            this._fragment,
            customers ->
                this._fragment
                    .customerViewModel()
                    .filterView()
                    .onFiltersChanged(
                        this._fragment.customerViewModel().filterView().inputtedFilters(),
                        customers));

    if (this._dialog.getCurrentFocus() != null) this._dialog.getCurrentFocus().clearFocus();
  }

  @NonNull
  public CustomerFilterBalance filterBalance() {
    return this._filterBalance;
  }

  @NonNull
  public CustomerFilterDebt filterDebt() {
    return this._filterDebt;
  }

  public void openDialog() {
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }
}
