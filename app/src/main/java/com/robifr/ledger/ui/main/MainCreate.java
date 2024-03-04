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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.MainDialogCreateBinding;
import com.robifr.ledger.ui.createcustomer.CreateCustomerFragment;
import com.robifr.ledger.ui.createproduct.CreateProductFragment;
import com.robifr.ledger.ui.createqueue.CreateQueueFragment;
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

    switch (view.getId()) {
      case R.id.createQueueButton -> {
        final CreateQueueFragment createQueueFragment =
            (CreateQueueFragment)
                new CreateQueueFragment.Factory()
                    .instantiate(
                        this._activity.getClassLoader(), CreateQueueFragment.class.getName());

        this._activity.pushFragmentStack(
            MainActivity.BottomNavigationTabTag.QUEUE.toString(),
            createQueueFragment,
            CreateQueueFragment.class.toString());
        this._activity.navigateTabStack(MainActivity.BottomNavigationTabTag.QUEUE.toString());
      }

      case R.id.createCustomerButton -> {
        final CreateCustomerFragment createCustomerFragment =
            (CreateCustomerFragment)
                new CreateCustomerFragment.Factory()
                    .instantiate(
                        this._activity.getClassLoader(), CreateCustomerFragment.class.getName());

        this._activity.pushFragmentStack(
            MainActivity.BottomNavigationTabTag.CUSTOMER.toString(),
            createCustomerFragment,
            CreateCustomerFragment.class.toString());
        this._activity.navigateTabStack(MainActivity.BottomNavigationTabTag.CUSTOMER.toString());
      }

      case R.id.createProductButton -> {
        final CreateProductFragment createProductFragment =
            (CreateProductFragment)
                new CreateProductFragment.Factory()
                    .instantiate(
                        this._activity.getClassLoader(), CreateProductFragment.class.getName());

        this._activity.pushFragmentStack(
            MainActivity.BottomNavigationTabTag.PRODUCT.toString(),
            createProductFragment,
            CreateProductFragment.class.toString());
        this._activity.navigateTabStack(MainActivity.BottomNavigationTabTag.PRODUCT.toString());
      }
    }

    this._dialog.dismiss();
  }

  public void openDialog() {
    this._dialog.show();
  }
}
