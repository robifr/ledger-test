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

package com.robifr.ledger.ui.main.queue.filter;

import android.view.View;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.data.QueueFilters;
import com.robifr.ledger.databinding.QueueDialogFilterBinding;
import com.robifr.ledger.ui.BackStack;
import com.robifr.ledger.ui.filter_customer.FilterCustomerFragment;
import com.robifr.ledger.ui.main.queue.QueueFragment;
import java.util.ArrayList;
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
    this._dialogBinding.filterCustomer.showNullCustomerSwitch.setOnCheckedChangeListener(this);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.filterCustomerButton -> {
        final List<Long> filteredCustomerIds =
            Objects.requireNonNullElse(
                this._fragment.queueViewModel().filterView().inputtedCustomerIds().getValue(),
                new ArrayList<>());
        final FilterCustomerFragment filterCustomerFragment =
            (FilterCustomerFragment)
                new FilterCustomerFragment.Factory(filteredCustomerIds)
                    .instantiate(
                        this._fragment.requireContext().getClassLoader(),
                        FilterCustomerFragment.class.getName());

        if (this._fragment.requireActivity() instanceof BackStack navigation
            && navigation.currentTabStackTag() != null) {
          navigation.pushFragmentStack(
              navigation.currentTabStackTag(),
              filterCustomerFragment,
              FilterCustomerFragment.class.toString());
          this._dialog.hide();
        }
      }
    }
  }

  @Override
  public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean isChecked) {
    Objects.requireNonNull(compoundButton);

    switch (compoundButton.getId()) {
      case R.id.showNullCustomerSwitch ->
          this._fragment.queueViewModel().filterView().onNullCustomerShownEnabled(isChecked);
    }
  }

  /**
   * @param isShown {@link QueueFilters#isNullCustomerShown()}
   */
  public void setNullCustomerShown(boolean isShown) {
    // Remove listener to prevent unintended updates to both view model
    // and the switch itself when manually set the switch.
    this._dialogBinding.filterCustomer.showNullCustomerSwitch.setOnCheckedChangeListener(null);
    this._dialogBinding.filterCustomer.showNullCustomerSwitch.setChecked(isShown);
    this._dialogBinding.filterCustomer.showNullCustomerSwitch.setOnCheckedChangeListener(this);
  }
}
