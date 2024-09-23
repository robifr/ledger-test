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
