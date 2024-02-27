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

package com.robifr.ledger.ui.edit_queue;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.create_queue.CreateQueueViewModelHandler;
import com.robifr.ledger.ui.edit_queue.view_model.EditQueueViewModel;

public class EditQueueViewModelHandler extends CreateQueueViewModelHandler {
  public EditQueueViewModelHandler(
      @NonNull EditQueueFragment fragment, @NonNull EditQueueViewModel viewModel) {
    super(fragment, viewModel);
    viewModel
        .editedQueueId()
        .observe(this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onEditedQueueId));
  }

  private void _onEditedQueueId(@Nullable Long queueId) {
    if (queueId != null) {
      final Bundle bundle = new Bundle();
      bundle.putLong(EditQueueFragment.Result.EDITED_QUEUE_ID.key(), queueId);

      this._fragment
          .getParentFragmentManager()
          .setFragmentResult(EditQueueFragment.Request.EDIT_QUEUE.key(), bundle);
    }

    this._fragment.finish();
  }
}
