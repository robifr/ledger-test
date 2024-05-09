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

package com.robifr.ledger.ui.createqueue.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.robifr.ledger.data.model.ProductOrderModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SelectProductOrderViewModel {
  @NonNull private final CreateQueueViewModel _viewModel;
  @NonNull private final MutableLiveData<Boolean> _isContextualModeActive = new MutableLiveData<>();

  /** Selected product order indexes from {@link CreateQueueViewModel#inputtedProductOrders()} */
  @NonNull private final MutableLiveData<Set<Integer>> _selectedIndexes = new MutableLiveData<>();

  public SelectProductOrderViewModel(@NonNull CreateQueueViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @NonNull
  public LiveData<Boolean> isContextualModeActive() {
    return this._isContextualModeActive;
  }

  @NonNull
  public LiveData<Set<Integer>> selectedIndexes() {
    return this._selectedIndexes;
  }

  public void onProductOrderCheckedChanged(int productOrderIndex, boolean isChecked) {
    final HashSet<Integer> selectedIndexes =
        this._selectedIndexes.getValue() != null
            ? new HashSet<>(this._selectedIndexes.getValue())
            : new HashSet<>();

    // Replace product image — its initial name — with checkbox
    // when being selected and vice versa.
    if (isChecked) selectedIndexes.add(productOrderIndex);
    else selectedIndexes.remove(productOrderIndex);

    if (selectedIndexes.isEmpty()
        // Prevent re-invoking the live data.
        && (this._isContextualModeActive.getValue() == null
            || this._isContextualModeActive.getValue())) {
      this._isContextualModeActive.setValue(false);

    } else if (!selectedIndexes.isEmpty()
        // Prevent re-invoking the live data.
        && (this._isContextualModeActive.getValue() == null
            || !this._isContextualModeActive.getValue())) {
      this._isContextualModeActive.setValue(true);
    }

    // Only set after contextual mode is set.
    // So that `ActionMode` instance already exist when we set its title.
    this._selectedIndexes.setValue(selectedIndexes);
  }

  public void onDeleteSelectedProductOrder() {
    final TreeSet<Integer> selectedIndexes =
        this._selectedIndexes.getValue() != null
            ? new TreeSet<>(this._selectedIndexes.getValue())
            : new TreeSet<>();
    final ArrayList<ProductOrderModel> inputtedProductOrders =
        this._viewModel.inputtedProductOrders().getValue() != null
            ? new ArrayList<>(this._viewModel.inputtedProductOrders().getValue())
            : new ArrayList<>();

    for (int i = inputtedProductOrders.size(); i-- > 0; ) {
      if (selectedIndexes.contains(i)) inputtedProductOrders.remove(i);
    }

    this.reset();
    this._viewModel.onProductOrdersChanged(inputtedProductOrders);
  }

  public void reset() {
    this._isContextualModeActive.setValue(false);
    this._selectedIndexes.setValue(Collections.unmodifiableSet(new HashSet<>()));
  }
}
