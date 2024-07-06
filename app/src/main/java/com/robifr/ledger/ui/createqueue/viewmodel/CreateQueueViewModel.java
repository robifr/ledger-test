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
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.repository.QueueRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

@HiltViewModel
public class CreateQueueViewModel extends ViewModel {
  @NonNull protected final QueueRepository _queueRepository;
  @NonNull protected final MakeProductOrderViewModel _makeProductOrderView;
  @NonNull protected final SelectProductOrderViewModel _selectProductOrderView;

  @NonNull
  protected final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  /**
   * Current inputted customer with changes to data like balance or debt as an overview before doing
   * the actual transaction.
   */
  @NonNull
  protected final SafeMutableLiveData<Optional<CustomerModel>> _temporalInputtedCustomer =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull
  protected final SafeMutableLiveData<Optional<CustomerModel>> _inputtedCustomer =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull
  protected final SafeMutableLiveData<ZonedDateTime> _inputtedDate =
      new SafeMutableLiveData<>(ZonedDateTime.now(ZoneId.systemDefault()));

  @NonNull
  protected final SafeMutableLiveData<QueueModel.Status> _inputtedStatus =
      new SafeMutableLiveData<>(QueueModel.Status.IN_QUEUE);

  @NonNull
  protected final SafeMutableLiveData<List<ProductOrderModel>> _inputtedProductOrders =
      new SafeMutableLiveData<>(List.of());

  @NonNull
  protected final SafeMutableLiveData<QueueModel.PaymentMethod> _inputtedPaymentMethod =
      new SafeMutableLiveData<>(QueueModel.PaymentMethod.CASH);

  @NonNull
  protected final SafeMutableLiveData<Set<QueueModel.PaymentMethod>> _allowedPaymentMethods =
      new SafeMutableLiveData<>(Set.of(QueueModel.PaymentMethod.CASH));

  @NonNull
  protected final SafeMutableLiveData<Boolean> _isPaymentMethodsViewVisible =
      new SafeMutableLiveData<>(false);

  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final ProductRepository _productRepository;

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _resultCreatedQueueId =
      new MutableLiveData<>();

  @Inject
  public CreateQueueViewModel(
      @NonNull QueueRepository queueRepository,
      @NonNull CustomerRepository customerRepository,
      @NonNull ProductRepository productRepository) {
    this._queueRepository = Objects.requireNonNull(queueRepository);
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._productRepository = Objects.requireNonNull(productRepository);
    this._makeProductOrderView = new MakeProductOrderViewModel(this);
    this._selectProductOrderView = new SelectProductOrderViewModel(this);
  }

  @NonNull
  public MakeProductOrderViewModel makeProductOrderView() {
    return this._makeProductOrderView;
  }

