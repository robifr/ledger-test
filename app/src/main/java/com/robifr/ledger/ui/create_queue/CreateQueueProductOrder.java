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

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.ProductOrderCardBinding;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class CreateQueueProductOrder implements View.OnClickListener, View.OnLongClickListener {
  @NonNull private final CreateQueueFragment _fragment;
  @NonNull private final MakeProductOrder _makeProductOrder;
  @Nullable private ActionMode _contextualMode;

  public CreateQueueProductOrder(@NonNull CreateQueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
    this._makeProductOrder = new MakeProductOrder(this._fragment);

    this._fragment
        .fragmentBinding()
        .productOrder
        .addButton
        .setOnClickListener(button -> this._makeProductOrder.openCreateDialog());
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.cardView -> {
        final ProductOrderCardBinding cardBinding = ProductOrderCardBinding.bind(view);
        final int cardIndex =
            this._fragment
                .fragmentBinding()
                .productOrder
                .listLayout
                .indexOfChild(cardBinding.cardView);
        final Boolean isContextualModeActive =
            this._fragment
                .createQueueViewModel()
                .selectProductOrderView()
                .isContextualModeActive()
                .getValue();

        // Check product order on contextual mode.
        if (isContextualModeActive != null && isContextualModeActive) {
          this._fragment
              .createQueueViewModel()
              .selectProductOrderView()
              .onProductOrderCheckedChanged(cardIndex, !cardBinding.checkbox.isChecked());

          // Edit selected product order.
        } else {
          this._fragment
              .createQueueViewModel()
              .makeProductOrderView()
              .onProductOrderToEditChanged(
                  this._fragment.createQueueViewModel().inputtedProductOrders().get(cardIndex));
          this._makeProductOrder.openEditDialog();
        }
      }
    }
  }

  @Override
  public boolean onLongClick(@NonNull View view) {
    Objects.requireNonNull(view);

    return switch (view.getId()) {
      case R.id.cardView -> {
        final ProductOrderCardBinding cardBinding = ProductOrderCardBinding.bind(view);
        final int cardIndex =
            this._fragment
                .fragmentBinding()
                .productOrder
                .listLayout
                .indexOfChild(cardBinding.cardView);
        this._fragment
            .createQueueViewModel()
            .selectProductOrderView()
            .onProductOrderCheckedChanged(cardIndex, !cardBinding.checkbox.isChecked());
        yield true;
      }

      default -> false;
    };
  }

  @NonNull
  public MakeProductOrder makeProductOrder() {
    return this._makeProductOrder;
  }

  public void setSelectedProductOrderByIndexes(@NonNull Set<Integer> selectedIndexes) {
    Objects.requireNonNull(selectedIndexes);

    if (this._contextualMode != null) {
      this._contextualMode.setTitle(Integer.toString(selectedIndexes.size()));
    }

    final TreeSet<Integer> indexes = new TreeSet<>(selectedIndexes);

    for (int i = this._fragment.fragmentBinding().productOrder.listLayout.getChildCount();
        i-- > 0; ) {
      final ProductOrderCardBinding cardBinding =
          ProductOrderCardBinding.bind(
              this._fragment.fragmentBinding().productOrder.listLayout.getChildAt(i));
      final boolean shouldCheck = !indexes.isEmpty() && indexes.last() == i;
      final int checkboxVisibility = shouldCheck ? View.VISIBLE : View.GONE;
      final int productImageVisibility = shouldCheck ? View.GONE : View.VISIBLE;

      cardBinding.cardView.setChecked(shouldCheck);
      cardBinding.checkbox.setChecked(shouldCheck);
      cardBinding.checkbox.setVisibility(checkboxVisibility);
      cardBinding.productImage.text.setVisibility(productImageVisibility);

      if (shouldCheck) indexes.pollLast();
    }
  }

  public void setContextualMode(boolean isActive) {
    if (isActive && this._fragment.requireActivity() instanceof AppCompatActivity activity) {
      this._contextualMode =
          activity.startSupportActionMode(new ContextualActionMode(this._fragment));

    } else if (!isActive && this._contextualMode != null) {
      this._contextualMode.finish();
      this._contextualMode = null;
    }
  }

  public void setGrandTotalPrice(@NonNull BigDecimal grandTotalPrice) {
    Objects.requireNonNull(grandTotalPrice);

    this._fragment
        .fragmentBinding()
        .productOrder
        .grandTotalPrice
        .setText(CurrencyFormat.format(grandTotalPrice, "id", "ID"));
  }

  public void setTotalDiscount(@NonNull BigDecimal totalDiscount) {
    Objects.requireNonNull(totalDiscount);

    this._fragment
        .fragmentBinding()
        .productOrder
        .totalDiscount
        .setText(CurrencyFormat.format(totalDiscount, "id", "ID"));
  }

  public void setCustomerBalanceAfterPaymentTitle(@Nullable String title) {
    final int viewVisibility = title != null ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().productOrder.customerBalanceTitle.setText(title);
    this._fragment
        .fragmentBinding()
        .productOrder
        .customerBalanceTitle
        .setVisibility(viewVisibility);
  }

  public void setCustomerBalanceAfterPayment(@Nullable Long balance) {
    final String text =
        balance != null ? CurrencyFormat.format(BigDecimal.valueOf(balance), "id", "ID") : null;
    final int viewVisibility = balance != null ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().productOrder.customerBalance.setText(text);
    this._fragment.fragmentBinding().productOrder.customerBalance.setVisibility(viewVisibility);
  }

  public void setCustomerDebtAfterPaymentTitle(@Nullable String title) {
    final int viewVisibility = title != null ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().productOrder.customerDebtTitle.setText(title);
    this._fragment.fragmentBinding().productOrder.customerDebtTitle.setVisibility(viewVisibility);
  }

  public void setCustomerDebtAfterPayment(@Nullable BigDecimal debt) {
    final String text = debt != null ? CurrencyFormat.format(debt, "id", "ID") : null;
    final int textColor =
        debt != null && debt.compareTo(BigDecimal.ZERO) < 0
            // Negative debt will be shown red.
            ? ContextCompat.getColor(this._fragment.requireContext(), R.color.red)
            : ContextCompat.getColor(this._fragment.requireContext(), R.color.text_enabled);
    final int viewVisibility = debt != null ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().productOrder.customerDebt.setText(text);
    this._fragment.fragmentBinding().productOrder.customerDebt.setTextColor(textColor);
    this._fragment.fragmentBinding().productOrder.customerDebt.setVisibility(viewVisibility);
  }

  public void notifyProductOrderAdded(int... productOrderIndex) {
    for (int index : productOrderIndex) {
      final ProductOrderCardBinding cardBinding =
          ProductOrderCardBinding.inflate(
              this._fragment.getLayoutInflater(),
              this._fragment.fragmentBinding().productOrder.listLayout,
              false);
      final ProductOrderCardComponent components =
          new ProductOrderCardComponent(this._fragment.requireContext(), cardBinding);

      components.setProductOrder(
          this._fragment.createQueueViewModel().inputtedProductOrders().get(index));
      cardBinding.cardView.setOnClickListener(this);
      cardBinding.cardView.setOnLongClickListener(this);
      cardBinding.checkbox.setChecked(false);
      cardBinding.checkbox.setVisibility(View.GONE);
      cardBinding.productImage.text.setVisibility(View.VISIBLE);
      this._fragment.fragmentBinding().productOrder.listLayout.addView(cardBinding.cardView);
    }
  }

  public void notifyProductOrderRemoved(int... productOrderIndex) {
    for (int index : productOrderIndex) {
      this._fragment.fragmentBinding().productOrder.listLayout.removeViewAt(index);
    }
  }

  public void notifyProductOrderUpdated(int... productOrderIndex) {
    for (int index : productOrderIndex) {
      final ProductOrderCardBinding cardBinding =
          ProductOrderCardBinding.bind(
              this._fragment.fragmentBinding().productOrder.listLayout.getChildAt(index));
      final ProductOrderCardComponent components =
          new ProductOrderCardComponent(this._fragment.requireContext(), cardBinding);

      components.setProductOrder(
          this._fragment.createQueueViewModel().inputtedProductOrders().get(index));
    }
  }

  private static class ContextualActionMode implements ActionMode.Callback {
    @NonNull private final CreateQueueFragment _fragment;
    @ColorInt private final int _normalStatusBarColor;

    public ContextualActionMode(@NonNull CreateQueueFragment fragment) {
      this._fragment = Objects.requireNonNull(fragment);
      this._normalStatusBarColor = fragment.requireActivity().getWindow().getStatusBarColor();
    }

    @Override
    public boolean onCreateActionMode(@NonNull ActionMode mode, @NonNull Menu menu) {
      Objects.requireNonNull(mode);
      Objects.requireNonNull(menu);

      mode.getMenuInflater().inflate(R.menu.createqueue_contextualtoolbar, menu);
      // Match status bar color with the contextual toolbar background color.
      this._fragment
          .requireActivity()
          .getWindow()
          .setStatusBarColor(
              ContextCompat.getColor(this._fragment.requireContext(), R.color.surface));
      return true;
    }

    @Override
    public boolean onPrepareActionMode(@NonNull ActionMode mode, @NonNull Menu menu) {
      return false;
    }

    @Override
    public boolean onActionItemClicked(@NonNull ActionMode mode, @NonNull MenuItem item) {
      Objects.requireNonNull(mode);
      Objects.requireNonNull(item);

      return switch (item.getItemId()) {
        case R.id.createqueue_contextualtoolbar_delete_item -> {
          this._fragment
              .createQueueViewModel()
              .selectProductOrderView()
              .onDeleteSelectedProductOrder();
          yield true;
        }

        default -> false;
      };
    }

    @Override
    public void onDestroyActionMode(@NonNull ActionMode mode) {
      Objects.requireNonNull(mode);

      this._fragment.createQueueViewModel().selectProductOrderView().reset();
      // Re-apply original status bar color.
      this._fragment.requireActivity().getWindow().setStatusBarColor(this._normalStatusBarColor);
    }
  }
}
