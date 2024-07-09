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
