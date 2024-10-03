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

package com.robifr.ledger.ui.createcustomer;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.CreateCustomerFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createcustomer.viewmodel.CreateCustomerViewModel;
import com.robifr.ledger.util.Compats;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class CreateCustomerFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  public enum Request implements FragmentResultKey {
    CREATE_CUSTOMER
  }

  public enum Result implements FragmentResultKey {
    CREATED_CUSTOMER_ID_LONG
  }

  @Nullable protected CreateCustomerFragmentBinding _fragmentBinding;
  @Nullable protected CreateCustomerName _inputName;
  @Nullable protected CreateCustomerBalance _inputBalance;
  @Nullable protected CreateCustomerDebt _inputDebt;

  @Nullable protected CreateCustomerViewModel _createCustomerViewModel;
  @Nullable protected CreateCustomerViewModelHandler _viewModelHandler;

  @NonNull private final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding = CreateCustomerFragmentBinding.inflate(inflater, container, false);
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);

    this._inputName = new CreateCustomerName(this);
    this._inputBalance = new CreateCustomerBalance(this);
    this._inputDebt = new CreateCustomerDebt(this);
    this._createCustomerViewModel = new ViewModelProvider(this).get(CreateCustomerViewModel.class);
    this._viewModelHandler =
        new CreateCustomerViewModelHandler(this, this._createCustomerViewModel);

    this.requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this.getViewLifecycleOwner(), this._onBackPressed);
    this._fragmentBinding.toolbar.getMenu().clear();
    this._fragmentBinding.toolbar.inflateMenu(R.menu.reusable_toolbar_edit);
    this._fragmentBinding.toolbar.setOnMenuItemClickListener(this);
    this._fragmentBinding.toolbar.setNavigationOnClickListener(
        v -> this._onBackPressed.handleOnBackPressed());
  }

  @Override
  public boolean onMenuItemClick(@NonNull MenuItem item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(this._createCustomerViewModel);

    return switch (item.getItemId()) {
      case R.id.save -> {
        this._createCustomerViewModel.onSave();
        yield true;
      }

      default -> false;
    };
  }

  @NonNull
  public CreateCustomerFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public CreateCustomerName inputName() {
    return Objects.requireNonNull(this._inputName);
  }

  @NonNull
  public CreateCustomerBalance inputBalance() {
    return Objects.requireNonNull(this._inputBalance);
  }

  @NonNull
  public CreateCustomerDebt inputDebt() {
    return Objects.requireNonNull(this._inputDebt);
  }

  @NonNull
  public CreateCustomerViewModel createCustomerViewModel() {
    return Objects.requireNonNull(this._createCustomerViewModel);
  }

  public void finish() {
    Objects.requireNonNull(this._fragmentBinding);

    Compats.hideKeyboard(this.requireContext(), this.requireView().findFocus());
    Navigation.findNavController(this._fragmentBinding.getRoot()).popBackStack();
  }

  private class OnBackPressedHandler extends OnBackPressedCallback {
    public OnBackPressedHandler() {
      super(true);
    }

    @Override
    public void handleOnBackPressed() {
      new MaterialAlertDialogBuilder(CreateCustomerFragment.this.requireContext())
          .setTitle(
              CreateCustomerFragment.this.getString(R.string.createCustomer_unsavedChangesWarning))
          .setNegativeButton(
              CreateCustomerFragment.this.getString(R.string.action_discardAndLeave),
              (dialog, type) -> CreateCustomerFragment.this.finish())
          .setPositiveButton(
              CreateCustomerFragment.this.getString(R.string.action_cancel),
              (dialog, type) -> dialog.dismiss())
          .show();
    }
  }
}
