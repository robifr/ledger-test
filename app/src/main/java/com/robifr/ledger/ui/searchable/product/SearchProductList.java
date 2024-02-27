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

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.robifr.ledger.R;
import com.robifr.ledger.ui.searchable.SearchableListComponent;
import com.robifr.ledger.ui.searchable.product.recycler.SearchProductAdapter;
import com.robifr.ledger.util.FindView;
import java.util.Objects;

public class SearchProductList {
  private final AppCompatActivity _activity;
  private final SearchProductAdapter _adapter;
  private final SearchableListComponent _productSearch;

  /** Whether customer search results displayed vertically or not. */
  private boolean _isVerticallyOriented = false;

  public SearchProductList(@NonNull AppCompatActivity activity) {
    this._activity = Objects.requireNonNull(activity);
    this._adapter = new SearchProductAdapter(this._activity);

    final View rootView = FindView.rootView(activity);
    final ConstraintLayout customerListLayout =
        (ConstraintLayout)
            this._activity
                .getLayoutInflater()
                .inflate(R.layout.searchable_list_layout, (ViewGroup) rootView, false);

    this._productSearch = new SearchableListComponent(this._activity, customerListLayout);

    //		final LinearLayout fragmentParentView = rootView.findViewById(R.id.searchable_linearlayout);
    //		fragmentParentView.addView(this._productSearch.rootLayout());

    this._productSearch.rootLayout().setVisibility(View.GONE);
    //		this._productSearch.titleView().setText("Customers");
    //		this._productSearch.seeMoreButton().setOnClickListener(button ->
    // this.setVerticallyOriented(true));
    //		this._productSearch.recyclerView()
    //			.setLayoutManager(new LinearLayoutManager(this._activity, LinearLayoutManager.HORIZONTAL,
    // false));
    //		this._productSearch.recyclerView().setAdapter(this._adapter);
  }

  public SearchProductAdapter adapter() {
    return this._adapter;
  }

  public SearchableListComponent productSearch() {
    return this._productSearch;
  }

  public boolean isVerticallyOriented() {
    return this._isVerticallyOriented;
  }

  /**
   * @see SearchProductList#_isVerticallyOriented
   */
  public void setVerticallyOriented(boolean isVerticallyOriented) {
    this._isVerticallyOriented = isVerticallyOriented;
    final int orientation =
        isVerticallyOriented ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL;
    final int titleAndButtonVisibility = isVerticallyOriented ? View.GONE : View.VISIBLE;
    //		final SearchProductAdapter.ViewType adapterViewType = isVerticallyOriented
    //			? SearchProductAdapter.ViewType.VERTICAL
    //			: SearchProductAdapter.ViewType.HORIZONTAL;

    //		this._productSearch.titleView().setVisibility(titleAndButtonVisibility);
    //		this._productSearch.seeMoreButton().setVisibility(titleAndButtonVisibility);
    //		this._productSearch.recyclerView()
    //			.setLayoutManager(new LinearLayoutManager(this._activity, orientation, false));
    //		this._productSearch.recyclerView().setAdapter(this._adapter);
    //		this._adapter.setCurrentViewType(adapterViewType);
    this._adapter.notifyDataSetChanged();
  }
}
