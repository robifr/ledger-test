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

package com.robifr.ledger.ui.main.search;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.main.MainActivity;
import com.robifr.ledger.ui.searchable.SearchableFragment;
import com.robifr.ledger.ui.searchable.product.SearchProductList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SearchFragment extends SearchableFragment {
  //	private SearchCustomerList _searchCustomerList;
  private SearchProductList _searchProductList;

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    super.onViewCreated(view, savedInstance);

    final MainActivity activity = (MainActivity) this.getActivity();
    //		this._searchCustomerList = new SearchCustomerList(activity);
    //		this._searchProductList = new SearchProductList(activity);

    //		final SearchView searchView = view.findViewById(R.id.searchable_seachview);
    //		searchView.setQueryHint("Search customers & products");
  }

  @Override
  public boolean onQueryTextSubmit(@NonNull String text) {
    Objects.requireNonNull(text);

    final MainActivity activity = (MainActivity) this.getActivity();
    final CompletableFuture<List<CustomerModel>> customerFuture =
        CustomerRepository.instance(activity).search(text);
    final CompletableFuture<List<ProductModel>> productFuture =
        ProductRepository.instance(activity).search(text);

    CompletableFuture.allOf(customerFuture, productFuture)
        .thenAccept(
            v -> {
              final List<CustomerModel> customers = customerFuture.join();
              final List<ProductModel> products = productFuture.join();
              final int customerResultVisibility = customers.isEmpty() ? View.GONE : View.VISIBLE;
              final int productResultVisibility = products.isEmpty() ? View.GONE : View.VISIBLE;

              activity.runOnUiThread(
                  () -> {
                    //					this._searchCustomerList.setVerticallyOriented(false);
                    //					this._searchCustomerList.customerSearch().rootLayout()
                    //						.setVisibility(customerResultVisibility);
                    //					this._searchCustomerList.adapter().list().clear();
                    //					this._searchCustomerList.adapter().list().addAll(customers);
                    //					this._searchCustomerList.adapter().notifyDataSetChanged();

                    this._searchProductList.setVerticallyOriented(false);
                    this._searchProductList
                        .productSearch()
                        .rootLayout()
                        .setVisibility(productResultVisibility);
                    this._searchProductList.adapter().list().clear();
                    this._searchProductList.adapter().list().addAll(products);
                    this._searchProductList.adapter().notifyDataSetChanged();
                  });
            });

    return true;
  }

  //	public SearchCustomerList searchCustomerList() { return this._searchCustomerList; }

  public SearchProductList searchProductList() {
    return this._searchProductList;
  }

  public void setVisible(boolean isVisible) {
    super.setVisible(isVisible);

    final MainActivity activity = (MainActivity) this.getActivity();
    final int buttonVisibility = isVisible ? View.GONE : View.VISIBLE;

    //		activity.floatingActionButton().setVisibility(buttonVisibility);
    //		activity.bottomNavigation().setVisibility(buttonVisibility);
  }
}
