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

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.QueueFilterer;
import com.robifr.ledger.data.QueueFilters;
import com.robifr.ledger.data.QueueSortMethod;
import com.robifr.ledger.data.QueueSorter;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.QueueRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.LiveDataModelChangedListener;
import com.robifr.ledger.ui.StringResources;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;

@HiltViewModel
public class QueueViewModel extends ViewModel {
  @NonNull private final QueueRepository _queueRepository;
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final QueueChangedListener _queueChangedListener;

  @NonNull
  private final CustomerChangedListener _customerChangedListener =
      new CustomerChangedListener(this);

  @NonNull private final QueueFilterViewModel _filterView;
  @NonNull private final QueueSorter _sorter = new QueueSorter();

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<QueueModel>> _queues = new MutableLiveData<>();
  @NonNull private final MutableLiveData<QueueSortMethod> _sortMethod = new MutableLiveData<>();

  /**
   * Currently expanded queue index from {@link QueueViewModel#_queues queues}. -1 or null to
   * represent none being expanded.
   */
  @NonNull private final MutableLiveData<Integer> _expandedQueueIndex = new MutableLiveData<>();

  @Inject
  public QueueViewModel(
      @NonNull QueueRepository queueRepository, @NonNull CustomerRepository customerRepository) {
    this._queueRepository = Objects.requireNonNull(queueRepository);
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._queueChangedListener = new QueueChangedListener(this._queues);

    final QueueFilterer filterer = new QueueFilterer();
    filterer.setFilters(
        QueueFilters.toBuilder()
            .setNullCustomerShown(true)
            .setFilteredDate(QueueFilters.DateRange.ALL_TIME)
            .setFilteredDateStartEnd(QueueFilters.DateRange.ALL_TIME.dateStartEnd())
            .setFilteredStatus(Set.of(QueueModel.Status.values()))
            .build());

    this._filterView = new QueueFilterViewModel(this, filterer);

    this._queueRepository.addModelChangedListener(this._queueChangedListener);
    this._customerRepository.addModelChangedListener(this._customerChangedListener);

    // It's unusual indeed to call its own method in its constructor. Setting up initial values
    // inside a fragment is painful. You have to consider whether the fragment recreated due to
    // configuration changes, or if it's popped from the backstack, or when the view model itself
    // is recreated due to the fragment being navigated by bottom navigation.
    this.onSortMethodChanged(new QueueSortMethod(QueueSortMethod.SortBy.CUSTOMER_NAME, true));

    final LiveData<List<QueueModel>> selectAllQueues = this.selectAllQueues();
    selectAllQueues.observeForever(
        new Observer<>() {
          @Override
          public void onChanged(@Nullable List<QueueModel> queues) {
            if (queues != null) {
              QueueViewModel.this._filterView.onFiltersChanged(
                  QueueViewModel.this._filterView.inputtedFilters(), queues);
            }

            selectAllQueues.removeObserver(this);
          }
        });
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
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<List<QueueModel>> queues() {
    return this._queues;
  }

  @NonNull
  public LiveData<QueueSortMethod> sortMethod() {
    return this._sortMethod;
  }

  /**
   * @see QueueViewModel#_expandedQueueIndex
   */
  public LiveData<Integer> expandedQueueIndex() {
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
                    new LiveDataEvent<>(
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
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
  }

  public void onQueuesChanged(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    // Ensuring currently expanded queue index is selecting the same queue
    // from the new `queues` list. Do this before list being updated,
    // because the view will get re-binding while requiring the expanded queue index.
    final Long currentExpandedQueueId =
        this._queues.getValue() != null
                && this._expandedQueueIndex.getValue() != null
                && this._expandedQueueIndex.getValue() != -1
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
    final List<QueueModel> queues =
        Objects.requireNonNullElse(this._queues.getValue(), new ArrayList<>());
    this.onSortMethodChanged(sortMethod, queues);
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
    final List<QueueModel> queues =
        Objects.requireNonNullElse(this._queues.getValue(), new ArrayList<>());
    this.onSortMethodChanged(sortBy, queues);
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

    final QueueSortMethod sortMethod = this._sortMethod.getValue();
    if (sortMethod == null) return;

    // Reverse sort order when selecting same sort option.
    final boolean isAscending =
        sortMethod.sortBy() == sortBy ? !sortMethod.isAscending() : sortMethod.isAscending();

    this.onSortMethodChanged(new QueueSortMethod(sortBy, isAscending), queues);
  }

  public void onExpandedQueueIndexChanged(int index) {
    this._expandedQueueIndex.setValue(index);
  }

  private class QueueChangedListener extends LiveDataModelChangedListener<QueueModel> {
    public QueueChangedListener(@NonNull MutableLiveData<List<QueueModel>> queues) {
      super(queues);
    }

    @Override
    @MainThread
    public void onUpdateLiveData(@NonNull List<QueueModel> queues) {
      QueueViewModel.this._filterView.onFiltersChanged(
          QueueViewModel.this._filterView.inputtedFilters(), queues);
    }
  }
}
