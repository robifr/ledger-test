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

package com.robifr.ledger.ui.editqueue;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createqueue.CreateQueueFragment;
import com.robifr.ledger.ui.editqueue.viewmodel.EditQueueViewModel;
import java.time.ZoneId;
import java.util.Objects;

public class EditQueueFragment extends CreateQueueFragment {
  public enum Arguments implements FragmentResultKey {
    INITIAL_QUEUE_ID_TO_EDIT;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Request implements FragmentResultKey {
    EDIT_QUEUE;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Result implements FragmentResultKey {
    EDITED_QUEUE_ID;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    super.onViewCreated(view, savedInstance);
    Objects.requireNonNull(this._fragmentBinding);

    this._createQueueViewModel =
        new ViewModelProvider(this, new EditQueueViewModel.Factory(this.requireContext()))
            .get(EditQueueViewModel.class);
    this._viewModelHandler =
        new EditQueueViewModelHandler(this, (EditQueueViewModel) this._createQueueViewModel);

    this._fragmentBinding.toolbar.setTitle(this.getString(R.string.text_edit_queue));

    final NavBackStackEntry backStackEntry =
        Navigation.findNavController(this._fragmentBinding.getRoot()).getCurrentBackStackEntry();

    if (this._createQueueViewModel instanceof EditQueueViewModel editQueueViewModel
        && backStackEntry != null
        && backStackEntry.getArguments() != null) {
      final QueueModel initialQueue =
          editQueueViewModel.selectQueueById(
              backStackEntry.getArguments().getLong(Arguments.INITIAL_QUEUE_ID_TO_EDIT.key(), 0L));
      Objects.requireNonNull(initialQueue); // Logically shouldn't be null when editing data.

      editQueueViewModel.setInitialQueueToEdit(initialQueue);
      editQueueViewModel.onCustomerChanged(initialQueue.customer());
      editQueueViewModel.onDateChanged(initialQueue.date().atZone(ZoneId.systemDefault()));
      editQueueViewModel.onStatusChanged(initialQueue.status());
      editQueueViewModel.onPaymentMethodChanged(initialQueue.paymentMethod());
      editQueueViewModel.onAddProductOrder(
          initialQueue.productOrders().toArray(new ProductOrderModel[0]));
    }
  }
}
