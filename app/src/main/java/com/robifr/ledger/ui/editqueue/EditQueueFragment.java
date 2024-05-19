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
import com.robifr.ledger.R;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createqueue.CreateQueueFragment;
import com.robifr.ledger.ui.editqueue.viewmodel.EditQueueViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class EditQueueFragment extends CreateQueueFragment {
  public enum Arguments implements FragmentResultKey {
    INITIAL_QUEUE_ID_TO_EDIT
  }

  public enum Request implements FragmentResultKey {
    EDIT_QUEUE
  }

  public enum Result implements FragmentResultKey {
    EDITED_QUEUE_ID
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    super.onViewCreated(view, savedInstance);
    Objects.requireNonNull(this._fragmentBinding);

    this._createQueueViewModel = new ViewModelProvider(this).get(EditQueueViewModel.class);
    this._viewModelHandler =
        new EditQueueViewModelHandler(this, (EditQueueViewModel) this._createQueueViewModel);

    this._fragmentBinding.toolbar.setTitle(this.getString(R.string.text_edit_queue));
  }
}
