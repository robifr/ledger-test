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

package com.robifr.ledger.ui.createproduct;

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
import com.robifr.ledger.databinding.CreateProductFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createproduct.viewmodel.CreateProductViewModel;
import com.robifr.ledger.util.Compats;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class CreateProductFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  public enum Request implements FragmentResultKey {
    CREATE_PRODUCT
  }

  public enum Result implements FragmentResultKey {
    CREATED_PRODUCT_ID_LONG
  }

  @Nullable protected CreateProductFragmentBinding _fragmentBinding;
  @Nullable protected CreateProductName _inputName;
  @Nullable protected CreateProductPrice _inputPrice;

  @Nullable protected CreateProductViewModel _createProductViewModel;
  @Nullable protected CreateProductViewModelHandler _viewModelHandler;

  @NonNull private final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding = CreateProductFragmentBinding.inflate(inflater, container, false);
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);

    this._inputName = new CreateProductName(this);
    this._inputPrice = new CreateProductPrice(this);
    this._createProductViewModel = new ViewModelProvider(this).get(CreateProductViewModel.class);
    this._viewModelHandler = new CreateProductViewModelHandler(this, this._createProductViewModel);

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
    Objects.requireNonNull(this._createProductViewModel);

    return switch (item.getItemId()) {
      case R.id.save -> {
        this._createProductViewModel.onSave();
        yield true;
      }

      default -> false;
    };
  }

  @NonNull
  public CreateProductFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public CreateProductName inputName() {
    return Objects.requireNonNull(this._inputName);
  }

  @NonNull
  public CreateProductPrice inputPrice() {
    return Objects.requireNonNull(this._inputPrice);
  }

  @NonNull
  public CreateProductViewModel createProductViewModel() {
    return Objects.requireNonNull(this._createProductViewModel);
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
      new MaterialAlertDialogBuilder(CreateProductFragment.this.requireContext())
          .setTitle(R.string.createProduct_unsavedChangesWarning)
          .setNegativeButton(
              R.string.action_discardAndLeave,
              (dialog, type) -> CreateProductFragment.this.finish())
          .setPositiveButton(R.string.action_cancel, (dialog, type) -> dialog.dismiss())
          .show();
    }
  }
}
