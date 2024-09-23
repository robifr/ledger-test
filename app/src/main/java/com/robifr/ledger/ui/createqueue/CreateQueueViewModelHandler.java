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

package com.robifr.ledger.ui.createqueue;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createqueue.viewmodel.CreateQueueViewModel;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CreateQueueViewModelHandler {
  @NonNull protected final CreateQueueFragment _fragment;
  @NonNull protected final CreateQueueViewModel _viewModel;

  public CreateQueueViewModelHandler(
      @NonNull CreateQueueFragment fragment, @NonNull CreateQueueViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .resultCreatedQueueId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultCreatedQueueId));
    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));

    this._viewModel
        .inputtedCustomer()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedCustomer);
    this._viewModel
        .temporalInputtedCustomer()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onTemporalInputtedCustomer);

    this._viewModel
        .inputtedDate()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedDate);
    this._viewModel
        .inputtedStatus()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedStatus);
    this._viewModel
        .inputtedProductOrders()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onProductOrders);

    this._viewModel
        .inputtedPaymentMethod()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedPaymentMethod);
    this._viewModel
        .allowedPaymentMethods()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onAllowedPaymentMethods);
    this._viewModel
        .isPaymentMethodsViewVisible()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onPaymentMethodsViewVisible);

    this._viewModel
        .makeProductOrderView()
        .inputtedProduct()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onMakeOrderProduct);
    this._viewModel
        .makeProductOrderView()
        .inputtedQuantityText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onMakeOrderQuantityText);
    this._viewModel
        .makeProductOrderView()
        .inputtedDiscountText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onMakeOrderDiscountText);
    this._viewModel
        .makeProductOrderView()
        .inputtedTotalPrice()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onMakeOrderTotalPrice);

    this._viewModel
        .selectProductOrderView()
        .isContextualModeActive()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSelectOrderContextualModeActive);
    this._viewModel
        .selectProductOrderView()
        .selectedIndexes()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSelectOrderSelectedIndexes);
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultCreatedQueueId(@NonNull Optional<Long> queueId) {
    Objects.requireNonNull(queueId);

    queueId.ifPresent(
        id -> {
          final Bundle bundle = new Bundle();
          bundle.putLong(CreateQueueFragment.Result.CREATED_QUEUE_ID_LONG.key(), id);

          this._fragment
              .getParentFragmentManager()
              .setFragmentResult(CreateQueueFragment.Request.CREATE_QUEUE.key(), bundle);
        });
    this._fragment.finish();
  }

  private void _onSnackbarMessage(@NonNull StringResources stringRes) {
    Objects.requireNonNull(stringRes);

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onInputtedCustomer(@NonNull Optional<CustomerModel> customer) {
    Objects.requireNonNull(customer);

    this._fragment.inputCustomer().setInputtedCustomer(customer.orElse(null));

    if (customer.isPresent()) {
      final String croppedName =
          customer.get().name().length() > 12
              ? customer.get().name().substring(0, 12)
              : customer.get().name();

      this._fragment
          .inputProductOrder()
          .setCustomerBalanceAfterPaymentTitle(
              this._fragment.getString(
                  R.string.productordercard_customerbalance_title, croppedName));
      this._fragment
          .inputProductOrder()
          .setCustomerDebtAfterPaymentTitle(
              this._fragment.getString(R.string.productordercard_customerdebt_title, croppedName));

    } else {
      this._fragment.inputProductOrder().setCustomerBalanceAfterPaymentTitle(null);
      this._fragment.inputProductOrder().setCustomerDebtAfterPaymentTitle(null);
    }
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onTemporalInputtedCustomer(@NonNull Optional<CustomerModel> customer) {
    Objects.requireNonNull(customer);

    this._fragment
        .inputProductOrder()
        .setCustomerBalanceAfterPayment(customer.map(CustomerModel::balance).orElse(null));
    this._fragment
        .inputProductOrder()
        .setCustomerDebtAfterPayment(customer.map(CustomerModel::debt).orElse(null));
  }

  private void _onInputtedDate(@NonNull ZonedDateTime date) {
    Objects.requireNonNull(date);

    this._fragment.inputDate().setInputtedDate(date);
  }

  private void _onInputtedStatus(@NonNull QueueModel.Status status) {
    Objects.requireNonNull(status);

    this._fragment.inputStatus().setInputtedStatus(status);
  }

  private void _onProductOrders(@NonNull List<ProductOrderModel> productOrders) {
    Objects.requireNonNull(productOrders);

    this._fragment.inputProductOrder().setInputtedProductOrders(productOrders);
    this._fragment
        .inputProductOrder()
        .setTotalDiscount(this._viewModel.inputtedQueue().totalDiscount());
    this._fragment
        .inputProductOrder()
        .setGrandTotalPrice(this._viewModel.inputtedQueue().grandTotalPrice());
  }

  private void _onInputtedPaymentMethod(@NonNull QueueModel.PaymentMethod paymentMethod) {
    Objects.requireNonNull(paymentMethod);

    this._fragment.inputPaymentMethod().setInputtedPaymentMethod(paymentMethod);
  }

  private void _onAllowedPaymentMethods(@NonNull Set<QueueModel.PaymentMethod> paymentMethods) {
    Objects.requireNonNull(paymentMethods);

    this._fragment.inputPaymentMethod().setEnabledButtons(paymentMethods);
  }

  private void _onPaymentMethodsViewVisible(boolean isVisible) {
    this._fragment.inputPaymentMethod().setVisible(isVisible);
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onMakeOrderProduct(@NonNull Optional<ProductModel> product) {
    Objects.requireNonNull(product);

    this._fragment.inputProductOrder().makeProductOrder().setInputtedProduct(product.orElse(null));
  }

  private void _onMakeOrderQuantityText(@NonNull String quantity) {
    Objects.requireNonNull(quantity);

    this._fragment.inputProductOrder().makeProductOrder().setInputtedQuantityText(quantity);
  }

  private void _onMakeOrderDiscountText(@NonNull String discount) {
    Objects.requireNonNull(discount);

    this._fragment.inputProductOrder().makeProductOrder().setInputtedDiscountText(discount);
  }

  private void _onMakeOrderTotalPrice(@NonNull BigDecimal totalPrice) {
    Objects.requireNonNull(totalPrice);

    this._fragment.inputProductOrder().makeProductOrder().setInputtedTotalPrice(totalPrice);
  }

  private void _onSelectOrderContextualModeActive(boolean isActive) {
    this._fragment.inputProductOrder().setContextualMode(isActive);
    // Disable every possible irrelevant action when contextual mode is on.
    this._fragment.fragmentBinding().customer.setEnabled(!isActive);
    this._fragment.fragmentBinding().customerLayout.setEnabled(!isActive);
    this._fragment.fragmentBinding().date.setEnabled(!isActive);
    this._fragment.fragmentBinding().status.setEnabled(!isActive);
    this._fragment.fragmentBinding().productOrder.addButton.setEnabled(!isActive);

    // Don't set it via view model as doing so will make the actual allowed payments lost.
    if (!isActive) {
      this._fragment
          .inputPaymentMethod()
          .setEnabledButtons(this._viewModel.allowedPaymentMethods().getValue());
    } else {
      this._fragment.inputPaymentMethod().setEnabledButtons(Set.of());
    }
  }

  private void _onSelectOrderSelectedIndexes(@NonNull Set<Integer> selectedIndexes) {
    Objects.requireNonNull(selectedIndexes);

    this._fragment.inputProductOrder().setSelectedProductOrderByIndexes(selectedIndexes);
  }
}
