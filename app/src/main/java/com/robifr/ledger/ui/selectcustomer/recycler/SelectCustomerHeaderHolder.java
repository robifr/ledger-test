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

package com.robifr.ledger.ui.selectcustomer.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardWideBinding;
import com.robifr.ledger.databinding.ListableListSelectedItemBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.customer.CustomerCardNormalComponent;
import com.robifr.ledger.ui.selectcustomer.SelectCustomerFragment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SelectCustomerHeaderHolder extends RecyclerViewHolder<Optional<CustomerModel>>
    implements View.OnClickListener {
  @NonNull private final SelectCustomerFragment _fragment;
  @NonNull private final ListableListSelectedItemBinding _headerBinding;
  @NonNull private final CustomerCardWideBinding _selectedCardBinding;
  @NonNull private final CustomerCardNormalComponent _selectedNormalCard;

  public SelectCustomerHeaderHolder(
      @NonNull SelectCustomerFragment fragment, @NonNull ListableListSelectedItemBinding binding) {
    super(binding.getRoot());
    this._fragment = Objects.requireNonNull(fragment);
    this._headerBinding = Objects.requireNonNull(binding);
    this._selectedCardBinding =
        CustomerCardWideBinding.inflate(
            this._fragment.getLayoutInflater(), this._headerBinding.selectedItemContainer, false);
    this._selectedNormalCard =
        new CustomerCardNormalComponent(
            this._fragment.requireContext(), this._selectedCardBinding.normalCard);

    this._headerBinding.selectedItemTitle.setText(
        this._fragment.getString(R.string.text_selected_customer));
    this._headerBinding.selectedItemContainer.addView(this._selectedCardBinding.getRoot());
    this._headerBinding.allListTitle.setText(this._fragment.getString(R.string.text_all_customers));
    this._headerBinding.newButton.setOnClickListener(this);
    // Don't set to `View.GONE` as the position will be occupied by checkbox.
    this._selectedCardBinding.normalCard.menuButton.setVisibility(View.INVISIBLE);
  }

  @Override
  public void bind(@NonNull Optional<CustomerModel> selectedCustomer) {
    Objects.requireNonNull(selectedCustomer);

    if (!selectedCustomer.isPresent()) {
      this._selectedCardBinding.cardView.setChecked(false);
      this._headerBinding.selectedItemDescription.setVisibility(View.GONE);
      this._headerBinding.selectedItemTitle.setVisibility(View.GONE);
      this._headerBinding.selectedItemContainer.setVisibility(View.GONE);
      return;
    }

    final List<CustomerModel> customers =
        this._fragment.selectCustomerViewModel().customers().getValue();
    final CustomerModel selectedCustomerOnDb =
        customers != null && selectedCustomer.isPresent()
            ? customers.stream()
                .filter(
                    customer ->
                        customer.id() != null && customer.id().equals(selectedCustomer.get().id()))
                .findFirst()
                .orElse(null)
            : null;

    // The original customer on database was deleted.
    if (selectedCustomerOnDb == null) {
      this._headerBinding.selectedItemDescription.setText(
          this._fragment.getString(R.string.text_originally_selected_customer_was_deleted));
      this._headerBinding.selectedItemDescription.setVisibility(View.VISIBLE);

      // The original customer on database was edited.
    } else if (selectedCustomer.isPresent()
        && !selectedCustomer.get().equals(selectedCustomerOnDb)) {
      this._headerBinding.selectedItemDescription.setText(
          this._fragment.getString(R.string.text_originally_selected_customer_was_changed));
      this._headerBinding.selectedItemDescription.setVisibility(View.VISIBLE);

      // It's the same unchanged customer.
    } else {
      this._headerBinding.selectedItemDescription.setVisibility(View.GONE);
    }

    this._selectedCardBinding.cardView.setChecked(true);
    this._selectedNormalCard.setCustomer(selectedCustomer.orElse(null));
    this._headerBinding.selectedItemTitle.setVisibility(View.VISIBLE);
    this._headerBinding.selectedItemContainer.setVisibility(View.VISIBLE);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.newButton ->
          Navigation.findNavController(this._fragment.fragmentBinding().getRoot())
              .navigate(R.id.createCustomerFragment);
    }
  }
}
