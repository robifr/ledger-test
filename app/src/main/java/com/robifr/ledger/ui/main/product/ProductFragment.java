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

package com.robifr.ledger.ui.main.product;

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
import com.robifr.ledger.data.ProductFilters;
import com.robifr.ledger.data.ProductSortMethod;
import com.robifr.ledger.databinding.ListableFragmentBinding;
import com.robifr.ledger.ui.BackStack;
import com.robifr.ledger.ui.main.product.filter.ProductFilter;
import com.robifr.ledger.ui.main.product.recycler.ProductAdapter;
import com.robifr.ledger.ui.main.product.viewmodel.ProductViewModel;
import com.robifr.ledger.ui.main.search.SearchFragment;
import java.util.Objects;

public class ProductFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  @Nullable private ListableFragmentBinding _fragmentBinding;
  @Nullable private ProductSort _sort;
  @Nullable private ProductFilter _filter;
  @Nullable private ProductAdapter _adapter;

  @Nullable private ProductViewModel _productViewModel;
  @Nullable private ProductViewModelHandler _viewModelHandler;

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

    this._sort = new ProductSort(this);
    this._filter = new ProductFilter(this);
    this._adapter = new ProductAdapter(this);
    this._productViewModel =
        new ViewModelProvider(
                this.requireActivity(), new ProductViewModel.Factory(this.requireContext()))
            .get(ProductViewModel.class);
    this._viewModelHandler = new ProductViewModelHandler(this, this._productViewModel);

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

    this._productViewModel.onProductsChanged(this._productViewModel.fetchAllProducts());
    this._productViewModel.onSortMethodChanged(
        new ProductSortMethod(ProductSortMethod.SortBy.NAME, true));
    this._productViewModel.filterView().onFiltersChanged(ProductFilters.toBuilder().build());
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
  public ProductSort sort() {
    return Objects.requireNonNull(this._sort);
  }

  @NonNull
  public ProductFilter filter() {
    return Objects.requireNonNull(this._filter);
  }

  @NonNull
  public ProductAdapter adapter() {
    return Objects.requireNonNull(this._adapter);
  }

  @NonNull
  public ProductViewModel productViewModel() {
    return Objects.requireNonNull(this._productViewModel);
  }

  public static class Factory extends FragmentFactory {
    @Override
    @NonNull
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
      Objects.requireNonNull(classLoader);
      Objects.requireNonNull(className);

      return (className.equals(ProductFragment.class.getName()))
          ? new ProductFragment()
          : super.instantiate(classLoader, className);
    }
  }
}
