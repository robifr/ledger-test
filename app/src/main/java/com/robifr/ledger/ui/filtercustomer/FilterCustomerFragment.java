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
import androidx.fragment.app.FragmentFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.ListableFragmentBinding;
import com.robifr.ledger.ui.BackStack;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.filtercustomer.recycler.FilterCustomerAdapter;
import com.robifr.ledger.ui.filtercustomer.viewmodel.FilterCustomerViewModel;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerFragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilterCustomerFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
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
  @NonNull private final List<Long> _initialFilteredCustomerIds;
  @Nullable private ListableFragmentBinding _fragmentBinding;
  @Nullable private FilterCustomerAdapter _adapter;
  @Nullable private FilterCustomerResultHandler _resultHandler;

  @Nullable private FilterCustomerViewModel _filterCustomerViewModel;
  @Nullable private FilterCustomerViewModelHandler _viewModelHandler;

  /** Default constructor when configuration changes. */
  public FilterCustomerFragment() {
    this(new ArrayList<>());
  }

  public FilterCustomerFragment(@NonNull List<Long> initialFilteredCustomerIds) {
    this._initialFilteredCustomerIds = Objects.requireNonNull(initialFilteredCustomerIds);
  }

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
    this._resultHandler = new FilterCustomerResultHandler(this);
    this._filterCustomerViewModel =
        new ViewModelProvider(this, new FilterCustomerViewModel.Factory(this.requireContext()))
            .get(FilterCustomerViewModel.class);
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

    final List<CustomerModel> customers = this._filterCustomerViewModel.fetchAllCustomers();
    final CustomerModel[] filteredCustomers =
        this._initialFilteredCustomerIds.stream()
            .flatMap(
                id ->
                    customers.stream()
                        .filter(customer -> customer.id() != null && customer.id().equals(id)))
            .toArray(CustomerModel[]::new);

    this._filterCustomerViewModel.onCustomersChanged(customers);
    this._filterCustomerViewModel.onAddFilteredCustomer(filteredCustomers);
  }

  @Override
  public boolean onMenuItemClick(@NonNull MenuItem item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(this._filterCustomerViewModel);

    return switch (item.getItemId()) {
      case R.id.search -> {
        final SearchCustomerFragment searchCustomerFragment =
            (SearchCustomerFragment)
                new SearchCustomerFragment.Factory(null)
                    .instantiate(
                        this.requireContext().getClassLoader(),
                        SearchCustomerFragment.class.getName());

        if (this.requireActivity() instanceof BackStack navigation
            && navigation.currentTabStackTag() != null) {
          navigation.pushFragmentStack(
              navigation.currentTabStackTag(),
              searchCustomerFragment,
              SearchCustomerFragment.class.toString());
        }

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
    if (this.requireActivity() instanceof BackStack navigation
        && navigation.currentTabStackTag() != null) {
      navigation.popFragmentStack(navigation.currentTabStackTag());
    }
  }

  public static class Factory extends FragmentFactory {
    @NonNull private final List<Long> _initialFilteredCustomerIds;

    public Factory(@NonNull List<Long> initialFilteredCustomerIds) {
      this._initialFilteredCustomerIds = Objects.requireNonNull(initialFilteredCustomerIds);
    }

    @Override
    @NonNull
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
      Objects.requireNonNull(classLoader);
      Objects.requireNonNull(className);

      return (className.equals(FilterCustomerFragment.class.getName()))
          ? new FilterCustomerFragment(this._initialFilteredCustomerIds)
          : super.instantiate(classLoader, className);
    }
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
