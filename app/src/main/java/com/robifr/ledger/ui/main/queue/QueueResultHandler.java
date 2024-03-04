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

package com.robifr.ledger.ui.main.queue;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentResultListener;
import com.robifr.ledger.ui.filtercustomer.FilterCustomerFragment;
import com.robifr.ledger.util.Compats;
import com.robifr.ledger.util.Enums;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueueResultHandler {
  @NonNull private final QueueFragment _fragment;

  public QueueResultHandler(@NonNull QueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._fragment
        .getParentFragmentManager()
        .setFragmentResultListener(
            FilterCustomerFragment.Request.FILTER_CUSTOMER.key(),
            this._fragment.getViewLifecycleOwner(),
            new FilterCustomerResultListener());
  }

  private class FilterCustomerResultListener implements FragmentResultListener {
    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
      Objects.requireNonNull(requestKey);
      Objects.requireNonNull(result);

      final FilterCustomerFragment.Request request =
          Enums.valueOf(
              requestKey,
              FilterCustomerFragment.Request.class,
              FilterCustomerFragment.Request::key);
      if (request == null) return;

      switch (request) {
        case FILTER_CUSTOMER -> {
          final List<Long> customerIds =
              Objects.requireNonNullElse(
                  Compats.longArrayListOf(
                      result, FilterCustomerFragment.Result.FILTERED_CUSTOMER_IDS.key()),
                  new ArrayList<>());

          QueueResultHandler.this
              ._fragment
              .queueViewModel()
              .filterView()
              .onCustomersIdsChanged(customerIds);
          QueueResultHandler.this._fragment.filter().openDialog();
        }
      }
    }
  }
}
