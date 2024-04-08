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

package com.robifr.ledger.ui.filtercustomer;

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
import com.robifr.ledger.databinding.ListableFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.filtercustomer.recycler.FilterCustomerAdapter;
import com.robifr.ledger.ui.filtercustomer.viewmodel.FilterCustomerViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class FilterCustomerFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  public enum Arguments implements FragmentResultKey {
    INITIAL_FILTERED_CUSTOMER_IDS;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Request implements FragmentResultKey {
    FILTER_CUSTOMER;

    @NonNull
    @Override
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Result implements FragmentResultKey {
    FILTERED_CUSTOMER_IDS;

    @NonNull
    @Override
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  @NonNull private final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();
  @Nullable private ListableFragmentBinding _fragmentBinding;
  @Nullable private FilterCustomerAdapter _adapter;
  @Nullable private FilterCustomerResultHandler _resultHandler;

  @Nullable private FilterCustomerViewModel _filterCustomerViewModel;
  @Nullable private FilterCustomerViewModelHandler _viewModelHandler;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding = ListableFragmentBinding.inflate(inflater, container, false);
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);

    this._adapter = new FilterCustomerAdapter(this);
    this._filterCustomerViewModel = new ViewModelProvider(this).get(FilterCustomerViewModel.class);
    this._viewModelHandler =
        new FilterCustomerViewModelHandler(this, this._filterCustomerViewModel);

    this.requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this.getViewLifecycleOwner(), this._onBackPressed);
    this._fragmentBinding.toolbar.getMenu().clear();
    this._fragmentBinding.toolbar.inflateMenu(R.menu.reusable_toolbar_select_multiple);
    this._fragmentBinding.toolbar.setTitle(this.getString(R.string.text_filter_customers));
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
    // Should be called after `FilterCustomerViewModelHandler` called. `onStart` is perfect place
    // for it. If there's a fragment inherit from this class, which mostly inherit their own
    // view model handler too. Then it's impossible to not call them both inside `onViewCreated`,
    // unless `super` call is omitted entirely.
    this._resultHandler = new FilterCustomerResultHandler(this);
  }

  @Override
  public boolean onMenuItemClick(@NonNull MenuItem item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(this._fragmentBinding);
    Objects.requireNonNull(this._filterCustomerViewModel);

    return switch (item.getItemId()) {
      case R.id.search -> {
        Navigation.findNavController(this._fragmentBinding.getRoot())
            .navigate(R.id.searchCustomerFragment);
        yield true;
      }

      case R.id.save -> {
        this._filterCustomerViewModel.onSave();
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
  public FilterCustomerAdapter adapter() {
    return Objects.requireNonNull(this._adapter);
  }

  @NonNull
  public FilterCustomerViewModel filterCustomerViewModel() {
    return Objects.requireNonNull(this._filterCustomerViewModel);
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
      FilterCustomerFragment.this.filterCustomerViewModel().onSave();
    }
  }
}
