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
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ListableFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.selectproduct.recycler.SelectProductAdapter;
import com.robifr.ledger.ui.selectproduct.viewmodel.SelectProductViewModel;
import com.robifr.ledger.util.Compats;
import java.util.Objects;

public class SelectProductFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  public enum Arguments implements FragmentResultKey {
    INITIAL_SELECTED_PRODUCT;

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

    final NavBackStackEntry backStackEntry =
        Navigation.findNavController(this._fragmentBinding.getRoot()).getCurrentBackStackEntry();
    final ProductModel initialSelectedProduct =
        backStackEntry != null && backStackEntry.getArguments() != null
            ? Compats.parcelableOf(
                backStackEntry.getArguments(),
                Arguments.INITIAL_SELECTED_PRODUCT.key(),
                ProductModel.class)
            : null;

    this._adapter = new SelectProductAdapter(this);
    this._resultHandler = new SelectProductResultHandler(this);
    this._selectProductViewModel =
        new ViewModelProvider(
                this,
                new SelectProductViewModel.Factory(this.requireContext(), initialSelectedProduct))
            .get(SelectProductViewModel.class);
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
    this._fragmentBinding.recyclerView.setItemViewCacheSize(0);

    view.post(() -> this._fragmentBinding.recyclerView.setAdapter(this._adapter));

    this._selectProductViewModel.fetchAllProducts();
  }

  @Override
  public boolean onMenuItemClick(@NonNull MenuItem item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(this._fragmentBinding);

    return switch (item.getItemId()) {
      case R.id.search -> {
        Navigation.findNavController(this._fragmentBinding.getRoot())
            .navigate(R.id.searchProductFragment);
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
