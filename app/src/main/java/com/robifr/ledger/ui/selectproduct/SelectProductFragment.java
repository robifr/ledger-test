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

package com.robifr.ledger.ui.selectproduct;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ListableFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.searchproduct.SearchProductFragment;
import com.robifr.ledger.ui.selectproduct.recycler.SelectProductAdapter;
import com.robifr.ledger.ui.selectproduct.viewmodel.SelectProductViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;
import java.util.Optional;

@AndroidEntryPoint
public class SelectProductFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  public enum Arguments implements FragmentResultKey {
    INITIAL_SELECTED_PRODUCT_PARCELABLE
  }

  public enum Request implements FragmentResultKey {
    SELECT_PRODUCT
  }

  public enum Result implements FragmentResultKey {
    SELECTED_PRODUCT_ID_LONG
  }

  @NonNull private final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();
  @Nullable private ListableFragmentBinding _fragmentBinding;
  @Nullable private SelectProductAdapter _adapter;
  @Nullable private SelectProductResultHandler _resultHandler;

  @Nullable private SelectProductViewModel _selectProductViewModel;
  @Nullable private SelectProductViewModelHandler _viewModelHandler;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding =
        ListableFragmentBinding.inflate(this.getLayoutInflater(), container, false);
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);

    this._adapter = new SelectProductAdapter(this);
    this._selectProductViewModel = new ViewModelProvider(this).get(SelectProductViewModel.class);
    this._viewModelHandler = new SelectProductViewModelHandler(this, this._selectProductViewModel);

    this.requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this.getViewLifecycleOwner(), this._onBackPressed);
    this._fragmentBinding.toolbar.getMenu().clear();
    this._fragmentBinding.toolbar.inflateMenu(R.menu.reusable_toolbar_select);
    this._fragmentBinding.toolbar.setTitle(this.getString(R.string.text_select_product));
    this._fragmentBinding.toolbar.setOnMenuItemClickListener(this);
    this._fragmentBinding.toolbar.setNavigationOnClickListener(
        v -> this._onBackPressed.handleOnBackPressed());
    this._fragmentBinding.horizontalToolbar.setVisibility(View.GONE);
    this._fragmentBinding.recyclerView.setLayoutManager(
        new LinearLayoutManager(this.requireContext()));
    this._fragmentBinding.recyclerView.setAdapter(this._adapter);
    this._fragmentBinding.recyclerView.setItemViewCacheSize(0);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Should be called after `SelectProductViewModelHandler` called. `onStart` is perfect place
    // for it. If there's a fragment inherit from this class, which mostly inherit their own
    // view model handler too. Then it's impossible to not call them both inside `onViewCreated`,
    // unless `super` call is omitted entirely.
    this._resultHandler = new SelectProductResultHandler(this);
  }

  @Override
  public boolean onMenuItemClick(@NonNull MenuItem item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(this._fragmentBinding);
    Objects.requireNonNull(this._selectProductViewModel);

    return switch (item.getItemId()) {
      case R.id.search -> {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(SearchProductFragment.Arguments.IS_SELECTION_ENABLED_BOOLEAN.key(), true);
        bundle.putLongArray(
            SearchProductFragment.Arguments.INITIAL_SELECTED_PRODUCT_IDS_LONG_ARRAY.key(),
            Optional.ofNullable(this._selectProductViewModel.initialSelectedProduct())
                .map(ProductModel::id)
                .map(id -> new long[] {id})
                .orElse(new long[] {}));

        Navigation.findNavController(this._fragmentBinding.getRoot())
            .navigate(R.id.searchProductFragment, bundle);
        yield true;
      }

      default -> false;
    };
  }

  @NonNull
  public ListableFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public SelectProductAdapter adapter() {
    return Objects.requireNonNull(this._adapter);
  }

  @NonNull
  public SelectProductViewModel selectProductViewModel() {
    return Objects.requireNonNull(this._selectProductViewModel);
  }

  public void finish() {
    Objects.requireNonNull(this._fragmentBinding);

    Navigation.findNavController(this._fragmentBinding.getRoot()).popBackStack();
  }

  private class OnBackPressedHandler extends OnBackPressedCallback {
    public OnBackPressedHandler() {
      super(true);
    }

    @Override
    public void handleOnBackPressed() {
      SelectProductFragment.this.selectProductViewModel().onProductSelected(null);
    }
  }
}
