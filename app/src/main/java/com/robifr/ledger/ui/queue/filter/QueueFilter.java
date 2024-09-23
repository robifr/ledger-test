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

package com.robifr.ledger.ui.queue.filter;

import android.content.DialogInterface;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.QueueDialogFilterBinding;
import com.robifr.ledger.ui.queue.QueueFragment;
import java.util.Objects;

public class QueueFilter implements DialogInterface.OnDismissListener {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;
  @NonNull private final QueueFilterCustomer _filterCustomer;
  @NonNull private final QueueFilterDate _filterDate;
  @NonNull private final QueueFilterStatus _filterStatus;
  @NonNull private final QueueFilterTotalPrice _filterTotalPrice;

  public QueueFilter(@NonNull QueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding =
        QueueDialogFilterBinding.inflate(
            this._fragment.getLayoutInflater(), this._fragment.fragmentBinding().getRoot(), false);

    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);
    this._dialog.setContentView(this._dialogBinding.getRoot());

    this._filterCustomer =
        new QueueFilterCustomer(this._fragment, this._dialogBinding, this._dialog);
    this._filterDate = new QueueFilterDate(this._fragment, this._dialogBinding, this._dialog);
    this._filterStatus = new QueueFilterStatus(this._fragment, this._dialogBinding, this._dialog);
    this._filterTotalPrice =
        new QueueFilterTotalPrice(this._fragment, this._dialogBinding, this._dialog);

    this._dialog.setOnDismissListener(this);
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    Objects.requireNonNull(dialog);

    this._fragment
        .queueViewModel()
        .selectAllQueues()
        .observe(
            this._fragment,
            queues ->
                this._fragment
                    .queueViewModel()
                    .filterView()
                    .onFiltersChanged(
                        this._fragment.queueViewModel().filterView().inputtedFilters(), queues));

    if (this._dialog.getCurrentFocus() != null) this._dialog.getCurrentFocus().clearFocus();
  }

  @NonNull
  public QueueFilterCustomer filterCustomer() {
    return this._filterCustomer;
  }

  @NonNull
  public QueueFilterDate filterDate() {
    return this._filterDate;
  }

  @NonNull
  public QueueFilterStatus filterStatus() {
    return this._filterStatus;
  }

  @NonNull
  public QueueFilterTotalPrice filterTotalPrice() {
    return this._filterTotalPrice;
  }

  public void openDialog() {
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }
}
