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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.SearchableFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.searchproduct.recycler.SearchProductAdapter;
import com.robifr.ledger.ui.searchproduct.viewmodel.SearchProductViewModel;
import com.robifr.ledger.util.Compats;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class SearchProductFragment extends Fragment implements SearchView.OnQueryTextListener {
  public enum Arguments implements FragmentResultKey {
    INITIAL_QUERY_STRING,
    IS_SELECTION_ENABLED_BOOLEAN,
    INITIAL_SELECTED_PRODUCT_IDS_LONG_ARRAY
  }

  public enum Request implements FragmentResultKey {
    SELECT_PRODUCT
  }

  public enum Result implements FragmentResultKey {
    SELECTED_PRODUCT_ID_LONG
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
    this._searchProductViewModel = new ViewModelProvider(this).get(SearchProductViewModel.class);
    this._viewModelHandler = new SearchProductViewModelHandler(this, this._searchProductViewModel);

    this.requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this.getViewLifecycleOwner(), this._onBackPressed);
    this.requireActivity()
        .getWindow() // Match status bar color with toolbar.
        .setStatusBarColor(this.requireContext().getColor(R.color.surface));
    this._fragmentBinding.toolbar.setNavigationOnClickListener(
        v -> this._onBackPressed.handleOnBackPressed());
    this._fragmentBinding.searchView.setQueryHint(this.getString(R.string.searchProduct));
    this._fragmentBinding.searchView.setOnQueryTextListener(this);
    this._fragmentBinding.noResultsImage.image.setImageResource(R.drawable.image_noresultsfound);
    this._fragmentBinding.noResultsImage.title.setText(R.string.searchProduct_noResultsFound);
    this._fragmentBinding.noResultsImage.description.setText(
        R.string.searchProduct_noResultsFound_description);
    this._fragmentBinding.recyclerView.setLayoutManager(
        new LinearLayoutManager(this.requireContext()));
    this._fragmentBinding.recyclerView.setAdapter(this._adapter);
    this._fragmentBinding.recyclerView.setItemViewCacheSize(0);

    if (!this._searchProductViewModel.initialQuery().isEmpty()) {
      this._fragmentBinding.searchView.setQuery(this._searchProductViewModel.initialQuery(), true);
    } else {
      this._fragmentBinding.searchView.requestFocus();
      Compats.showKeyboard(this.requireContext(), this.fragmentBinding().searchView);
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
