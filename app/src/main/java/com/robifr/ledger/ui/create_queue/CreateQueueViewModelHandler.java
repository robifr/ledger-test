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

package com.robifr.ledger.ui.create_queue;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.create_queue.view_model.CreateQueueViewModel;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CreateQueueViewModelHandler {
  @NonNull protected final CreateQueueFragment _fragment;
  @NonNull protected final CreateQueueViewModel _viewModel;

  public CreateQueueViewModelHandler(
      @NonNull CreateQueueFragment fragment, @NonNull CreateQueueViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .snackbarMessage()
        .observe(this._fragment.requireActivity(), new Observer<>(this::_onSnackbarMessage));
    this._viewModel
        .createdQueueId()
        .observe(this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onCreatedQueueId));

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
        .inputtedPaymentMethod()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedPaymentMethod);
    this._viewModel
        .allowedPaymentMethods()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onAllowedPaymentMethods);
    this._viewModel
        .isPaymentMethodsViewVisible()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onPaymentMethodsViewVisible);

    this._viewModel
        .addedProductOrderIndexes()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onAddedOrderIndexes));
    this._viewModel
        .removedProductOrderIndexes()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onRemovedOrderIndexes));
    this._viewModel
        .updatedProductOrderIndexes()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onUpdatedOrderIndexes));

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

  private void _onSnackbarMessage(@Nullable StringResources stringRes) {
    if (stringRes == null) return;

    Snackbar.make(
            this._fragment.requireView(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onCreatedQueueId(@Nullable Long queueId) {
    if (queueId != null) {
      final Bundle bundle = new Bundle();
      bundle.putLong(CreateQueueFragment.Result.CREATED_QUEUE_ID.key(), queueId);

      this._fragment
          .getParentFragmentManager()
          .setFragmentResult(CreateQueueFragment.Request.CREATE_QUEUE.key(), bundle);
    }

    this._fragment.finish();
  }

  private void _onInputtedCustomer(@Nullable CustomerModel customer) {
    this._fragment.inputCustomer().setInputtedCustomer(customer);

    if (customer != null) {
      final String croppedName =
          customer.name().length() > 12 ? customer.name().substring(0, 12) : customer.name();

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

  private void _onTemporalInputtedCustomer(@Nullable CustomerModel customer) {
    final Long balance = customer != null ? customer.balance() : null;
    final BigDecimal debt = customer != null ? customer.debt() : null;

    this._fragment.inputProductOrder().setCustomerBalanceAfterPayment(balance);
    this._fragment.inputProductOrder().setCustomerDebtAfterPayment(debt);
  }

  private void _onInputtedDate(@Nullable ZonedDateTime date) {
    if (date != null) this._fragment.inputDate().setInputtedDate(date);
  }

  private void _onInputtedStatus(@Nullable QueueModel.Status status) {
    if (status != null) this._fragment.inputStatus().setInputtedStatus(status);
  }

  private void _onAddedOrderIndexes(@Nullable List<Integer> indexes) {
    if (indexes == null) return;

    this._fragment
        .inputProductOrder()
        .notifyProductOrderAdded(indexes.stream().mapToInt(Integer::intValue).toArray());
    this._fragment
        .inputProductOrder()
        .setGrandTotalPrice(this._viewModel.inputtedQueue().grandTotalPrice());
    this._fragment
        .inputProductOrder()
        .setTotalDiscount(this._viewModel.inputtedQueue().totalDiscount());
  }

  private void _onRemovedOrderIndexes(@Nullable List<Integer> indexes) {
    if (indexes == null) return;

    this._fragment
        .inputProductOrder()
        .notifyProductOrderRemoved(indexes.stream().mapToInt(Integer::intValue).toArray());
    this._fragment
        .inputProductOrder()
        .setGrandTotalPrice(this._viewModel.inputtedQueue().grandTotalPrice());
    this._fragment
        .inputProductOrder()
        .setTotalDiscount(this._viewModel.inputtedQueue().totalDiscount());
  }

  private void _onUpdatedOrderIndexes(@Nullable List<Integer> indexes) {
    if (indexes == null) return;

    this._fragment
        .inputProductOrder()
        .notifyProductOrderUpdated(indexes.stream().mapToInt(Integer::intValue).toArray());
    this._fragment
        .inputProductOrder()
        .setGrandTotalPrice(this._viewModel.inputtedQueue().grandTotalPrice());
    this._fragment
        .inputProductOrder()
        .setTotalDiscount(this._viewModel.inputtedQueue().totalDiscount());
  }

  private void _onInputtedPaymentMethod(@Nullable QueueModel.PaymentMethod paymentMethod) {
    if (paymentMethod != null) {
      this._fragment.inputPaymentMethod().setInputtedPaymentMethod(paymentMethod);
    }
  }

  private void _onAllowedPaymentMethods(@Nullable Set<QueueModel.PaymentMethod> paymentMethods) {
    if (paymentMethods != null) {
      this._fragment.inputPaymentMethod().setEnabledButtons(paymentMethods);
    }
  }

  private void _onPaymentMethodsViewVisible(@Nullable Boolean isVisible) {
    if (isVisible != null) this._fragment.inputPaymentMethod().setVisible(isVisible);
  }

  private void _onMakeOrderProduct(@Nullable ProductModel product) {
    this._fragment.inputProductOrder().makeProductOrder().setInputtedProduct(product);
  }

  private void _onMakeOrderQuantityText(@Nullable String quantity) {
    this._fragment.inputProductOrder().makeProductOrder().setInputtedQuantityText(quantity);
  }

  private void _onMakeOrderDiscountText(@Nullable String discount) {
    this._fragment.inputProductOrder().makeProductOrder().setInputtedDiscountText(discount);
  }

  private void _onMakeOrderTotalPrice(@Nullable BigDecimal totalPrice) {
    if (totalPrice != null) {
      this._fragment.inputProductOrder().makeProductOrder().setInputtedTotalPrice(totalPrice);
    }
  }

  private void _onSelectOrderContextualModeActive(@Nullable Boolean isActive) {
    if (isActive == null) return;

    this._fragment.inputProductOrder().setContextualMode(isActive);
    // Disable every possible irrelevant action when contextual mode is on.
    this._fragment.fragmentBinding().customer.setEnabled(!isActive);
    this._fragment.fragmentBinding().customerLayout.setEndIconVisible(!isActive);
    this._fragment.fragmentBinding().date.setEnabled(!isActive);
    this._fragment.fragmentBinding().status.setEnabled(!isActive);
    this._fragment.fragmentBinding().productOrder.addButton.setEnabled(!isActive);

    final Set<QueueModel.PaymentMethod> allowedPayments =
        this._viewModel.allowedPaymentMethods().getValue();

    // Don't set it via view model as doing so will make the actual allowed payments lost.
    if (!isActive && allowedPayments != null) {
      this._fragment.inputPaymentMethod().setEnabledButtons(allowedPayments);
    } else {
      this._fragment.inputPaymentMethod().setEnabledButtons(Set.of());
    }
  }

  private void _onSelectOrderSelectedIndexes(@Nullable Set<Integer> selectedIndexes) {
    if (selectedIndexes != null) {
      this._fragment.inputProductOrder().setSelectedProductOrderByIndexes(selectedIndexes);
    }
  }
}
