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

package com.robifr.ledger.ui.main.queue.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.QueueCardDialogMenuBinding;
import com.robifr.ledger.ui.BackStack;
import com.robifr.ledger.ui.edit_queue.EditQueueFragment;
import com.robifr.ledger.ui.main.queue.QueueFragment;
import java.util.Objects;

public class QueueListMenu implements View.OnClickListener {
  @NonNull private final QueueFragment _fragment;
  @NonNull private final QueueListHolder _holder;
  @NonNull private final QueueCardDialogMenuBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public QueueListMenu(@NonNull QueueFragment fragment, @NonNull QueueListHolder holder) {
    this._fragment = Objects.requireNonNull(fragment);
    this._holder = Objects.requireNonNull(holder);
    this._dialogBinding = QueueCardDialogMenuBinding.inflate(this._fragment.getLayoutInflater());
    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);

    this._dialog.setContentView(this._dialogBinding.getRoot());
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.editButton -> {
        if (this._holder.boundQueue().id() == null) return;

        final EditQueueFragment editQueueFragment =
            (EditQueueFragment)
                new EditQueueFragment.Factory(this._holder.boundQueue().id())
                    .instantiate(
                        this._fragment.requireContext().getClassLoader(),
                        EditQueueFragment.class.getName());

        if (this._fragment.requireActivity() instanceof BackStack navigation
            && navigation.currentTabStackTag() != null) {
          navigation.pushFragmentStack(
              navigation.currentTabStackTag(),
              editQueueFragment,
              EditQueueFragment.class.toString());
          this._dialog.dismiss();
        }
      }

      case R.id.deleteButton -> {
        this._fragment.queueViewModel().deleteQueue(this._holder.boundQueue());
        this._dialog.dismiss();
      }
    }
  }

  public void openDialog() {
    this._dialogBinding.editButton.setOnClickListener(this);
    this._dialogBinding.deleteButton.setOnClickListener(this);
    this._dialog.show();
  }
}
