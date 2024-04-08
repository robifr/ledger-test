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

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.QueueSortMethod;
import com.robifr.ledger.data.QueueSorter;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.ModelChangedListener;
import com.robifr.ledger.repository.QueueRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.LiveDataModelUpdater;
import com.robifr.ledger.ui.StringResources;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class QueueViewModel extends ViewModel {
  @NonNull private final QueueRepository _queueRepository;
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final QueuesUpdater _queuesUpdater;
  @NonNull private final CustomerUpdater _customerUpdater = new CustomerUpdater();
  @NonNull private final QueueFilterViewModel _filterView = new QueueFilterViewModel(this);
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
    this._queuesUpdater = new QueuesUpdater(this._queues);

    this._queueRepository.addModelChangedListener(this._queuesUpdater);
    this._customerRepository.addModelChangedListener(this._customerUpdater);
  }

  @Override
  public void onCleared() {
    this._queueRepository.removeModelChangedListener(this._queuesUpdater);
    this._customerRepository.removeModelChangedListener(this._customerUpdater);
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
        .thenAccept(
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

  private class QueuesUpdater extends LiveDataModelUpdater<QueueModel> {
    public QueuesUpdater(@NonNull MutableLiveData<List<QueueModel>> queues) {
      super(queues);
    }

    @Override
    @MainThread
    public void onUpdateLiveData(@NonNull List<QueueModel> queues) {
      QueueViewModel.this._filterView.onFiltersChanged(
          QueueViewModel.this._filterView.inputtedFilters(), queues);
    }
  }

  private class CustomerUpdater implements ModelChangedListener<CustomerModel> {
    @Override
    @WorkerThread
    public void onModelAdded(@NonNull List<CustomerModel> customers) {}

    @Override
    @WorkerThread
    public void onModelUpdated(@NonNull List<CustomerModel> customers) {
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                final ArrayList<QueueModel> queues =
                    QueueViewModel.this._queues.getValue() != null
                        ? new ArrayList<>(QueueViewModel.this._queues.getValue())
                        : new ArrayList<>();

                for (CustomerModel customer : customers) {
                  for (int i = 0; i < queues.size(); i++) {
                    final QueueModel queue = queues.get(i);

                    // When customer updated, apply those changes into the queue model.
                    if (queue.customerId() != null && queue.customerId().equals(customer.id())) {
                      final QueueModel updatedQueue =
                          QueueModel.toBuilder(queue)
                              .setCustomerId(customer.id())
                              .setCustomer(customer)
                              .build();
                      queues.set(i, updatedQueue);
                    }
                  }
                }

                QueueViewModel.this._filterView.onFiltersChanged(
                    QueueViewModel.this._filterView.inputtedFilters(), queues);
              });
    }

    @Override
    @WorkerThread
    public void onModelDeleted(@NonNull List<CustomerModel> customers) {
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                final ArrayList<QueueModel> queues =
                    QueueViewModel.this._queues.getValue() != null
                        ? new ArrayList<>(QueueViewModel.this._queues.getValue())
                        : new ArrayList<>();

                for (CustomerModel customer : customers) {
                  for (int i = 0; i < queues.size(); i++) {
                    final QueueModel queue = queues.get(i);

                    // When customer deleted, remove them from the queue model.
                    if (queue.customerId() != null && queue.customerId().equals(customer.id())) {
                      final QueueModel updatedQueue =
                          QueueModel.toBuilder(queue).setCustomerId(null).setCustomer(null).build();
                      queues.set(i, updatedQueue);
                    }
                  }
                }

                QueueViewModel.this._filterView.onFiltersChanged(
                    QueueViewModel.this._filterView.inputtedFilters(), queues);
              });
    }

    @Override
    @WorkerThread
    public void onModelUpserted(@NonNull List<CustomerModel> customers) {
      // Only when customer updated, apply those changes into the queue model
      // and ignore for any inserted customer.
      this.onModelUpdated(customers);
    }
  }
}
