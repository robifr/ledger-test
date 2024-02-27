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

package com.robifr.ledger.ui.filter_customer.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.chip.ChipGroup;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.ListableListSelectedItemBinding;
import com.robifr.ledger.databinding.ReusableChipBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.filter_customer.FilterCustomerFragment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FilterCustomerHeaderHolder extends RecyclerViewHolder<Optional> {
  @NonNull private final FilterCustomerFragment _fragment;
  @NonNull private final ListableListSelectedItemBinding _headerBinding;
  @NonNull private final ChipGroup _chipGroup;

  public FilterCustomerHeaderHolder(
      @NonNull FilterCustomerFragment fragment, @NonNull ListableListSelectedItemBinding binding) {
    super(binding.getRoot());
    this._fragment = Objects.requireNonNull(fragment);
    this._headerBinding = Objects.requireNonNull(binding);
    this._chipGroup = new ChipGroup(this._fragment.requireContext());

    final int chipSpacing = (int) this._fragment.getResources().getDimension(R.dimen.chip_spacing);
    final int chipGroupPaddingTop =
        (int) this._fragment.getResources().getDimension(R.dimen.cardlist_margin);

    this._chipGroup.setLayoutParams(
        new ChipGroup.LayoutParams(
            ChipGroup.LayoutParams.MATCH_PARENT, ChipGroup.LayoutParams.WRAP_CONTENT));
    this._chipGroup.setPadding(0, chipGroupPaddingTop, 0, 0);
    this._chipGroup.setSingleLine(false);
    this._chipGroup.setChipSpacingVertical(chipSpacing);
    this._chipGroup.setChipSpacingHorizontal(chipSpacing);
    this._headerBinding.selectedItemContainer.addView(this._chipGroup);
    this._headerBinding.selectedItemTitle.setText("Filtered customers");
    this._headerBinding.selectedItemDescription.setVisibility(View.GONE);
    this._headerBinding.allListTitle.setText("All customers");
    this._headerBinding.newButton.setVisibility(View.GONE);
  }

  @Override
  public void bind(@NonNull Optional ignore) {
    final List<CustomerModel> filteredCustomers =
        this._fragment.filterCustomerViewModel().filteredCustomers();
    final int filteredCustomersViewsVisibility =
        !filteredCustomers.isEmpty() ? View.VISIBLE : View.GONE;

    this._headerBinding.selectedItemTitle.setVisibility(filteredCustomersViewsVisibility);
    this._headerBinding.selectedItemContainer.setVisibility(filteredCustomersViewsVisibility);

    for (int i = 0; i < filteredCustomers.size(); i++) {
      final CustomerModel filteredCustomer = filteredCustomers.get(i);
      final ReusableChipBinding chipBinding =
          i >= this._chipGroup.getChildCount()
              ? ReusableChipBinding.inflate(
                  this._fragment.getLayoutInflater(), this._chipGroup, false)
              : ReusableChipBinding.bind(this._chipGroup.getChildAt(i));

      // Set chip text based on filtered customer name.
      chipBinding.chip.setText(filteredCustomer.name());

      if (chipBinding.chip.getParent() == null) this._chipGroup.addView(chipBinding.chip);
    }

    // Delete old unused chips those are previously used by old customer.
    if (this._chipGroup.getChildCount() > filteredCustomers.size()) {
      final int size = this._chipGroup.getChildCount() - filteredCustomers.size();
      this._chipGroup.removeViews(filteredCustomers.size(), size);
    }
  }
}
