/**
 * Copyright (c) 2022-present Robi
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

package com.robifr.ledger.ui.edit_queue.view_model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.repository.QueueRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.create_queue.view_model.CreateQueueViewModel;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class EditQueueViewModel extends CreateQueueViewModel {
  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _editedQueueId = new MutableLiveData<>();

  @Nullable private QueueModel _initialQueueToEdit = null;

  public EditQueueViewModel(
      @NonNull QueueRepository queueRepository,
      @NonNull CustomerRepository customerRepository,
      @NonNull ProductRepository productRepository) {
    super(queueRepository, customerRepository, productRepository);
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
    if (this._inputtedProductOrders.size() == 0) {
      this._snackbarMessage.setValue(
          new LiveDataEvent<>("Required to have at least one product order!"));
      return;
    }

    this._updateQueue(this.inputtedQueue());
  }

  public void setInitialQueueToEdit(@NonNull QueueModel queue) {
    this._initialQueueToEdit = Objects.requireNonNull(queue);
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> editedQueueId() {
    return this._editedQueueId;
  }

  @Nullable
  public QueueModel selectQueueById(@Nullable Long queueId) {
    final LiveDataEvent<String> notFoundError =
        new LiveDataEvent<>("Error! Unable to obtain queue with ID " + queueId);
    QueueModel queue = null;

    try {
      queue = this._queueRepository.selectById(queueId).get();
      if (queue == null) this._snackbarMessage.setValue(notFoundError);

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(notFoundError);
    }

    return queue;
  }

  @Override
  protected void _onUpdateAllowedPaymentMethod() {
    final QueueModel inputtedQueue = this.inputtedQueue();
    final HashSet<QueueModel.PaymentMethod> allowedPaymentMethods =
        this._allowedPaymentMethods.getValue() != null
            ? new HashSet<>(this._allowedPaymentMethods.getValue())
            : new HashSet<>(Set.of(QueueModel.PaymentMethod.CASH));

    final boolean isBalanceEnoughToPay =
        inputtedQueue.customer() != null
            && BigDecimal.valueOf(inputtedQueue.customer().balance())
                    .subtract(inputtedQueue.grandTotalPrice())
                    .compareTo(BigDecimal.ZERO)
                >= 0;
    final boolean isCustomerAndOrdersUnchanged =
        this._initialQueueToEdit != null
            && this._initialQueueToEdit.productOrders().equals(inputtedQueue.productOrders())
            && this._initialQueueToEdit.customerId() != null
            && this._initialQueueToEdit.customerId().equals(inputtedQueue.customerId());

    if (inputtedQueue.status() == QueueModel.Status.COMPLETED
        && (isBalanceEnoughToPay || isCustomerAndOrdersUnchanged)) {
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
    Objects.requireNonNull(this._initialQueueToEdit);

    final QueueModel inputtedQueue = this.inputtedQueue();
    final CustomerModel customer =
        inputtedQueue.customer() != null
            ? CustomerModel.toBuilder(inputtedQueue.customer())
                .setBalance(
                    inputtedQueue
                        .customer()
                        .balanceOnUpdatedPayment(this._initialQueueToEdit, inputtedQueue))
                .setDebt(
                    inputtedQueue
                        .customer()
                        .debtOnUpdatedPayment(this._initialQueueToEdit, inputtedQueue))
                .build()
            : null;

    this._temporalInputtedCustomer.setValue(customer);
  }

  private void _updateQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._queueRepository
        .update(queue)
        .thenAcceptAsync(
            effected -> {
              if (effected > 0) this._editedQueueId.postValue(new LiveDataEvent<>(queue.id()));

              this._snackbarMessage.postValue(
                  new LiveDataEvent<>("Updated " + effected + " queue(s)"));
            });
  }

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Context _context;

    public Factory(@NonNull Context context) {
      Objects.requireNonNull(context);

      this._context = context.getApplicationContext();
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> cls) {
      Objects.requireNonNull(cls);

      final EditQueueViewModel viewModel =
          new EditQueueViewModel(
              QueueRepository.instance(this._context),
              CustomerRepository.instance(this._context),
              ProductRepository.instance(this._context));
      return Objects.requireNonNull(cls.cast(viewModel));
    }
  }
}
