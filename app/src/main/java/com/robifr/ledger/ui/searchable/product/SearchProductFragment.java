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

package com.robifr.ledger.ui.searchable.product;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.searchable.SearchableFragment;
import java.util.Objects;

public class SearchProductFragment extends SearchableFragment {
  private SearchProductList _searchList;

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    super.onViewCreated(view, savedInstance);

    final AppCompatActivity activity = (AppCompatActivity) this.getActivity();
    this._searchList = new SearchProductList(activity);

    //		final SearchView searchView = view.findViewById(R.id.searchable_seachview);
    //		searchView.setQueryHint("Search products");
  }

  @Override
  public boolean onQueryTextSubmit(@NonNull String text) {
    Objects.requireNonNull(text);

    final AppCompatActivity activity = (AppCompatActivity) this.getActivity();
    final ProductRepository productRepository = ProductRepository.instance(activity);

    productRepository
        .search(text)
        .thenAccept(
            products -> {
              final int resultVisibility = products.isEmpty() ? View.GONE : View.VISIBLE;

              activity.runOnUiThread(
                  () -> {
                    this._searchList.productSearch().rootLayout().setVisibility(resultVisibility);
                    this._searchList.adapter().list().clear();
                    this._searchList.adapter().list().addAll(products);
                    this._searchList.adapter().notifyDataSetChanged();
                  });
            });

    return true;
  }

  public SearchProductList searchList() {
    return this._searchList;
  }
}
