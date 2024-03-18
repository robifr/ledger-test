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

package com.robifr.ledger.ui.queue.filter;

import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
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

    this._fragment.queueViewModel().fetchAllQueues();

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
    // Allow bottom sheet to go fully expanded.
    final View bottomSheet =
        Objects.requireNonNull(
            this._dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet));
    bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
    bottomSheet.setLayoutParams(bottomSheet.getLayoutParams());

    this._dialog.show();
  }
}
