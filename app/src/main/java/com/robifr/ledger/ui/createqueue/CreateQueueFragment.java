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
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.CreateQueueFragmentBinding;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createqueue.viewmodel.CreateQueueViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

@AndroidEntryPoint
public class CreateQueueFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
  public enum Request implements FragmentResultKey {
    CREATE_QUEUE;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Result implements FragmentResultKey {
    CREATED_QUEUE_ID;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  @NonNull protected final OnBackPressedHandler _onBackPressed = new OnBackPressedHandler();
  @Nullable protected CreateQueueFragmentBinding _fragmentBinding;
  @Nullable protected CreateQueueCustomer _inputCustomer;
  @Nullable protected CreateQueueDate _inputDate;
  @Nullable protected CreateQueueStatus _inputStatus;
  @Nullable protected CreateQueuePaymentMethod _inputPaymentMethod;
  @Nullable protected CreateQueueProductOrder _inputProductOrder;
  @Nullable protected CreateQueueResultHandler _resultHandler;

  @Nullable protected CreateQueueViewModel _createQueueViewModel;
  @Nullable protected CreateQueueViewModelHandler _viewModelHandler;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this._createQueueViewModel = new ViewModelProvider(this).get(CreateQueueViewModel.class);
    this._createQueueViewModel.onDateChanged(ZonedDateTime.now(ZoneId.systemDefault()));
    this._createQueueViewModel.onStatusChanged(QueueModel.Status.IN_QUEUE);
    this._createQueueViewModel.onPaymentMethodChanged(QueueModel.PaymentMethod.CASH);
    this._createQueueViewModel.setAllowedPaymentMethods(Set.of(QueueModel.PaymentMethod.CASH));
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);
    Objects.requireNonNull(this._createQueueViewModel);

    this._fragmentBinding = CreateQueueFragmentBinding.inflate(inflater, container, false);
    this._viewModelHandler = new CreateQueueViewModelHandler(this, this._createQueueViewModel);

    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);
    Objects.requireNonNull(this._createQueueViewModel);

    this._inputCustomer = new CreateQueueCustomer(this);
    this._inputDate = new CreateQueueDate(this);
    this._inputStatus = new CreateQueueStatus(this);
    this._inputPaymentMethod = new CreateQueuePaymentMethod(this);
    this._inputProductOrder = new CreateQueueProductOrder(this);
    this._resultHandler = new CreateQueueResultHandler(this);

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