  @NonNull
  public SelectProductOrderViewModel selectProductOrderView() {
    return this._selectProductOrderView;
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<Optional<CustomerModel>> inputtedCustomer() {
    return this._inputtedCustomer;
  }

  /**
   * @see CreateQueueViewModel#_temporalInputtedCustomer
   */
  @NonNull
  public SafeLiveData<Optional<CustomerModel>> temporalInputtedCustomer() {
    return this._temporalInputtedCustomer;
  }

  @NonNull
  public SafeLiveData<ZonedDateTime> inputtedDate() {
    return this._inputtedDate;
  }

  @NonNull
  public SafeLiveData<QueueModel.Status> inputtedStatus() {
    return this._inputtedStatus;
  }

  @NonNull
  public SafeLiveData<List<ProductOrderModel>> inputtedProductOrders() {
    return this._inputtedProductOrders;
  }

  @NonNull
  public SafeLiveData<QueueModel.PaymentMethod> inputtedPaymentMethod() {
    return this._inputtedPaymentMethod;
  }

  @NonNull
  public SafeLiveData<Set<QueueModel.PaymentMethod>> allowedPaymentMethods() {
    return this._allowedPaymentMethods;
  }

  @NonNull
  public SafeLiveData<Boolean> isPaymentMethodsViewVisible() {
    return this._isPaymentMethodsViewVisible;
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> resultCreatedQueueId() {
    return this._resultCreatedQueueId;
  }

  /**
   * Get current inputted queue from any corresponding inputted live data. If any live data is set
   * using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public QueueModel inputtedQueue() {
    return QueueModel.toBuilder()
        .setStatus(this._inputtedStatus.getValue())
        .setDate(this._inputtedDate.getValue().toInstant())
        .setPaymentMethod(this._inputtedPaymentMethod.getValue())
        .setCustomerId(this._inputtedCustomer.getValue().map(CustomerModel::id).orElse(null))
        .setCustomer(this._inputtedCustomer.getValue().orElse(null))
        .setProductOrders(this._inputtedProductOrders.getValue())
        .build();
  }

  public void onCustomerChanged(@Nullable CustomerModel customer) {
    this._inputtedCustomer.setValue(Optional.ofNullable(customer));
    this._onUpdateAllowedPaymentMethod();
    // Update after allowed payment methods updated, in case payment method changed.
    this._onUpdateTemporalInputtedCustomer();
  }

  public void onDateChanged(@NonNull ZonedDateTime date) {
    Objects.requireNonNull(date);

    this._inputtedDate.setValue(date);
  }

  public void onStatusChanged(@NonNull QueueModel.Status status) {
    Objects.requireNonNull(status);

    // Hide payment methods view when status is other than completed.
    final boolean isPaymentMethodsViewVisible = status == QueueModel.Status.COMPLETED;

    this._inputtedStatus.setValue(status);
    this._isPaymentMethodsViewVisible.setValue(isPaymentMethodsViewVisible);
    this._onUpdateAllowedPaymentMethod();
    // Update after allowed payment methods updated, in case payment method changed.
    this._onUpdateTemporalInputtedCustomer();
  }

  public void onProductOrdersChanged(@NonNull List<ProductOrderModel> productOrders) {
    Objects.requireNonNull(productOrders);

    this._inputtedProductOrders.setValue(Collections.unmodifiableList(productOrders));
    this._onUpdateAllowedPaymentMethod();
    // Update after allowed payment methods updated, in case payment method changed.
    this._onUpdateTemporalInputtedCustomer();
  }

  public void onPaymentMethodChanged(@NonNull QueueModel.PaymentMethod paymentMethod) {
    Objects.requireNonNull(paymentMethod);

    this._inputtedPaymentMethod.setValue(paymentMethod);
    this._onUpdateTemporalInputtedCustomer();
  }

  public void onSave() {
    if (this._inputtedProductOrders.getValue().isEmpty()) {
      this._snackbarMessage.setValue(
          new LiveDataEvent<>(
              new StringResources.Strings(
                  R.string.text_please_to_include_at_least_one_product_order)));
      return;
    }

    this._addQueue(this.inputtedQueue());
  }

  @NonNull
  public LiveData<CustomerModel> selectCustomerById(@Nullable Long customerId) {
    final MutableLiveData<CustomerModel> result = new MutableLiveData<>();

    this._customerRepository
        .selectById(customerId)
        .thenAcceptAsync(
            customer -> {
              if (customer == null) {
                this._snackbarMessage.postValue(
                    new LiveDataEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_failed_to_find_related_customer)));
              }

              result.postValue(customer);
            });
    return result;
  }

  @NonNull
  public LiveData<ProductModel> selectProductById(@Nullable Long productId) {
    final MutableLiveData<ProductModel> result = new MutableLiveData<>();

    this._productRepository
        .selectById(productId)
        .thenAcceptAsync(
            product -> {
              if (product == null) {
                this._snackbarMessage.postValue(
                    new LiveDataEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_failed_to_find_related_product)));
              }

              result.postValue(product);
            });
    return result;
  }

  protected void _onUpdateAllowedPaymentMethod() {
    final HashSet<QueueModel.PaymentMethod> allowedPaymentMethods =
        new HashSet<>(this._allowedPaymentMethods.getValue());
    final boolean isBalanceSufficient =
        this._inputtedCustomer
            .getValue()
            .filter(customer -> customer.isBalanceSufficient(null, this.inputtedQueue()))
            .isPresent();

    if (this._inputtedStatus.getValue() == QueueModel.Status.COMPLETED && isBalanceSufficient) {
      allowedPaymentMethods.add(QueueModel.PaymentMethod.ACCOUNT_BALANCE);
    } else {
      allowedPaymentMethods.remove(QueueModel.PaymentMethod.ACCOUNT_BALANCE);
    }

    this._allowedPaymentMethods.setValue(Collections.unmodifiableSet(allowedPaymentMethods));

    // Change payment method to cash when current selected one marked as not allowed.
    if (!allowedPaymentMethods.contains(this._inputtedPaymentMethod.getValue())) {
      this.onPaymentMethodChanged(QueueModel.PaymentMethod.CASH);
    }
  }

  protected void _onUpdateTemporalInputtedCustomer() {
    final CustomerModel customer =
        this._inputtedCustomer
            .getValue()
            .map(
                model ->
                    CustomerModel.toBuilder(model)
                        .setBalance(model.balanceOnMadePayment(this.inputtedQueue()))
                        .setDebt(model.debtOnMadePayment(this.inputtedQueue()))
                        .build())
            .orElse(null);

    this._temporalInputtedCustomer.setValue(Optional.ofNullable(customer));
  }

  private void _addQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._queueRepository
        .add(queue)
        .thenAcceptAsync(
            id -> {
              if (id != 0L) this._resultCreatedQueueId.postValue(new LiveDataEvent<>(id));

              final StringResources stringRes =
                  id != 0L
                      ? new StringResources.Plurals(R.plurals.args_added_x_queue, 1, 1)
                      : new StringResources.Strings(R.string.text_error_failed_to_add_queue);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
  }
}
