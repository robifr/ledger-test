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

package com.robifr.ledger.ui.search;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.SearchableFragmentBinding;
import com.robifr.ledger.databinding.SearchableListHorizontalBinding;
import com.robifr.ledger.ui.search.viewmodel.SearchViewModel;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerFragment;
import com.robifr.ledger.ui.searchproduct.SearchProductFragment;
import com.robifr.ledger.util.Compats;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class SearchFragment extends Fragment
    implements View.OnClickListener, SearchView.OnQueryTextListener {
  @NonNull private final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();
  @Nullable private SearchableFragmentBinding _fragmentBinding;
  @Nullable private SearchableListHorizontalBinding _customerListBinding;
  @Nullable private SearchableListHorizontalBinding _productListBinding;

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

    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);
    Objects.requireNonNull(this._customerListBinding);
    Objects.requireNonNull(this._productListBinding);

    this._searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
    this._viewModelHandler = new SearchViewModelHandler(this, this._searchViewModel);

    this.requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this.getViewLifecycleOwner(), this._onBackPressed);
    this.requireActivity()
        .getWindow() // Match status bar color with toolbar.
        .setStatusBarColor(this.requireContext().getColor(R.color.surface));
    this._fragmentBinding.toolbar.setNavigationOnClickListener(
        v -> this._onBackPressed.handleOnBackPressed());
    this._fragmentBinding.searchView.setQueryHint(
        this.getString(R.string.text_search_customers_and_products));
    this._fragmentBinding.searchView.setOnQueryTextListener(this);
    this._fragmentBinding.noResultsImage.image.setImageResource(R.drawable.image_noresultsfound);
    this._fragmentBinding.noResultsImage.title.setText(R.string.text_no_results_found);
    this._fragmentBinding.noResultsImage.description.setText(
        this.getString(R.string.text_cant_find_any_matching_customers_nor_products));
    this._fragmentBinding.recyclerView.setVisibility(View.GONE);
    this._fragmentBinding.horizontalListContainer.addView(this._customerListBinding.getRoot());
    this._fragmentBinding.horizontalListContainer.addView(this._productListBinding.getRoot());
    this._customerListBinding.title.setText(R.string.text_customers);
    this._customerListBinding.viewMoreButton.setOnClickListener(this);
    this._customerListBinding.getRoot().setVisibility(View.GONE);
    this._productListBinding.title.setText(R.string.text_products);
    this._productListBinding.viewMoreButton.setOnClickListener(this);
    this._productListBinding.getRoot().setVisibility(View.GONE);

    if (this._searchViewModel.query().isEmpty()) {
      this._fragmentBinding.searchView.requestFocus();
      Compats.showKeyboard(this.requireContext(), this._fragmentBinding.searchView);
    }
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
              SearchCustomerFragment.Arguments.INITIAL_QUERY_STRING.key(),
              this._fragmentBinding.searchView.getQuery().toString());

          Navigation.findNavController(this._fragmentBinding.getRoot())
              .navigate(R.id.searchCustomerFragment, bundle);

        } else if (view == this._productListBinding.viewMoreButton) {
          final Bundle bundle = new Bundle();
          bundle.putString(
              SearchProductFragment.Arguments.INITIAL_QUERY_STRING.key(),
              this._fragmentBinding.searchView.getQuery().toString());

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

    final TypedValue backgroundColor = new TypedValue();
    this.requireContext()
        .getTheme()
        .resolveAttribute(android.R.attr.colorBackground, backgroundColor, true);

    this.requireActivity().getWindow().setStatusBarColor(backgroundColor.data);
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
