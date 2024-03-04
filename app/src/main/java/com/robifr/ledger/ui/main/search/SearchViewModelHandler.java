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

package com.robifr.ledger.ui.main.search;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.CustomerCardPackedBinding;
import com.robifr.ledger.databinding.ProductCardPackedBinding;
import com.robifr.ledger.ui.main.customer.CustomerCardPackedComponent;
import com.robifr.ledger.ui.main.product.ProductCardPackedComponent;
import com.robifr.ledger.ui.main.search.viewmodel.SearchViewModel;
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
    final int horizontalListVisibility =
        customers == null || customers.size() == 0 ? View.GONE : View.VISIBLE;
    final int noResultsVisibility =
        customers != null
                && customers.size() == 0
                && (this._viewModel.products().getValue() == null
                    || this._viewModel.products().getValue().isEmpty())
            ? View.VISIBLE
            : View.GONE;

    this._fragment.customerListBinding().getRoot().setVisibility(horizontalListVisibility);
    this._fragment.fragmentBinding().horizontalListContainer.setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);

    if (customers != null) {
      // TODO: Can be improved with object pool.
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
    final int horizontalListVisibility =
        products == null || products.size() == 0 ? View.GONE : View.VISIBLE;
    final int noResultsVisibility =
        products != null
                && products.size() == 0
                // Show illustration when both customers and products are empty.
                && (this._viewModel.customers().getValue() == null
                    || this._viewModel.customers().getValue().isEmpty())
            ? View.VISIBLE
            : View.GONE;

    this._fragment.productListBinding().getRoot().setVisibility(horizontalListVisibility);
    this._fragment.fragmentBinding().horizontalListContainer.setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);

    if (products != null) {
      // TODO: Can be improved with object pool.
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
