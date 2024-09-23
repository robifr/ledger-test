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

package com.robifr.ledger.ui.main;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.MainDialogCreateBinding;
import java.util.Objects;

public class MainCreate implements View.OnClickListener {
  @NonNull private final MainActivity _activity;
  @NonNull private final MainDialogCreateBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public MainCreate(@NonNull MainActivity activity) {
    this._activity = Objects.requireNonNull(activity);
    this._dialogBinding = MainDialogCreateBinding.inflate(this._activity.getLayoutInflater());
    this._dialog = new BottomSheetDialog(this._activity, R.style.BottomSheetDialog);

    this._dialogBinding.createQueueButton.setOnClickListener(this);
    this._dialogBinding.createCustomerButton.setOnClickListener(this);
    this._dialogBinding.createProductButton.setOnClickListener(this);
    this._dialog.setContentView(this._dialogBinding.getRoot());
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    final NavController navController =
        Navigation.findNavController(this._activity, R.id.fragmentContainer);

    switch (view.getId()) {
      case R.id.createQueueButton -> navController.navigate(R.id.createQueueFragment);
      case R.id.createCustomerButton -> navController.navigate(R.id.createCustomerFragment);
      case R.id.createProductButton -> navController.navigate(R.id.createProductFragment);
    }

    this._dialog.dismiss();
  }

  public void openDialog() {
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }
}
