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

package com.robifr.ledger.ui.filtercustomer.recycler;

import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.ListableListSelectedItemBinding;
import com.robifr.ledger.databinding.ReusableChipInputBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.filtercustomer.FilterCustomerAction;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class FilterCustomerHeaderHolder<T extends FilterCustomerAction>
    extends RecyclerViewHolder<Optional<Void>, T> implements View.OnClickListener {
  @NonNull private final ListableListSelectedItemBinding _headerBinding;
  @NonNull private final ChipGroup _chipGroup;

  public FilterCustomerHeaderHolder(
      @NonNull ListableListSelectedItemBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._headerBinding = Objects.requireNonNull(binding);
    this._chipGroup = new ChipGroup(this.itemView.getContext());

    final int chipSpacing =
        (int) this.itemView.getContext().getResources().getDimension(R.dimen.chip_spacing);
    final int chipGroupPaddingTop =
        (int) this.itemView.getContext().getResources().getDimension(R.dimen.cardlist_margin);

    this._chipGroup.setLayoutParams(
        new ChipGroup.LayoutParams(
            ChipGroup.LayoutParams.MATCH_PARENT, ChipGroup.LayoutParams.WRAP_CONTENT));
    this._chipGroup.setPadding(0, chipGroupPaddingTop, 0, 0);
    this._chipGroup.setSingleLine(false);
    this._chipGroup.setChipSpacingVertical(chipSpacing);
    this._chipGroup.setChipSpacingHorizontal(chipSpacing);
    this._headerBinding.selectedItemContainer.addView(this._chipGroup);
    this._headerBinding.selectedItemTitle.setText(
        this.itemView.getContext().getString(R.string.filterCustomer_filteredCustomers));
    this._headerBinding.selectedItemDescription.setVisibility(View.GONE);
    this._headerBinding.allListTitle.setText(
        this.itemView.getContext().getString(R.string.filterCustomer_allCustomers));
    this._headerBinding.newButton.setVisibility(View.GONE);
  }

  @Override
  public void bind(@NonNull Optional<Void> ignore) {
    final int filteredCustomersViewsVisibility =
        !this._action.filteredCustomers().isEmpty() ? View.VISIBLE : View.GONE;

    this._headerBinding.selectedItemTitle.setVisibility(filteredCustomersViewsVisibility);
    this._headerBinding.selectedItemContainer.setVisibility(filteredCustomersViewsVisibility);

    for (int i = 0; i < this._action.filteredCustomers().size(); i++) {
      final CustomerModel filteredCustomer = this._action.filteredCustomers().get(i);
      final ReusableChipInputBinding chipBinding =
          i >= this._chipGroup.getChildCount()
              ? ReusableChipInputBinding.inflate(
                  LayoutInflater.from(this.itemView.getContext()), this._chipGroup, false)
              : ReusableChipInputBinding.bind(this._chipGroup.getChildAt(i));

      // Set chip ID based on the customer index to make them easier to find.
      chipBinding.chip.setId(i);
      chipBinding.chip.setText(filteredCustomer.name());
      chipBinding.chip.setOnClickListener(this);

      if (chipBinding.chip.getParent() == null) this._chipGroup.addView(chipBinding.chip);
    }

    // Delete old unused chips those are previously used by old customer.
    if (this._chipGroup.getChildCount() > this._action.filteredCustomers().size()) {
      final int size = this._chipGroup.getChildCount() - this._action.filteredCustomers().size();
      this._chipGroup.removeViews(this._action.filteredCustomers().size(), size);
    }
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    if (view instanceof Chip) {
      final ArrayList<CustomerModel> filteredCustomers =
          new ArrayList<>(this._action.filteredCustomers());

      for (int i = filteredCustomers.size(); i-- > 0; ) {
        if (view.getId() == i) {
          // The ID is the chip index itself.
          this._chipGroup.removeViewAt(view.getId());
          filteredCustomers.remove(view.getId());
          break;
        }
      }

      this._action.onFilteredCustomersChanged(filteredCustomers);
    }
  }
}
