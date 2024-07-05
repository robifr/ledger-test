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
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.CustomerCardPackedBinding;
import com.robifr.ledger.databinding.ProductCardPackedBinding;
import com.robifr.ledger.ui.customer.CustomerCardPackedComponent;
import com.robifr.ledger.ui.product.ProductCardPackedComponent;
import com.robifr.ledger.ui.search.viewmodel.SearchViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onCustomers(@NonNull Optional<List<CustomerModel>> customers) {
    Objects.requireNonNull(customers);

    final int listVisibility =
        customers.isPresent() && !customers.get().isEmpty() ? View.VISIBLE : View.GONE;
    final int noResultsVisibility =
        // Only show illustration when both of customers and products are empty list.
        customers.isPresent()
                && customers.get().isEmpty()
                && this._viewModel.products().getValue().isPresent()
                && this._viewModel.products().getValue().get().isEmpty()
            ? View.VISIBLE
            : View.GONE;

    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);
    this._fragment.customerListBinding().getRoot().setVisibility(listVisibility);
    this._fragment.customerListBinding().listContainer.removeAllViews();

    for (CustomerModel customer : customers.orElse(List.of())) {
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

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onProducts(@NonNull Optional<List<ProductModel>> products) {
    Objects.requireNonNull(products);

    final int listVisibility =
        products.isPresent() && !products.get().isEmpty() ? View.VISIBLE : View.GONE;
    final int noResultsVisibility =
        // Only show illustration when both of customers and products are empty list.
        products.isPresent()
                && products.get().isEmpty()
                && this._viewModel.customers().getValue().isPresent()
                && this._viewModel.customers().getValue().get().isEmpty()
            ? View.VISIBLE
            : View.GONE;

    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);
    this._fragment.productListBinding().getRoot().setVisibility(listVisibility);
    this._fragment.productListBinding().listContainer.removeAllViews();

    for (ProductModel product : products.orElse(List.of())) {
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
