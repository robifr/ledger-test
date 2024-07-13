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

package com.robifr.ledger.ui.dashboard;

import androidx.annotation.NonNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @param xAxisDomain The domain values for the x-axis.
 * @param yAxisDomain The domain values for the y-axis.
 * @param data Map of data to be rendered with.
 * @param <X> The type of domain for the x-axis. Typically {@link String} for a band axis.
 * @param <Y> The type of domain for the y-axis. Like {@link String} for a band axis or {@link
 *     Double} for a linear axis.
 */
public record CartesianChartModel<X, Y>(
    @NonNull List<X> xAxisDomain, @NonNull List<Y> yAxisDomain, @NonNull Map<String, Double> data) {
  public CartesianChartModel {
    Objects.requireNonNull(xAxisDomain);
    Objects.requireNonNull(yAxisDomain);
    Objects.requireNonNull(data);
  }
}
