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

package com.robifr.ledger.ui.searchproduct;

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
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.SearchableFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.searchproduct.recycler.SearchProductAdapter;
import com.robifr.ledger.ui.searchproduct.viewmodel.SearchProductViewModel;
import com.robifr.ledger.util.Compats;
import java.util.Objects;

public class SearchProductFragment extends Fragment implements SearchView.OnQueryTextListener {
  public enum Arguments implements FragmentResultKey {
    INITIAL_QUERY;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Request implements FragmentResultKey {
    SELECT_PRODUCT;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Result implements FragmentResultKey {
    SELECTED_PRODUCT_ID;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  @NonNull private final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();
  @Nullable private SearchableFragmentBinding _fragmentBinding;
  @Nullable private SearchProductAdapter _adapter;
  @ColorInt private int _normalStatusBarColor;

  @Nullable private SearchProductViewModel _searchProductViewModel;
  @Nullable private SearchProductViewModelHandler _viewModelHandler;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding = SearchableFragmentBinding.inflate(inflater, container, false);
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);

    this._adapter = new SearchProductAdapter(this);
    this._normalStatusBarColor = this.requireActivity().getWindow().getStatusBarColor();
    this._searchProductViewModel =
        new ViewModelProvider(this, new SearchProductViewModel.Factory(this.requireContext()))
            .get(SearchProductViewModel.class);
    this._viewModelHandler = new SearchProductViewModelHandler(this, this._searchProductViewModel);

    this.requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this.getViewLifecycleOwner(), this._onBackPressed);
    this.requireActivity()
        .getWindow() // Match status bar color with toolbar.
        .setStatusBarColor(this.requireContext().getColor(R.color.surface));
    this._fragmentBinding.toolbar.setNavigationOnClickListener(
        v -> this._onBackPressed.handleOnBackPressed());
    this._fragmentBinding.seachView.setQueryHint(this.getString(R.string.text_search_products));
    this._fragmentBinding.seachView.setOnQueryTextListener(this);
    this._fragmentBinding.seachView.requestFocus();
    this._fragmentBinding.noResultsImage.image.setImageResource(R.drawable.image_noresultsfound);
    this._fragmentBinding.noResultsImage.title.setText(R.string.text_no_results_found);
    this._fragmentBinding.noResultsImage.description.setText(
        this.getString(R.string.text_cant_find_any_matching_products));
    this._fragmentBinding.recyclerView.setLayoutManager(
        new LinearLayoutManager(this.requireContext()));
    this._fragmentBinding.recyclerView.setAdapter(this._adapter);
    this._fragmentBinding.recyclerView.setItemViewCacheSize(0);

    final NavBackStackEntry backStackEntry =
        Navigation.findNavController(this._fragmentBinding.getRoot()).getCurrentBackStackEntry();

    if (backStackEntry != null && backStackEntry.getArguments() != null) {
      this._fragmentBinding.seachView.setQuery(
          backStackEntry.getArguments().getString(Arguments.INITIAL_QUERY.key()), true);
    } else {
      Compats.showKeyboard(this.requireContext(), this._fragmentBinding.seachView);
    }
  }

  @Override
  public boolean onQueryTextSubmit(@NonNull String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(@NonNull String newText) {
    Objects.requireNonNull(this._searchProductViewModel);

    this._searchProductViewModel.onSearch(newText);
    return true;
  }

  @NonNull
  public SearchableFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public SearchProductAdapter adapter() {
    return Objects.requireNonNull(this._adapter);
  }

  @NonNull
  public SearchProductViewModel searchProductViewModel() {
    return Objects.requireNonNull(this._searchProductViewModel);
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
      SearchProductFragment.this.searchProductViewModel().onProductSelected(null);
    }
  }
}
