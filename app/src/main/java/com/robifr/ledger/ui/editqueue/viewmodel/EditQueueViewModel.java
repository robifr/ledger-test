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

package com.robifr.ledger.ui.editqueue.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.repository.QueueRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createqueue.viewmodel.CreateQueueViewModel;
import com.robifr.ledger.ui.editqueue.EditQueueFragment;
import com.robifr.ledger.util.livedata.SafeEvent;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class EditQueueViewModel extends CreateQueueViewModel {
  @Nullable private QueueModel _initialQueueToEdit = null;

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultEditedQueueId =
      new MutableLiveData<>();

  @Inject
  public EditQueueViewModel(
      @NonNull QueueRepository queueRepository,
      @NonNull CustomerRepository customerRepository,
      @NonNull ProductRepository productRepository,
      @NonNull SavedStateHandle savedStateHandle) {
    super(queueRepository, customerRepository, productRepository);
    Objects.requireNonNull(savedStateHandle);

    SafeEvent.observeOnce(
        // Shouldn't be null when editing data.
        this.selectQueueById(
            Objects.requireNonNull(
                savedStateHandle.get(EditQueueFragment.Arguments.INITIAL_QUEUE_ID_TO_EDIT.key()))),
        queue -> {
          this._initialQueueToEdit = queue;
          this.onCustomerChanged(queue.customer());
          this.onDateChanged(queue.date().atZone(ZoneId.systemDefault()));
          this.onStatusChanged(queue.status());
          this.onPaymentMethodChanged(queue.paymentMethod());
          this.onProductOrdersChanged(queue.productOrders());
        },
        Objects::nonNull);
  }

  @Override
  @NonNull
  public QueueModel inputtedQueue() {
    final Long id =
        this._initialQueueToEdit != null && this._initialQueueToEdit.id() != null
            ? this._initialQueueToEdit.id()
            : null;
    return QueueModel.toBuilder(super.inputtedQueue()).setId(id).build();
  }

  @Override
  public void onSave() {
    final QueueModel inputtedQueue = this.inputtedQueue();

    if (inputtedQueue.productOrders().isEmpty()) {
      this._snackbarMessage.setValue(
          new SafeEvent<>(
              new StringResources.Strings(
                  R.string.text_please_to_include_at_least_one_product_order)));
      return;
    }

    this._updateQueue(inputtedQueue);
  }

  @NonNull
  public LiveData<SafeEvent<Optional<Long>>> resultEditedQueueId() {
    return this._resultEditedQueueId;
  }

  @NonNull
  public LiveData<QueueModel> selectQueueById(@Nullable Long queueId) {
    final MutableLiveData<QueueModel> result = new MutableLiveData<>();

    this._queueRepository
        .selectById(queueId)
        .thenAcceptAsync(
            queue -> {
              if (queue == null) {
                this._snackbarMessage.postValue(
                    new SafeEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_failed_to_find_related_queue)));
              }

              result.postValue(queue);
            });
    return result;
  }

  @Override
  protected void _onUpdateAllowedPaymentMethod() {
    final QueueModel inputtedQueue = this.inputtedQueue();
    final HashSet<QueueModel.PaymentMethod> allowedPaymentMethods =
        new HashSet<>(this._allowedPaymentMethods.getValue());
    final boolean isBalanceSufficient =
        this._inputtedCustomer
            .getValue()
            .filter(
                customer ->
                    this._initialQueueToEdit != null
                        && inputtedQueue.status() == QueueModel.Status.COMPLETED
                        && customer.isBalanceSufficient(this._initialQueueToEdit, inputtedQueue))
            .isPresent();
    final boolean isTemporalBalancePositive =
        // Compare with the account balance payment option
        // as if the user does it before they actually do.
        this._inputtedCustomer
            .getValue()
            .filter(
                customer ->
                    this._initialQueueToEdit != null
                        && customer.balanceOnUpdatedPayment(
                                this._initialQueueToEdit,
                                QueueModel.toBuilder(inputtedQueue)
                                    .setPaymentMethod(QueueModel.PaymentMethod.ACCOUNT_BALANCE)
                                    .build())
                            >= 0L)
            .isPresent();

    if (isBalanceSufficient && isTemporalBalancePositive) {
      allowedPaymentMethods.add(QueueModel.PaymentMethod.ACCOUNT_BALANCE);
    } else {
      allowedPaymentMethods.remove(QueueModel.PaymentMethod.ACCOUNT_BALANCE);
    }

    this._allowedPaymentMethods.setValue(Collections.unmodifiableSet(allowedPaymentMethods));

    // Change payment method to cash when current selected one marked as not allowed.
    if (!allowedPaymentMethods.contains(inputtedQueue.paymentMethod())) {
      this.onPaymentMethodChanged(QueueModel.PaymentMethod.CASH);
    }
  }

  @Override
  protected void _onUpdateTemporalInputtedCustomer() {
    final QueueModel inputtedQueue = this.inputtedQueue();
    final CustomerModel customer =
        this._inputtedCustomer
            .getValue()
            .filter(model -> this._initialQueueToEdit != null)
            .map(
                model ->
                    CustomerModel.toBuilder(model)
                        .setBalance(
                            model.balanceOnUpdatedPayment(this._initialQueueToEdit, inputtedQueue))
                        .setDebt(
                            model.debtOnUpdatedPayment(this._initialQueueToEdit, inputtedQueue))
                        .build())
            .orElse(null);

    this._temporalInputtedCustomer.setValue(Optional.ofNullable(customer));
  }

  private void _updateQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._queueRepository
        .update(queue)
        .thenAcceptAsync(
            effected -> {
              if (effected > 0) {
                this._resultEditedQueueId.postValue(
                    new SafeEvent<>(Optional.ofNullable(queue.id())));
              }

              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_updated_x_queue, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_update_queue);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }
}
