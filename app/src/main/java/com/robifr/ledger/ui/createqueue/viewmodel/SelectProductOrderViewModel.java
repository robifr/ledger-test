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
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SelectProductOrderViewModel {
  @NonNull private final CreateQueueViewModel _viewModel;

  @NonNull
  private final SafeMutableLiveData<Boolean> _isContextualModeActive =
      new SafeMutableLiveData<>(false);

  /** Selected product order indexes from {@link CreateQueueViewModel#inputtedProductOrders()} */
  @NonNull
  private final SafeMutableLiveData<Set<Integer>> _selectedIndexes =
      new SafeMutableLiveData<>(Set.of());

  public SelectProductOrderViewModel(@NonNull CreateQueueViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @NonNull
  public SafeLiveData<Boolean> isContextualModeActive() {
    return this._isContextualModeActive;
  }

  @NonNull
  public SafeLiveData<Set<Integer>> selectedIndexes() {
    return this._selectedIndexes;
  }

  public void onProductOrderCheckedChanged(int productOrderIndex, boolean isChecked) {
    final HashSet<Integer> selectedIndexes = new HashSet<>(this._selectedIndexes.getValue());

    // Replace product image — its initial name — with checkbox
    // when being selected and vice versa.
    if (isChecked) selectedIndexes.add(productOrderIndex);
    else selectedIndexes.remove(productOrderIndex);

    if (selectedIndexes.isEmpty()
        // Prevent re-invoking the live data.
        && this._isContextualModeActive.getValue()) {
      this._isContextualModeActive.setValue(false);

    } else if (!selectedIndexes.isEmpty()
        // Prevent re-invoking the live data.
        && (!this._isContextualModeActive.getValue())) {
      this._isContextualModeActive.setValue(true);
    }

    // Only set after contextual mode is set.
    // So that `ActionMode` instance already exist when we set its title.
    this._selectedIndexes.setValue(selectedIndexes);
  }

  public void onDeleteSelectedProductOrder() {
    final TreeSet<Integer> selectedIndexes = new TreeSet<>(this._selectedIndexes.getValue());
    final ArrayList<ProductOrderModel> inputtedProductOrders =
        new ArrayList<>(this._viewModel.inputtedProductOrders().getValue());

    for (int i = inputtedProductOrders.size(); i-- > 0; ) {
      if (selectedIndexes.contains(i)) inputtedProductOrders.remove(i);
    }

    this.reset();
    this._viewModel.onProductOrdersChanged(inputtedProductOrders);
  }

  public void reset() {
    this._isContextualModeActive.setValue(false);
    this._selectedIndexes.setValue(Set.of());
  }
}
