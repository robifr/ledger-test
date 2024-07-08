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

package com.robifr.ledger.ui.queue.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.QueueFilterer;
import com.robifr.ledger.data.display.QueueSortMethod;
import com.robifr.ledger.data.display.QueueSorter;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.QueueRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class QueueViewModel extends ViewModel {
  @NonNull private final QueueRepository _queueRepository;
  @NonNull private final CustomerRepository _customerRepository;

  @NonNull
  private final QueueChangedListener _queueChangedListener = new QueueChangedListener(this);

  @NonNull
  private final CustomerChangedListener _customerChangedListener =
      new CustomerChangedListener(this);

  @NonNull private final QueueFilterViewModel _filterView;
  @NonNull private final QueueSorter _sorter = new QueueSorter();

  @NonNull
  private final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final SafeMutableLiveData<List<QueueModel>> _queues =
      new SafeMutableLiveData<>(List.of());

  @NonNull
  private final SafeMutableLiveData<QueueSortMethod> _sortMethod =
      new SafeMutableLiveData<>(new QueueSortMethod(QueueSortMethod.SortBy.CUSTOMER_NAME, true));

  /**
   * Currently expanded queue index from {@link QueueViewModel#_queues queues}. -1 to represent none
   * being expanded.
   */
  @NonNull
  private final SafeMutableLiveData<Integer> _expandedQueueIndex = new SafeMutableLiveData<>(-1);

  @Inject
  public QueueViewModel(
      @NonNull QueueRepository queueRepository, @NonNull CustomerRepository customerRepository) {
    this._queueRepository = Objects.requireNonNull(queueRepository);
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._filterView = new QueueFilterViewModel(this, new QueueFilterer());

    this._queueRepository.addModelChangedListener(this._queueChangedListener);
    this._customerRepository.addModelChangedListener(this._customerChangedListener);

    // Setting up initial values inside a fragment is painful. See commit d5604599.
    SafeEvent.observeOnce(
        this.selectAllQueues(),
        queues -> this._filterView.onFiltersChanged(this._filterView.inputtedFilters(), queues),
        Objects::nonNull);
  }

  @Override
  public void onCleared() {
    this._queueRepository.removeModelChangedListener(this._queueChangedListener);
    this._customerRepository.removeModelChangedListener(this._customerChangedListener);
  }

  @NonNull
  public QueueFilterViewModel filterView() {
    return this._filterView;
  }

  @NonNull
  public LiveData<SafeEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<List<QueueModel>> queues() {
    return this._queues;
  }

  @NonNull
  public SafeLiveData<QueueSortMethod> sortMethod() {
    return this._sortMethod;
  }

  /**
   * @see QueueViewModel#_expandedQueueIndex
   */
  public SafeLiveData<Integer> expandedQueueIndex() {
    return this._expandedQueueIndex;
  }

  @NonNull
  public LiveData<List<QueueModel>> selectAllQueues() {
    final MutableLiveData<List<QueueModel>> result = new MutableLiveData<>();

    this._queueRepository
        .selectAll()
        .thenAcceptAsync(
            queues -> {
              if (queues == null) {
                this._snackbarMessage.postValue(
                    new SafeEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_unable_to_retrieve_all_queues)));
              }

              result.postValue(queues);
            });
    return result;
  }

  public void deleteQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._queueRepository
        .delete(queue)
        .thenAcceptAsync(
            effected -> {
              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_deleted_x_queue, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_delete_queue);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }

  public void onQueuesChanged(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    // Ensuring currently expanded queue index is selecting the same queue
    // from the new `queues` list. Do this before list being updated,
    // because the view will get re-binding while requiring the expanded queue index.
    final Long currentExpandedQueueId =
        this._expandedQueueIndex.getValue() != -1
            ? this._queues.getValue().get(this._expandedQueueIndex.getValue()).id()
            : null;
    int expandedQueueIndex = -1;

    for (int i = 0; i < queues.size(); i++) {
      if (queues.get(i).id() != null && queues.get(i).id().equals(currentExpandedQueueId)) {
        expandedQueueIndex = i;
        break;
      }
    }

    this.onExpandedQueueIndexChanged(expandedQueueIndex);
    this._queues.setValue(Collections.unmodifiableList(queues));
  }

  public void onSortMethodChanged(@NonNull QueueSortMethod sortMethod) {
    this.onSortMethodChanged(sortMethod, this._queues.getValue());
  }

  public void onSortMethodChanged(
      @NonNull QueueSortMethod sortMethod, @NonNull List<QueueModel> queues) {
    Objects.requireNonNull(sortMethod);
    Objects.requireNonNull(queues);

    this._sortMethod.setValue(sortMethod);
    this._sorter.setSortMethod(sortMethod);
    this.onQueuesChanged(this._sorter.sort(queues));
  }

  /**
   * @see QueueViewModel#onSortMethodChanged(QueueSortMethod.SortBy, List)
   */
  public void onSortMethodChanged(@NonNull QueueSortMethod.SortBy sortBy) {
    this.onSortMethodChanged(sortBy, this._queues.getValue());
  }

  /**
   * Sort {@link QueueViewModel#queues() queues} based on specified {@link QueueSortMethod.SortBy}
   * type. Doing so will reverse the order — Ascending becomes descending and vice versa. Use {@link
   * QueueViewModel#onSortMethodChanged(QueueSortMethod)} if you want to apply the order by
   * yourself.
   */
  public void onSortMethodChanged(
      @NonNull QueueSortMethod.SortBy sortBy, @NonNull List<QueueModel> queues) {
    Objects.requireNonNull(sortBy);
    Objects.requireNonNull(queues);

    // Reverse sort order when selecting same sort option.
    final boolean isAscending =
        this._sortMethod.getValue().sortBy() == sortBy
            ? !this._sortMethod.getValue().isAscending()
            : this._sortMethod.getValue().isAscending();

    this.onSortMethodChanged(new QueueSortMethod(sortBy, isAscending), queues);
  }

  public void onExpandedQueueIndexChanged(int index) {
    this._expandedQueueIndex.setValue(index);
  }
}
