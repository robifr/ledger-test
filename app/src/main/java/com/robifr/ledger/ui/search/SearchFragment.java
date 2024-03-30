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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.SearchableFragmentBinding;
import com.robifr.ledger.databinding.SearchableListHorizontalBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.search.viewmodel.SearchViewModel;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerFragment;
import com.robifr.ledger.ui.searchproduct.SearchProductFragment;
import com.robifr.ledger.util.Compats;
import java.util.Objects;

public class SearchFragment extends Fragment
    implements View.OnClickListener, SearchView.OnQueryTextListener {
  public enum Request implements FragmentResultKey {
    SELECT_CUSTOMER;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Result implements FragmentResultKey {
    SELECTED_CUSTOMER_ID;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  @NonNull private final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();
  @Nullable private SearchableFragmentBinding _fragmentBinding;
  @Nullable private SearchableListHorizontalBinding _customerListBinding;
  @Nullable private SearchableListHorizontalBinding _productListBinding;
  @ColorInt private int _normalStatusBarColor;

  @Nullable private SearchViewModel _searchViewModel;
  @Nullable private SearchViewModelHandler _viewModelHandler;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding = SearchableFragmentBinding.inflate(inflater, container, false);
    this._customerListBinding = SearchableListHorizontalBinding.inflate(inflater, container, false);
    this._productListBinding = SearchableListHorizontalBinding.inflate(inflater, container, false);

    this._fragmentBinding.getRoot().addView(this._customerListBinding.getRoot());
    this._fragmentBinding.getRoot().addView(this._productListBinding.getRoot());
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);
    Objects.requireNonNull(this._customerListBinding);
    Objects.requireNonNull(this._productListBinding);

    this._normalStatusBarColor = this.requireActivity().getWindow().getStatusBarColor();
    this._searchViewModel =
        new ViewModelProvider(this, new SearchViewModel.Factory(this.requireContext()))
            .get(SearchViewModel.class);
    this._viewModelHandler = new SearchViewModelHandler(this, this._searchViewModel);

    this.requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this.getViewLifecycleOwner(), this._onBackPressed);
    this.requireActivity()
        .getWindow() // Match status bar color with toolbar.
        .setStatusBarColor(this.requireContext().getColor(R.color.surface));
    this._fragmentBinding.toolbar.setNavigationOnClickListener(
        v -> this._onBackPressed.handleOnBackPressed());
    this._fragmentBinding.seachView.setQueryHint(
        this.getString(R.string.text_search_customers_and_products));
    this._fragmentBinding.seachView.setOnQueryTextListener(this);
    this._fragmentBinding.seachView.requestFocus();
    this._fragmentBinding.noResultsImage.image.setImageResource(R.drawable.image_noresultsfound);
    this._fragmentBinding.noResultsImage.title.setText(R.string.text_no_results_found);
    this._fragmentBinding.noResultsImage.description.setText(
        this.getString(R.string.text_cant_find_any_matching_customers_nor_products));
    this._fragmentBinding.recyclerView.setVisibility(View.GONE);
    this._customerListBinding.title.setText(R.string.text_customers);
    this._customerListBinding.viewMoreButton.setOnClickListener(this);
    this._customerListBinding.getRoot().setVisibility(View.GONE);
    this._productListBinding.title.setText(R.string.text_products);
    this._productListBinding.viewMoreButton.setOnClickListener(this);
    this._productListBinding.getRoot().setVisibility(View.GONE);

    Compats.showKeyboard(this.requireContext(), this._fragmentBinding.seachView);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);
    Objects.requireNonNull(this._customerListBinding);
    Objects.requireNonNull(this._productListBinding);

    switch (view.getId()) {
      case R.id.viewMoreButton -> {
        if (view == this._customerListBinding.viewMoreButton) {
          final Bundle bundle = new Bundle();
          bundle.putString(
              SearchCustomerFragment.Arguments.INITIAL_QUERY.key(),
              this._fragmentBinding.seachView.getQuery().toString());

          Navigation.findNavController(this._fragmentBinding.getRoot())
              .navigate(R.id.searchCustomerFragment, bundle);

        } else if (view == this._productListBinding.viewMoreButton) {
          final Bundle bundle = new Bundle();
          bundle.putString(
              SearchProductFragment.Arguments.INITIAL_QUERY.key(),
              this._fragmentBinding.seachView.getQuery().toString());

          Navigation.findNavController(this._fragmentBinding.getRoot())
              .navigate(R.id.searchProductFragment, bundle);
        }
      }
    }
  }

  @Override
  public boolean onQueryTextSubmit(@NonNull String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(@NonNull String newText) {
    Objects.requireNonNull(this._searchViewModel);

    this._searchViewModel.onSearch(newText);
    return true;
  }

  @NonNull
  public SearchableFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public SearchableListHorizontalBinding customerListBinding() {
    return Objects.requireNonNull(this._customerListBinding);
  }

  @NonNull
  public SearchableListHorizontalBinding productListBinding() {
    return Objects.requireNonNull(this._productListBinding);
  }

  @NonNull
  public SearchViewModel searchViewModel() {
    return Objects.requireNonNull(this._searchViewModel);
  }

  public void finish() {
    Objects.requireNonNull(this._fragmentBinding);

    this.requireActivity().getWindow().setStatusBarColor(this._normalStatusBarColor);
    Compats.hideKeyboard(this.requireContext(), this.requireView().findFocus());
    Navigation.findNavController(this._fragmentBinding.getRoot()).popBackStack();
  }

  private class OnBackPressedHandler extends OnBackPressedCallback {
    public OnBackPressedHandler() {
      super(true);
    }

    @Override
    public void handleOnBackPressed() {
      SearchFragment.this.finish();
    }
  }
}
