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

package com.robifr.ledger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import androidx.annotation.NonNull;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CustomerModelTest {
  @NonNull
  private final CustomerModel _customer =
      CustomerModel.toBuilder()
          .setName("Amy")
          .setId(1L)
          .setBalance(10_000L)
          .setDebt(BigDecimal.ZERO)
          .build();

  @NonNull
  private final ProductOrderModel _order =
      ProductOrderModel.toBuilder()
          .setId(1L)
          .setQueueId(1L)
          .setProductId(1L)
          .setProductName("Apple")
          .setProductPrice(1000L)
          .setQuantity(1.0)
          .setDiscount(0L)
          .setTotalPrice(BigDecimal.valueOf(1000L))
          .build();

  @NonNull
  private final QueueModel _queue =
      QueueModel.toBuilder()
          .setStatus(QueueModel.Status.IN_QUEUE)
          .setDate(Instant.now())
          .setPaymentMethod(QueueModel.PaymentMethod.CASH)
          .setId(1L)
          .setCustomerId(1L)
          .setCustomer(this._customer)
          .setProductOrders(List.of(this._order))
          .build();

  @Test
  public void balanceOnPayment() {
    final QueueModel completedQueue_accountBalance =
        QueueModel.toBuilder(this._queue)
            .setStatus(QueueModel.Status.COMPLETED)
            .setPaymentMethod(QueueModel.PaymentMethod.ACCOUNT_BALANCE)
            .build();
    final QueueModel completedQueue_cash =
        QueueModel.toBuilder(this._queue)
            .setStatus(QueueModel.Status.COMPLETED)
            .setPaymentMethod(QueueModel.PaymentMethod.CASH)
            .build();
    final QueueModel uncompletedQueue_accountBalance =
        QueueModel.toBuilder(this._queue)
            .setStatus(QueueModel.Status.IN_QUEUE)
            .setPaymentMethod(QueueModel.PaymentMethod.ACCOUNT_BALANCE)
            .build();
    final QueueModel uncompletedQueue_cash =
        QueueModel.toBuilder(this._queue)
            .setStatus(QueueModel.Status.IN_QUEUE)
            .setPaymentMethod(QueueModel.PaymentMethod.CASH)
            .build();

    final CustomerModel lowBalanceCustomer =
        CustomerModel.toBuilder(this._customer).setBalance(500L).build();
    final CustomerModel secondCustomer =
        CustomerModel.toBuilder(this._customer).setId(2L).setName("Ben").build();

    final QueueModel completedQueue_accountBalance_noCustomer =
        QueueModel.toBuilder(completedQueue_accountBalance)
            .setCustomerId(null)
            .setCustomer(null)
            .build();
    final QueueModel completedQueue_accountBalance_secondCustomer =
        QueueModel.toBuilder(completedQueue_accountBalance)
            .setCustomerId(secondCustomer.id())
            .setCustomer(secondCustomer)
            .build();

    final QueueModel completedQueue_accountBalance_doubleOrder =
        QueueModel.toBuilder(completedQueue_accountBalance)
            .setProductOrders(List.of(this._order, this._order))
            .build();
    final QueueModel completedQueue_accountBalance_noOrder =
        QueueModel.toBuilder(completedQueue_accountBalance_noCustomer)
            .setProductOrders(Collections.emptyList())
            .build();

    assertAll( // spotless:off
        // On made payment.
        () -> assertEquals(9000L, this._customer.balanceOnMadePayment(completedQueue_accountBalance), "Deduct balance when queue completed with account balance"),
        () -> assertEquals(10_000L, this._customer.balanceOnMadePayment(completedQueue_cash), "Keep balance when queue completed with cash"),
        () -> assertEquals(10_000L, this._customer.balanceOnMadePayment(uncompletedQueue_cash), "Keep balance when queue uncompleted"),
        () -> assertEquals(10_000L, this._customer.balanceOnMadePayment(uncompletedQueue_accountBalance), "Keep balance when queue uncompleted"),
        () -> assertEquals(500L, lowBalanceCustomer.balanceOnMadePayment(completedQueue_accountBalance), "Keep balance when the balance low"),

        // On reverted payment.
        () -> assertEquals(11_000L, this._customer.balanceOnRevertedPayment(completedQueue_accountBalance), "Revert balance when queue completed with account balance"),
        () -> assertEquals(10_000L, this._customer.balanceOnRevertedPayment(completedQueue_cash), "Keep balance when queue completed with cash"),
        () -> assertEquals(10_000L, this._customer.balanceOnRevertedPayment(uncompletedQueue_cash), "Keep balance when queue uncompleted"),
        () -> assertEquals(10_000L, this._customer.balanceOnRevertedPayment(uncompletedQueue_accountBalance), "Keep balance when queue uncompleted"),

        // On updated payment.

        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance, completedQueue_accountBalance), "Keep balance when queue unchanged"),
        () -> assertEquals(11_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance, completedQueue_cash), "Revert balance when queue changed to cash"),
        () -> assertEquals(11_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance, uncompletedQueue_accountBalance), "Revert balance when old queue with account balance changed to uncompleted"),
        () -> assertEquals(11_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance, uncompletedQueue_cash), "Revert balance when old queue with account balance changed to uncompleted"),

        () -> assertEquals(9000L, this._customer.balanceOnUpdatedPayment(completedQueue_cash, completedQueue_accountBalance), "Deduct balance when queue changed to account balance"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(completedQueue_cash, completedQueue_cash), "Keep balance when queue unchanged"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(completedQueue_cash, uncompletedQueue_accountBalance), "Keep balance when old queue with cash changed to uncompleted"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(completedQueue_cash, uncompletedQueue_cash), "Keep balance when old queue with cash changed to uncompleted"),

        () -> assertEquals(9000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_accountBalance, completedQueue_accountBalance), "Deduct balance when queue changed to completed with account balance"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_accountBalance, completedQueue_cash), "Keep balance when queue changed to completed with cash"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_accountBalance, uncompletedQueue_accountBalance), "Keep balance when queue stays uncompleted"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_accountBalance, uncompletedQueue_cash), "Keep balance when queue stays uncompleted"),

        () -> assertEquals(9000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_cash, completedQueue_accountBalance), "Deduct balance when queue changed to completed with account balance"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_cash, completedQueue_cash), "Keep balance when queue stays with cash"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_cash, uncompletedQueue_accountBalance), "Keep balance when queue stays uncompleted"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_cash, uncompletedQueue_cash), "Keep balance when queue stays uncompleted"),

        // When the old queue doesn't have customer beforehand.
        // Though, it should be impossible in first place to have
        // a completed queue with account balance and no customer.
        () -> assertEquals(9000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance_noCustomer, completedQueue_accountBalance), "Deduct balance when queue changed to have a customer"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance_noCustomer, completedQueue_cash), "Keep balance when old queue has no customer"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance_noCustomer, uncompletedQueue_accountBalance), "Keep balance when old queue has no customer"),
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance_noCustomer, uncompletedQueue_cash), "Keep balance when old queue has no customer"),

        // When the queue has different customer.
        () -> assertEquals(9000L, secondCustomer.balanceOnUpdatedPayment(completedQueue_accountBalance, completedQueue_accountBalance_secondCustomer), "Deduct balance when queue completed with different customer"),
        () -> assertEquals(9000L, secondCustomer.balanceOnUpdatedPayment(completedQueue_cash, completedQueue_accountBalance_secondCustomer), "Deduct balance when queue completed with different customer"),
        () -> assertEquals(9000L, secondCustomer.balanceOnUpdatedPayment(uncompletedQueue_accountBalance, completedQueue_accountBalance_secondCustomer), "Deduct balance when queue completed with different customer"),
        () -> assertEquals(9000L, secondCustomer.balanceOnUpdatedPayment(uncompletedQueue_cash, completedQueue_accountBalance_secondCustomer), "Deduct balance when queue completed with different customer"),

        // When the queue has more total price of ordered products.
        () -> assertEquals(9000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance, completedQueue_accountBalance_doubleOrder), "Deduct balance by total price difference when queue completed with more total price"),
        () -> assertEquals(8000L, this._customer.balanceOnUpdatedPayment(completedQueue_cash, completedQueue_accountBalance_doubleOrder), "Deduct balance when queue completed with more total price"),
        () -> assertEquals(8000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_accountBalance, completedQueue_accountBalance_doubleOrder), "Deduct balance when queue completed with more total price"),
        () -> assertEquals(8000L, this._customer.balanceOnUpdatedPayment(uncompletedQueue_cash, completedQueue_accountBalance_doubleOrder), "Deduct balance when queue completed with more total price"),

        // When the queue has less total price
        // of ordered products — reduced of product, quantity, etc.
        () -> assertEquals(11_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance, completedQueue_accountBalance_noOrder), "Revert balance when queue completed with lesser total price"),
        // It should be impossible in first place.
        () -> assertEquals(10_000L, this._customer.balanceOnUpdatedPayment(completedQueue_accountBalance_noCustomer, completedQueue_accountBalance_noOrder), "Revert balance up to original when old queue with no customer completed with lesser total price")
    ); // spotless:on
  }

  @Test
  public void debtOnPayment() {
    final QueueModel completedQueue =
        QueueModel.toBuilder(this._queue).setStatus(QueueModel.Status.COMPLETED).build();
    final QueueModel uncompletedQueue =
        QueueModel.toBuilder(this._queue).setStatus(QueueModel.Status.IN_QUEUE).build();

    final CustomerModel secondCustomer =
        CustomerModel.toBuilder(this._customer).setId(2L).setName("Ben").build();

    final QueueModel uncompletedQueue_noCustomer =
        QueueModel.toBuilder(uncompletedQueue).setCustomerId(null).setCustomer(null).build();
    final QueueModel completedQueue_secondCustomer =
        QueueModel.toBuilder(completedQueue)
            .setCustomerId(secondCustomer.id())
            .setCustomer(secondCustomer)
            .build();
    final QueueModel uncompletedQueue_secondCustomer =
        QueueModel.toBuilder(uncompletedQueue)
            .setCustomerId(secondCustomer.id())
            .setCustomer(secondCustomer)
            .build();

    final QueueModel completedQueue_doubleOrder =
        QueueModel.toBuilder(completedQueue)
            .setProductOrders(List.of(this._order, this._order))
            .build();
    final QueueModel uncompletedQueue_doubleOrder =
        QueueModel.toBuilder(uncompletedQueue)
            .setProductOrders(List.of(this._order, this._order))
            .build();

    assertAll( // spotless:off
        // On made payment.
        () -> assertEquals(BigDecimal.valueOf(-1000), this._customer.debtOnMadePayment(uncompletedQueue), "Add debt when queue uncompleted"),
        () -> assertEquals(BigDecimal.ZERO, this._customer.debtOnMadePayment(completedQueue), "Keep debt when queue completed"),

        // On reverted payment.
        () -> assertEquals(BigDecimal.valueOf(1000), this._customer.debtOnRevertedPayment(uncompletedQueue), "Revert debt when queue uncompleted"),
        () -> assertEquals(BigDecimal.ZERO, this._customer.debtOnRevertedPayment(completedQueue), "Keep debt when queue completed"),

        // On updated payment.
        () -> assertEquals(BigDecimal.ZERO, this._customer.debtOnUpdatedPayment(completedQueue, completedQueue), "Keep debt when queue unchanged"),
        () -> assertEquals(BigDecimal.valueOf(-1000), this._customer.debtOnUpdatedPayment(completedQueue, uncompletedQueue), "Add debt when queue changed to uncompleted"),
        () -> assertEquals(BigDecimal.valueOf(1000), this._customer.debtOnUpdatedPayment(uncompletedQueue, completedQueue), "Revert debt when queue changed to completed"),
        () -> assertEquals(BigDecimal.ZERO, this._customer.debtOnUpdatedPayment(uncompletedQueue, uncompletedQueue), "Keep debt when queue unchanged"),

        // When the old queue doesn't have customer beforehand.
        () -> assertEquals(BigDecimal.ZERO, this._customer.debtOnUpdatedPayment(uncompletedQueue_noCustomer, completedQueue), "Keep debt when queue stays completed"),
        () -> assertEquals(BigDecimal.valueOf(-1000), this._customer.debtOnUpdatedPayment(uncompletedQueue_noCustomer, uncompletedQueue), "Add debt when old queue with no customer changed to uncompleted"),

        // When the queue has different customer.
        () -> assertEquals(BigDecimal.ZERO, secondCustomer.debtOnUpdatedPayment(completedQueue, completedQueue_secondCustomer), "Keep debt when queue stays completed"),
        () -> assertEquals(BigDecimal.valueOf(-1000), secondCustomer.debtOnUpdatedPayment(completedQueue, uncompletedQueue_secondCustomer), "Add debt when queue changed to uncompleted with different customer"),
        () -> assertEquals(BigDecimal.ZERO, secondCustomer.debtOnUpdatedPayment(uncompletedQueue, completedQueue_secondCustomer), "Revert debt when queue changed to completed with different customer"),
        () -> assertEquals(BigDecimal.valueOf(-1000), secondCustomer.debtOnUpdatedPayment(uncompletedQueue, uncompletedQueue_secondCustomer), "Add debt when queue stays uncompleted with different customer"),

        // When the queue has more total price of ordered products.
        () -> assertEquals(BigDecimal.ZERO, secondCustomer.debtOnUpdatedPayment(completedQueue, completedQueue_doubleOrder), "Keep debt when queue stays completed"),
        () -> assertEquals(BigDecimal.valueOf(-2000), secondCustomer.debtOnUpdatedPayment(completedQueue, uncompletedQueue_doubleOrder), "Add debt when queue changed to uncompleted with different total price"),
        () -> assertEquals(BigDecimal.valueOf(1000), secondCustomer.debtOnUpdatedPayment(uncompletedQueue, completedQueue_doubleOrder), "Revert debt when queue changed to completed with different total price"),
        () -> assertEquals(BigDecimal.valueOf(-2000), secondCustomer.debtOnUpdatedPayment(uncompletedQueue, uncompletedQueue_doubleOrder), "Add debt when queue stays uncompleted with different total price")
    ); // spotless:on
  }
}
