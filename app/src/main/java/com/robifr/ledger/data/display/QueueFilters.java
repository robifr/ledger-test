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

package com.robifr.ledger.data.display;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import com.robifr.ledger.data.model.QueueModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @param filteredCustomerIds Filter queue if {@link QueueModel#customerId() customer ID} is
 *     included inside the list.
 * @param isNullCustomerShown Whether queue with no {@link QueueModel#customerId() customer ID}
 *     should be shown or not.
 * @param filteredStatus Filter queue if {@link QueueModel#status() status} is included.
 * @param filteredTotalPrice Filter queue if {@link QueueModel#grandTotalPrice() grand total price}
 *     is in-between min (first) and max (second). Set the pair value as null to represent unbounded
 *     number.
 * @param filteredDate Filter queues if {@link QueueModel#date() date} is still considered within
 *     specified range of start and end date.
 */
public record QueueFilters(
    @NonNull List<Long> filteredCustomerIds,
    boolean isNullCustomerShown,
    @NonNull Set<QueueModel.Status> filteredStatus,
    @NonNull Pair<BigDecimal, BigDecimal> filteredTotalPrice,
    @NonNull QueueDate filteredDate) {

  public QueueFilters {
    Objects.requireNonNull(filteredCustomerIds);
    Objects.requireNonNull(filteredStatus);
    Objects.requireNonNull(filteredTotalPrice);
    Objects.requireNonNull(filteredDate);

    filteredCustomerIds = Collections.unmodifiableList(filteredCustomerIds);
    filteredStatus = Collections.unmodifiableSet(filteredStatus);
  }

  @NonNull
  public static Builder toBuilder() {
    return new Builder();
  }

  @NonNull
  public static Builder toBuilder(@NonNull QueueFilters filters) {
    Objects.requireNonNull(filters);

    return new Builder()
        .setFilteredCustomerIds(filters.filteredCustomerIds())
        .setNullCustomerShown(filters.isNullCustomerShown())
        .setFilteredStatus(filters.filteredStatus())
        .setFilteredTotalPrice(filters.filteredTotalPrice())
        .setFilteredDate(filters.filteredDate());
  }

  public static class Builder {
    @NonNull private List<Long> _filteredCustomerIds = new ArrayList<>();
    private boolean _isNullCustomerShown = true;
    @NonNull private Set<QueueModel.Status> _filteredStatus = Set.of(QueueModel.Status.values());
    @NonNull private Pair<BigDecimal, BigDecimal> _filteredTotalPrice = new Pair<>(null, null);
    @NonNull private QueueDate _filteredDate = QueueDate.withRange(QueueDate.Range.ALL_TIME);

    private Builder() {}

    @NonNull
    public Builder setFilteredCustomerIds(@NonNull List<Long> customerIds) {
      this._filteredCustomerIds = Objects.requireNonNull(customerIds);
      return this;
    }

    @NonNull
    public Builder setNullCustomerShown(boolean isShown) {
      this._isNullCustomerShown = isShown;
      return this;
    }

    @NonNull
    public Builder setFilteredStatus(@NonNull Set<QueueModel.Status> status) {
      this._filteredStatus = Objects.requireNonNull(status);
      return this;
    }

    @NonNull
    public Builder setFilteredTotalPrice(@NonNull Pair<BigDecimal, BigDecimal> price) {
      this._filteredTotalPrice = Objects.requireNonNull(price);
      return this;
    }

    @NonNull
    public Builder setFilteredDate(@NonNull QueueDate date) {
      this._filteredDate = Objects.requireNonNull(date);
      return this;
    }

    @NonNull
    public QueueFilters build() {
      return new QueueFilters(
          this._filteredCustomerIds,
          this._isNullCustomerShown,
          this._filteredStatus,
          this._filteredTotalPrice,
          this._filteredDate);
    }
  }
}
