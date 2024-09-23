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

package com.robifr.ledger.ui.product.filter;

import android.content.DialogInterface;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.ProductDialogFilterBinding;
import com.robifr.ledger.ui.product.ProductFragment;
import java.util.Objects;

public class ProductFilter implements DialogInterface.OnDismissListener {
  @NonNull private final ProductFragment _fragment;
  @NonNull private final ProductDialogFilterBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;
  @NonNull private final ProductFilterPrice _filterPrice;

  public ProductFilter(@NonNull ProductFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._dialogBinding = ProductDialogFilterBinding.inflate(this._fragment.getLayoutInflater());

    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);
    this._dialog.setContentView(this._dialogBinding.getRoot());

    this._filterPrice = new ProductFilterPrice(this._fragment, this._dialogBinding, this._dialog);

    this._dialog.setOnDismissListener(this);
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    Objects.requireNonNull(dialog);

    this._fragment
        .productViewModel()
        .selectAllProducts()
        .observe(
            this._fragment,
            products ->
                this._fragment
                    .productViewModel()
                    .filterView()
                    .onFiltersChanged(
                        this._fragment.productViewModel().filterView().inputtedFilters(),
                        products));

    if (this._dialog.getCurrentFocus() != null) this._dialog.getCurrentFocus().clearFocus();
  }

  @NonNull
  public ProductFilterPrice filterPrice() {
    return this._filterPrice;
  }

  public void openDialog() {
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }
}
