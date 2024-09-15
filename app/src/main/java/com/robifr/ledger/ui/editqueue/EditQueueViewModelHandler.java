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
import androidx.annotation.NonNull;
import com.robifr.ledger.ui.createqueue.CreateQueueViewModelHandler;
import com.robifr.ledger.ui.editqueue.viewmodel.EditQueueViewModel;
import java.util.Objects;
import java.util.Optional;

public class EditQueueViewModelHandler extends CreateQueueViewModelHandler {
  public EditQueueViewModelHandler(
      @NonNull EditQueueFragment fragment, @NonNull EditQueueViewModel viewModel) {
    super(fragment, viewModel);
    viewModel
        .resultEditedQueueId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultEditedQueueId));
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultEditedQueueId(@NonNull Optional<Long> queueId) {
    Objects.requireNonNull(queueId);

    queueId.ifPresent(
        id -> {
          final Bundle bundle = new Bundle();
          bundle.putLong(EditQueueFragment.Result.EDITED_QUEUE_ID_LONG.key(), id);

          this._fragment
              .getParentFragmentManager()
              .setFragmentResult(EditQueueFragment.Request.EDIT_QUEUE.key(), bundle);
        });
    this._fragment.finish();
  }
}
