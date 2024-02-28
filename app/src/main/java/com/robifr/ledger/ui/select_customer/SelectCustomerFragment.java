/**
 * Copyright (c) 2022-present Robi
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

package com.robifr.ledger.ui.select_customer;

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
import com.robifr.ledger.ui.search_customer.SearchCustomerFragment;
import com.robifr.ledger.ui.select_customer.recycler.SelectCustomerAdapter;
import com.robifr.ledger.ui.select_customer.view_model.SelectCustomerViewModel;
import java.util.Objects;

public class SelectCustomerFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
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
  @Nullable private final CustomerModel _initialSelectedCustomer;
  @Nullable private ListableFragmentBinding _fragmentBinding;
  @Nullable private SelectCustomerAdapter _adapter;
  @Nullable private SelectCustomerResultHandler _resultHandler;

  @Nullable private SelectCustomerViewModel _selectCustomerViewModel;
  @Nullable private SelectCustomerViewModelHandler _viewModelHandler;

  /** Default constructor when configuration changes. */
  public SelectCustomerFragment() {
    this(null);
  }

  protected SelectCustomerFragment(@Nullable CustomerModel initialSelectedCustomer) {
    this._initialSelectedCustomer = initialSelectedCustomer;
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

    this._adapter = new SelectCustomerAdapter(this);
    this._resultHandler = new SelectCustomerResultHandler(this);
    this._selectCustomerViewModel =
        new ViewModelProvider(
                this,
                new SelectCustomerViewModel.Factory(
                    this.requireContext(), this._initialSelectedCustomer))
            .get(SelectCustomerViewModel.class);
    this._viewModelHandler =
        new SelectCustomerViewModelHandler(this, this._selectCustomerViewModel);

    this.requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this.getViewLifecycleOwner(), this._onBackPressed);
    this._fragmentBinding.toolbar.getMenu().clear();
    this._fragmentBinding.toolbar.inflateMenu(R.menu.reusable_toolbar_select);
    this._fragmentBinding.toolbar.setTitle(this.getString(R.string.text_select_customer));
    this._fragmentBinding.toolbar.setOnMenuItemClickListener(this);
    this._fragmentBinding.toolbar.setNavigationOnClickListener(
        v -> this._onBackPressed.handleOnBackPressed());
    this._fragmentBinding.horizontalToolbar.setVisibility(View.GONE);
    this._fragmentBinding.recyclerView.setLayoutManager(
        new LinearLayoutManager(this.requireContext()));
    this._fragmentBinding.recyclerView.setAdapter(this._adapter);
    this._fragmentBinding.recyclerView.setItemViewCacheSize(0);

    this._selectCustomerViewModel.onCustomersChanged(
        this._selectCustomerViewModel.fetchAllCustomers());
  }

  @Override
  public boolean onMenuItemClick(@NonNull MenuItem item) {
    Objects.requireNonNull(item);

    return switch (item.getItemId()) {
      case R.id.search -> {
        final SearchCustomerFragment searchCustomerFragment =
            (SearchCustomerFragment)
                new SearchCustomerFragment.Factory()
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

      default -> false;
    };
  }

  @NonNull
  public ListableFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public SelectCustomerAdapter adapter() {
    return Objects.requireNonNull(this._adapter);
  }

  @NonNull
  public SelectCustomerViewModel selectCustomerViewModel() {
    return Objects.requireNonNull(this._selectCustomerViewModel);
  }

  public void finish() {
    if (this.requireActivity() instanceof BackStack navigation
        && navigation.currentTabStackTag() != null) {
      navigation.popFragmentStack(navigation.currentTabStackTag());
    }
  }

  public static class Factory extends FragmentFactory {
    @Nullable private final CustomerModel _initialSelectedCustomer;

    public Factory(@Nullable CustomerModel initialSelectedCustomer) {
      this._initialSelectedCustomer = initialSelectedCustomer;
    }

    @Override
    @NonNull
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
      Objects.requireNonNull(classLoader);
      Objects.requireNonNull(className);

      return (className.equals(SelectCustomerFragment.class.getName()))
          ? new SelectCustomerFragment(this._initialSelectedCustomer)
          : super.instantiate(classLoader, className);
    }
  }

  private class OnBackPressedHandler extends OnBackPressedCallback {
    public OnBackPressedHandler() {
      super(true);
    }

    @Override
    public void handleOnBackPressed() {
      SelectCustomerFragment.this.selectCustomerViewModel().onCustomerSelected(null);
    }
  }
}
