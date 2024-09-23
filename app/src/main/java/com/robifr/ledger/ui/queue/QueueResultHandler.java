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

package com.robifr.ledger.ui.queue;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.robifr.ledger.ui.filtercustomer.FilterCustomerFragment;
import com.robifr.ledger.util.Compats;
import java.util.ArrayList;
import java.util.Arrays;
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
            this::_onFilterCustomerResult);
  }

  private void _onFilterCustomerResult(@NonNull String requestKey, @NonNull Bundle result) {
    Objects.requireNonNull(requestKey);
    Objects.requireNonNull(result);

    final FilterCustomerFragment.Request request =
        Arrays.stream(FilterCustomerFragment.Request.values())
            .filter(e -> e.key().equals(requestKey))
            .findFirst()
            .orElse(null);
    if (request == null) return;

    switch (request) {
      case FILTER_CUSTOMER -> {
        final List<Long> customerIds =
            Objects.requireNonNullElse(
                Compats.longArrayListOf(
                    result, FilterCustomerFragment.Result.FILTERED_CUSTOMER_IDS_LONG_ARRAY.key()),
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
