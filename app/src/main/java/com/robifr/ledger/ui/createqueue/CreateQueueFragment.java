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

package com.robifr.ledger.ui.createqueue;

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
import com.robifr.ledger.databinding.CreateQueueFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createqueue.viewmodel.CreateQueueViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class CreateQueueFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  public enum Request implements FragmentResultKey {
    CREATE_QUEUE
  }

  public enum Result implements FragmentResultKey {
    CREATED_QUEUE_ID_LONG
  }

  @Nullable protected CreateQueueFragmentBinding _fragmentBinding;
  @Nullable protected CreateQueueCustomer _inputCustomer;
  @Nullable protected CreateQueueDate _inputDate;
  @Nullable protected CreateQueueStatus _inputStatus;
  @Nullable protected CreateQueuePaymentMethod _inputPaymentMethod;
  @Nullable protected CreateQueueProductOrder _inputProductOrder;
  @Nullable protected CreateQueueResultHandler _resultHandler;

  @Nullable protected CreateQueueViewModel _createQueueViewModel;
  @Nullable protected CreateQueueViewModelHandler _viewModelHandler;

  @NonNull private final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding = CreateQueueFragmentBinding.inflate(inflater, container, false);
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);

    this._inputCustomer = new CreateQueueCustomer(this);
    this._inputDate = new CreateQueueDate(this);
    this._inputStatus = new CreateQueueStatus(this);
    this._inputPaymentMethod = new CreateQueuePaymentMethod(this);
    this._inputProductOrder = new CreateQueueProductOrder(this);
    this._createQueueViewModel = new ViewModelProvider(this).get(CreateQueueViewModel.class);
    this._viewModelHandler = new CreateQueueViewModelHandler(this, this._createQueueViewModel);

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
  public void onStart() {
    super.onStart();
    // Should be called after `CreateQueueViewModelHandler` called. `onStart` is perfect place
    // for it. If there's a fragment inherit from this class, which mostly inherit their own
    // view model handler too. Then it's impossible to not call them both inside `onViewCreated`,
    // unless `super` call is omitted entirely.
    this._resultHandler = new CreateQueueResultHandler(this);
  }

  @Override
  public boolean onMenuItemClick(@NonNull MenuItem item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(this._createQueueViewModel);

    return switch (item.getItemId()) {
      case R.id.save -> {
        this._createQueueViewModel.onSave();
        yield true;
      }

      default -> false;
    };
  }

  @NonNull
  public CreateQueueFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public CreateQueueCustomer inputCustomer() {
    return Objects.requireNonNull(this._inputCustomer);
  }

  @NonNull
  public CreateQueueDate inputDate() {
    return Objects.requireNonNull(this._inputDate);
  }

  @NonNull
  public CreateQueueStatus inputStatus() {
    return Objects.requireNonNull(this._inputStatus);
  }

  @NonNull
  public CreateQueuePaymentMethod inputPaymentMethod() {
    return Objects.requireNonNull(this._inputPaymentMethod);
  }

  @NonNull
  public CreateQueueProductOrder inputProductOrder() {
    return Objects.requireNonNull(this._inputProductOrder);
  }

  @NonNull
  public CreateQueueViewModel createQueueViewModel() {
    return Objects.requireNonNull(this._createQueueViewModel);
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
      new MaterialAlertDialogBuilder(CreateQueueFragment.this.requireContext())
          .setTitle(CreateQueueFragment.this.getString(R.string.text_discard_this_unsaved_task))
          .setNegativeButton(
              CreateQueueFragment.this.getString(R.string.text_discard),
              (dialog, type) -> CreateQueueFragment.this.finish())
          .setPositiveButton(
              CreateQueueFragment.this.getString(R.string.text_cancel),
              (dialog, type) -> dialog.dismiss())
          .show();
    }
  }
}
