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

package com.robifr.ledger.ui.main.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.robifr.ledger.R;
import com.robifr.ledger.data.CustomerFilters;
import com.robifr.ledger.data.CustomerSortMethod;
import com.robifr.ledger.databinding.ListableFragmentBinding;
import com.robifr.ledger.ui.BackStack;
import com.robifr.ledger.ui.main.customer.filter.CustomerFilter;
import com.robifr.ledger.ui.main.customer.recycler.CustomerAdapter;
import com.robifr.ledger.ui.main.customer.viewmodel.CustomerViewModel;
import com.robifr.ledger.ui.main.search.SearchFragment;
import java.util.Objects;

public class CustomerFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  @Nullable private ListableFragmentBinding _fragmentBinding;
  @Nullable private CustomerSort _sort;
  @Nullable private CustomerFilter _filter;
  @Nullable private CustomerAdapter _adapter;

  @Nullable private CustomerViewModel _customerViewModel;
  @Nullable private CustomerViewModelHandler _viewModelHandler;

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

    this._sort = new CustomerSort(this);
    this._filter = new CustomerFilter(this);
    this._adapter = new CustomerAdapter(this);
    this._customerViewModel =
        new ViewModelProvider(
                this.requireActivity(), new CustomerViewModel.Factory(this.requireContext()))
            .get(CustomerViewModel.class);
    this._viewModelHandler = new CustomerViewModelHandler(this, this._customerViewModel);

    this._fragmentBinding.toolbar.getMenu().clear();
    this._fragmentBinding.toolbar.inflateMenu(R.menu.reusable_toolbar_main);
    this._fragmentBinding.toolbar.setNavigationIcon(null);
    this._fragmentBinding.toolbar.setTitle(this.getString(R.string.app_name));
    this._fragmentBinding.toolbar.setOnMenuItemClickListener(this);
    this._fragmentBinding.sortByChip.setOnClickListener(chip -> this._sort.openDialog());
    this._fragmentBinding.filtersChip.setOnClickListener(chip -> this._filter.openDialog());
    this._fragmentBinding.recyclerView.setLayoutManager(
        new LinearLayoutManager(this.requireContext()));
    this._fragmentBinding.recyclerView.setAdapter(this._adapter);
    this._fragmentBinding.recyclerView.setItemViewCacheSize(0);

    this._customerViewModel.onCustomersChanged(this._customerViewModel.fetchAllCustomers());
    this._customerViewModel.onSortMethodChanged(
        new CustomerSortMethod(CustomerSortMethod.SortBy.NAME, true));
    this._customerViewModel.filterView().onFiltersChanged(CustomerFilters.toBuilder().build());
  }

  @Override
  public boolean onMenuItemClick(@NonNull MenuItem item) {
    Objects.requireNonNull(item);

    return switch (item.getItemId()) {
      case R.id.search -> {
        final SearchFragment searchFragment =
            (SearchFragment)
                new SearchFragment.Factory()
                    .instantiate(
                        this.requireContext().getClassLoader(), SearchFragment.class.getName());

        if (this.requireActivity() instanceof BackStack navigation
            && navigation.currentTabStackTag() != null) {
          navigation.pushFragmentStack(
              navigation.currentTabStackTag(), searchFragment, SearchFragment.class.toString());
        }

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
  public CustomerSort sort() {
    return Objects.requireNonNull(this._sort);
  }

  @NonNull
  public CustomerFilter filter() {
    return Objects.requireNonNull(this._filter);
  }

  @NonNull
  public CustomerAdapter adapter() {
    return Objects.requireNonNull(this._adapter);
  }

  @NonNull
  public CustomerViewModel customerViewModel() {
    return Objects.requireNonNull(this._customerViewModel);
  }

  public static class Factory extends FragmentFactory {
    @Override
    @NonNull
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
      Objects.requireNonNull(classLoader);
      Objects.requireNonNull(className);

      return (className.equals(CustomerFragment.class.getName()))
          ? new CustomerFragment()
          : super.instantiate(classLoader, className);
    }
  }
}
