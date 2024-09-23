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

package com.robifr.ledger.ui.queue.viewmodel;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.display.QueueFilterer;
import com.robifr.ledger.data.display.QueueFilters;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.util.CurrencyFormat;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class QueueFilterViewModel {
  @NonNull private final QueueViewModel _viewModel;
  @NonNull private final QueueFilterer _filterer;

  @NonNull
  private final SafeMutableLiveData<List<Long>> _inputtedCustomerIds =
      new SafeMutableLiveData<>(List.of());

  @NonNull
  private final SafeMutableLiveData<Boolean> _inputtedIsNullCustomerShown =
      new SafeMutableLiveData<>(true);

  @NonNull
  private final SafeMutableLiveData<Set<QueueModel.Status>> _inputtedStatus =
      new SafeMutableLiveData<>(Set.of(QueueModel.Status.values()));

  @NonNull
  private final SafeMutableLiveData<String> _inputtedMinTotalPriceText =
      new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<String> _inputtedMaxTotalPriceText =
      new SafeMutableLiveData<>("");

  @NonNull
  private final SafeMutableLiveData<QueueDate> _inputtedDate =
      new SafeMutableLiveData<>(QueueDate.withRange(QueueDate.Range.ALL_TIME));

  public QueueFilterViewModel(@NonNull QueueViewModel viewModel, @NonNull QueueFilterer filterer) {
    this._viewModel = Objects.requireNonNull(viewModel);
    this._filterer = Objects.requireNonNull(filterer);
  }

  @NonNull
  public SafeLiveData<List<Long>> inputtedCustomerIds() {
    return this._inputtedCustomerIds;
  }

  @NonNull
  public SafeLiveData<Boolean> inputtedIsNullCustomerShown() {
    return this._inputtedIsNullCustomerShown;
  }

  @NonNull
  public SafeLiveData<Set<QueueModel.Status>> inputtedStatus() {
    return this._inputtedStatus;
  }

  @NonNull
  public SafeLiveData<String> inputtedMinTotalPriceText() {
    return this._inputtedMinTotalPriceText;
  }

  @NonNull
  public SafeLiveData<String> inputtedMaxTotalPriceText() {
    return this._inputtedMaxTotalPriceText;
  }

  @NonNull
  public SafeLiveData<QueueDate> inputtedDate() {
    return this._inputtedDate;
  }

  /**
   * Get current inputted filter from any corresponding inputted live data. If any live data is set
   * using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public QueueFilters inputtedFilters() {
    // Nullable value to represent unbounded range.
    BigDecimal minTotalPrice = null;
    BigDecimal maxTotalPrice = null;

    try {
      if (!this._inputtedMinTotalPriceText.getValue().isBlank()) {
        minTotalPrice =
            CurrencyFormat.parse(this._inputtedMinTotalPriceText.getValue(), "id", "ID");
      }

    } catch (ParseException ignore) {
    }

    try {
      if (!this._inputtedMaxTotalPriceText.getValue().isBlank()) {
        maxTotalPrice =
            CurrencyFormat.parse(this._inputtedMaxTotalPriceText.getValue(), "id", "ID");
      }

    } catch (ParseException ignore) {
    }

    return QueueFilters.toBuilder()
        .setFilteredCustomerIds(this._inputtedCustomerIds.getValue())
        .setNullCustomerShown(this._inputtedIsNullCustomerShown.getValue())
        .setFilteredStatus(this._inputtedStatus.getValue())
        .setFilteredDate(this._inputtedDate.getValue())
        .setFilteredTotalPrice(new Pair<>(minTotalPrice, maxTotalPrice))
        .build();
  }

  public void onCustomersIdsChanged(@NonNull List<Long> customerIds) {
    Objects.requireNonNull(customerIds);

    this._inputtedCustomerIds.setValue(Collections.unmodifiableList(customerIds));
  }

  public void onNullCustomerShownEnabled(boolean isShown) {
    this._inputtedIsNullCustomerShown.setValue(isShown);
  }

  public void onStatusChanged(@NonNull Set<QueueModel.Status> status) {
    Objects.requireNonNull(status);

    this._inputtedStatus.setValue(Collections.unmodifiableSet(status));
  }

  public void onMinTotalPriceTextChanged(@NonNull String minTotalPrice) {
    Objects.requireNonNull(minTotalPrice);

    this._inputtedMinTotalPriceText.setValue(minTotalPrice);
  }

  public void onMaxTotalPriceTextChanged(@NonNull String maxTotalPrice) {
    Objects.requireNonNull(maxTotalPrice);

    this._inputtedMaxTotalPriceText.setValue(maxTotalPrice);
  }

  public void onDateChanged(@NonNull QueueDate date) {
    Objects.requireNonNull(date);

    this._inputtedDate.setValue(date);
  }

  public void onFiltersChanged(@NonNull QueueFilters filters) {
    this.onFiltersChanged(filters, this._viewModel.queues().getValue());
  }

  public void onFiltersChanged(@NonNull QueueFilters filters, @NonNull List<QueueModel> queues) {
    Objects.requireNonNull(filters);
    Objects.requireNonNull(queues);

    final String minTotalPrice =
        filters.filteredTotalPrice().first != null
            ? CurrencyFormat.format(filters.filteredTotalPrice().first, "id", "ID")
            : "";
    final String maxTotalPrice =
        filters.filteredTotalPrice().second != null
            ? CurrencyFormat.format(filters.filteredTotalPrice().second, "id", "ID")
            : "";

    this.onCustomersIdsChanged(filters.filteredCustomerIds());
    this.onNullCustomerShownEnabled(filters.isNullCustomerShown());
    this.onStatusChanged(filters.filteredStatus());
    this.onDateChanged(filters.filteredDate());
    this.onMinTotalPriceTextChanged(minTotalPrice);
    this.onMaxTotalPriceTextChanged(maxTotalPrice);
    this._filterer.setFilters(filters);

    final List<QueueModel> filteredQueues = this._filterer.filter(queues);
    // Re-sort the list, after previously re-populating the list with a new filtered value.
    this._viewModel.onSortMethodChanged(this._viewModel.sortMethod().getValue(), filteredQueues);
  }
}
