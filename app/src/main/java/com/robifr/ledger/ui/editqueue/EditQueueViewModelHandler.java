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
