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

package com.robifr.ledger.ui.queue.filter;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.QueueFilters;
import com.robifr.ledger.databinding.QueueDialogFilterBinding;
import com.robifr.ledger.ui.filtercustomer.FilterCustomerFragment;
import com.robifr.ledger.ui.queue.QueueFragment;
import java.util.List;
import java.util.Objects;

public class QueueFilterCustomer
    implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public QueueFilterCustomer(
      @NonNull QueueFragment fragment,
      @NonNull QueueDialogFilterBinding dialogBinding,
      @NonNull BottomSheetDialog dialog) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = Objects.requireNonNull(dialogBinding);
    this._dialog = Objects.requireNonNull(dialog);

    this._dialogBinding.filterCustomer.filterCustomerButton.setOnClickListener(this);
    this._dialogBinding.filterCustomer.showNullCustomer.setOnClickListener(
        v -> this._dialogBinding.filterCustomer.showNullCustomerCheckbox.performClick());
    this._dialogBinding.filterCustomer.showNullCustomerCheckbox.setOnCheckedChangeListener(this);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.filterCustomerButton -> {
        final List<Long> filteredCustomerIds =
            this._fragment.queueViewModel().filterView().inputtedCustomerIds().getValue();

        final Bundle bundle = new Bundle();
        bundle.putLongArray(
            FilterCustomerFragment.Arguments.INITIAL_FILTERED_CUSTOMER_IDS_LONG_ARRAY.key(),
            filteredCustomerIds.stream().mapToLong(Long::longValue).toArray());

        Navigation.findNavController(this._fragment.fragmentBinding().getRoot())
            .navigate(R.id.filterCustomerFragment, bundle);
        this._dialog.dismiss();
      }
    }
  }

  @Override
  public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean isChecked) {
    Objects.requireNonNull(compoundButton);

    switch (compoundButton.getId()) {
      case R.id.showNullCustomerCheckbox ->
          this._fragment.queueViewModel().filterView().onNullCustomerShownEnabled(isChecked);
    }
  }

  /**
   * @param isShown {@link QueueFilters#isNullCustomerShown()}
   */
  public void setNullCustomerShown(boolean isShown) {
    // Remove listener to prevent unintended updates to both view model
    // and the switch itself when manually set the switch.
    this._dialogBinding.filterCustomer.showNullCustomerCheckbox.setOnCheckedChangeListener(null);
    this._dialogBinding.filterCustomer.showNullCustomerCheckbox.setChecked(isShown);
    this._dialogBinding.filterCustomer.showNullCustomerCheckbox.setOnCheckedChangeListener(this);
  }
}
