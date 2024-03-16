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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
  protected final MutableLiveData<CustomerModel> _temporalInputtedCustomer =
      new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<CustomerModel> _inputtedCustomer = new MutableLiveData<>();

  @NonNull protected final MutableLiveData<ZonedDateTime> _inputtedDate = new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<QueueModel.Status> _inputtedStatus = new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<QueueModel.PaymentMethod> _inputtedPaymentMethod =
      new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<Set<QueueModel.PaymentMethod>> _allowedPaymentMethods =
      new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<Boolean> _isPaymentMethodsViewVisible = new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<LiveDataEvent<List<Integer>>> _addedProductOrderIndexes =
      new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<LiveDataEvent<List<Integer>>> _removedProductOrderIndexes =
      new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<LiveDataEvent<List<Integer>>> _updatedProductOrderIndexes =
      new MutableLiveData<>();

  @NonNull protected final ArrayList<ProductOrderModel> _inputtedProductOrders = new ArrayList<>();

  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final ProductRepository _productRepository;

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _createdQueueId = new MutableLiveData<>();

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
  public LiveData<LiveDataEvent<Long>> createdQueueId() {
    return this._createdQueueId;
  }

  @NonNull
  public LiveData<CustomerModel> inputtedCustomer() {
    return this._inputtedCustomer;
  }

  /**
   * @see CreateQueueViewModel#_temporalInputtedCustomer
   */
  @NonNull
  public LiveData<CustomerModel> temporalInputtedCustomer() {
    return this._temporalInputtedCustomer;
  }

  @NonNull
  public LiveData<ZonedDateTime> inputtedDate() {
    return this._inputtedDate;
  }

  @NonNull
  public LiveData<QueueModel.Status> inputtedStatus() {
    return this._inputtedStatus;
  }

  @NonNull
  public LiveData<QueueModel.PaymentMethod> inputtedPaymentMethod() {
    return this._inputtedPaymentMethod;
  }

  @NonNull
  public LiveData<Set<QueueModel.PaymentMethod>> allowedPaymentMethods() {
    return this._allowedPaymentMethods;
  }

  public void setAllowedPaymentMethods(@NonNull Set<QueueModel.PaymentMethod> paymentMethods) {
    Objects.requireNonNull(paymentMethods);

    this._allowedPaymentMethods.setValue(Collections.unmodifiableSet(paymentMethods));
  }

  @NonNull
  public LiveData<Boolean> isPaymentMethodsViewVisible() {
    return this._isPaymentMethodsViewVisible;
  }

  @NonNull
  public LiveData<LiveDataEvent<List<Integer>>> addedProductOrderIndexes() {
    return this._addedProductOrderIndexes;
  }

  @NonNull
  public LiveData<LiveDataEvent<List<Integer>>> removedProductOrderIndexes() {
    return this._removedProductOrderIndexes;
  }

  @NonNull
  public LiveData<LiveDataEvent<List<Integer>>> updatedProductOrderIndexes() {
    return this._updatedProductOrderIndexes;
  }

  @NonNull
  public List<ProductOrderModel> inputtedProductOrders() {
    return Collections.unmodifiableList(this._inputtedProductOrders);
  }

  /**
   * Get current inputted queue from any corresponding inputted live data. If any live data is set
   * using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public QueueModel inputtedQueue() {
    final QueueModel defaultQueue =
        QueueModel.toBuilder()
            .setStatus(QueueModel.Status.IN_QUEUE)
            .setDate(Instant.now())
            .setPaymentMethod(QueueModel.PaymentMethod.CASH)
            .build();

    final QueueModel.Status status =
        Objects.requireNonNullElse(this._inputtedStatus.getValue(), defaultQueue.status());
    final ZonedDateTime date =
        Objects.requireNonNullElse(
            this._inputtedDate.getValue(), defaultQueue.date().atZone(ZoneId.systemDefault()));
    final QueueModel.PaymentMethod paymentMethod =
        Objects.requireNonNullElse(
            this._inputtedPaymentMethod.getValue(), defaultQueue.paymentMethod());
    final Long customerId =
        this._inputtedCustomer.getValue() != null && this._inputtedCustomer.getValue().id() != null
            ? this._inputtedCustomer.getValue().id()
            : defaultQueue.customerId();

    return QueueModel.toBuilder()
        .setStatus(status)
        .setDate(date.toInstant())
        .setPaymentMethod(paymentMethod)
        .setCustomerId(customerId)
        .setCustomer(this._inputtedCustomer.getValue())
        .setProductOrders(this._inputtedProductOrders)
        .build();
  }

  public void onCustomerChanged(@Nullable CustomerModel customer) {
    this._inputtedCustomer.setValue(customer);
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

  public void onPaymentMethodChanged(@NonNull QueueModel.PaymentMethod paymentMethod) {
    Objects.requireNonNull(paymentMethod);

    this._inputtedPaymentMethod.setValue(paymentMethod);
    this._onUpdateTemporalInputtedCustomer();
  }

  public void onAddProductOrder(@NonNull ProductOrderModel... productOrder) {
    Objects.requireNonNull(productOrder);

    final List<Integer> addedIndexes =
        IntStream.rangeClosed(
                this._inputtedProductOrders.size(),
                this._inputtedProductOrders.size() + productOrder.length - 1)
            .boxed()
            .collect(Collectors.toList());

    this._inputtedProductOrders.addAll(List.of(productOrder));
    this._addedProductOrderIndexes.setValue(new LiveDataEvent<>(addedIndexes));
    this._onUpdateAllowedPaymentMethod();
    // Update after allowed payment methods updated, in case payment method changed.
    this._onUpdateTemporalInputtedCustomer();
  }

  public void onRemoveProductOrder(@NonNull ProductOrderModel... productOrder) {
    Objects.requireNonNull(productOrder);

    final ArrayList<Integer> removedIndexes = new ArrayList<>();

    for (int i = productOrder.length; i-- > 0; ) {
      removedIndexes.add(this._inputtedProductOrders.indexOf(productOrder[i]));
      this._inputtedProductOrders.remove(productOrder[i]);
    }

    this._removedProductOrderIndexes.setValue(new LiveDataEvent<>(removedIndexes));
    this._onUpdateAllowedPaymentMethod();
    // Update after allowed payment methods updated, in case payment method changed.
    this._onUpdateTemporalInputtedCustomer();
  }

  public void onUpdateProductOrder(int index, @NonNull ProductOrderModel newProductOrder) {
    Objects.requireNonNull(newProductOrder);

    this._inputtedProductOrders.set(index, newProductOrder);
    this._updatedProductOrderIndexes.setValue(new LiveDataEvent<>(List.of(index)));
    this._onUpdateAllowedPaymentMethod();
    // Update after allowed payment methods updated, in case payment method changed.
    this._onUpdateTemporalInputtedCustomer();
  }

  public void onSave() {
    if (this._inputtedProductOrders.size() == 0) {
      this._snackbarMessage.setValue(
          new LiveDataEvent<>(
              new StringResources.Strings(
                  R.string.text_please_to_include_at_least_one_product_order)));
      return;
    }

    this._addQueue(this.inputtedQueue());
  }

  @Nullable
  public CustomerModel selectCustomerById(@Nullable Long customerId) {
    final StringResources notFoundRes =
        new StringResources.Strings(R.string.text_error_failed_to_find_related_customer);
    CustomerModel customer = null;

    try {
      customer = this._customerRepository.selectById(customerId).get();
      if (customer == null) this._snackbarMessage.setValue(new LiveDataEvent<>(notFoundRes));

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(new LiveDataEvent<>(notFoundRes));
    }

    return customer;
  }

  @Nullable
  public ProductModel selectProductById(@Nullable Long productId) {
    final StringResources notFoundRes =
        new StringResources.Strings(R.string.text_error_failed_to_find_related_product);
    ProductModel product = null;

    try {
      product = this._productRepository.selectById(productId).get();
      if (product == null) this._snackbarMessage.setValue(new LiveDataEvent<>(notFoundRes));

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(new LiveDataEvent<>(notFoundRes));
    }

    return product;
  }

  protected void _onUpdateAllowedPaymentMethod() {
    final QueueModel inputtedQueue = this.inputtedQueue();
    final HashSet<QueueModel.PaymentMethod> allowedPaymentMethods =
        this._allowedPaymentMethods.getValue() != null
            ? new HashSet<>(this._allowedPaymentMethods.getValue())
            : new HashSet<>(Set.of(QueueModel.PaymentMethod.CASH));

    if (inputtedQueue.status() == QueueModel.Status.COMPLETED
        && inputtedQueue.customer() != null
        && inputtedQueue.customer().isBalanceSufficient(null, inputtedQueue)) {
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

  protected void _onUpdateTemporalInputtedCustomer() {
    final QueueModel inputtedQueue = this.inputtedQueue();
    final CustomerModel customer =
        inputtedQueue.customer() != null
            ? CustomerModel.toBuilder(inputtedQueue.customer())
                .setBalance(inputtedQueue.customer().balanceOnMadePayment(inputtedQueue))
                .setDebt(inputtedQueue.customer().debtOnMadePayment(inputtedQueue))
                .build()
            : null;

    this._temporalInputtedCustomer.setValue(customer);
  }

  private void _addQueue(@NonNull QueueModel queue) {
    Objects.requireNonNull(queue);

    this._queueRepository
        .add(queue)
        .thenAcceptAsync(
            id -> {
              if (id != 0L) this._createdQueueId.postValue(new LiveDataEvent<>(id));

              final StringResources stringRes =
                  id != 0L
                      ? new StringResources.Plurals(R.plurals.args_added_x_queue, 1, 1)
                      : new StringResources.Strings(R.string.text_error_failed_to_add_queue);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
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

      final CreateQueueViewModel viewModel =
          new CreateQueueViewModel(
              QueueRepository.instance(this._context),
              CustomerRepository.instance(this._context),
              ProductRepository.instance(this._context));
      return Objects.requireNonNull(cls.cast(viewModel));
    }
  }
}
