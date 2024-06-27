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

package com.robifr.ledger.ui.search;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.CustomerCardPackedBinding;
import com.robifr.ledger.databinding.ProductCardPackedBinding;
import com.robifr.ledger.ui.customer.CustomerCardPackedComponent;
import com.robifr.ledger.ui.product.ProductCardPackedComponent;
import com.robifr.ledger.ui.search.viewmodel.SearchViewModel;
import java.util.List;
import java.util.Objects;

public class SearchViewModelHandler {
  @NonNull private final SearchFragment _fragment;
  @NonNull private final SearchViewModel _viewModel;

  public SearchViewModelHandler(
      @NonNull SearchFragment fragment, @NonNull SearchViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel.customers().observe(this._fragment.getViewLifecycleOwner(), this::_onCustomers);
    this._viewModel.products().observe(this._fragment.getViewLifecycleOwner(), this::_onProducts);
  }

  private void _onCustomers(@Nullable List<CustomerModel> customers) {
    final int listVisibility = customers == null || customers.isEmpty() ? View.GONE : View.VISIBLE;
    final int noResultsVisibility =
        customers != null
                && customers.isEmpty()
                // Show illustration when both customers and products are empty.
                && (this._viewModel.products().getValue() == null
                    || this._viewModel.products().getValue().isEmpty())
            ? View.VISIBLE
            : View.GONE;

    this._fragment.customerListBinding().getRoot().setVisibility(listVisibility);
    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);

    if (customers != null) {
      this._fragment.customerListBinding().listContainer.removeAllViews();

      for (CustomerModel customer : customers) {
        final CustomerCardPackedBinding cardBinding =
            CustomerCardPackedBinding.inflate(
                this._fragment.getLayoutInflater(),
                this._fragment.customerListBinding().getRoot(),
                false);
        final CustomerCardPackedComponent cardComponent =
            new CustomerCardPackedComponent(this._fragment.requireContext(), cardBinding);

        cardComponent.setCustomer(customer);
        this._fragment.customerListBinding().listContainer.addView(cardBinding.getRoot());
      }
    }
  }

  private void _onProducts(@Nullable List<ProductModel> products) {
    final int listVisibility = products == null || products.isEmpty() ? View.GONE : View.VISIBLE;
    final int noResultsVisibility =
        products != null
                && products.isEmpty()
                // Show illustration when both customers and products are empty.
                && (this._viewModel.customers().getValue() == null
                    || this._viewModel.customers().getValue().isEmpty())
            ? View.VISIBLE
            : View.GONE;

    this._fragment.productListBinding().getRoot().setVisibility(listVisibility);
    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);

    if (products != null) {
      this._fragment.productListBinding().listContainer.removeAllViews();

      for (ProductModel product : products) {
        final ProductCardPackedBinding cardBinding =
            ProductCardPackedBinding.inflate(
                this._fragment.getLayoutInflater(),
                this._fragment.customerListBinding().getRoot(),
                false);
        final ProductCardPackedComponent cardComponent =
            new ProductCardPackedComponent(this._fragment.requireContext(), cardBinding);

        cardComponent.setProduct(product);
        this._fragment.productListBinding().listContainer.addView(cardBinding.getRoot());
      }
    }
  }
}
