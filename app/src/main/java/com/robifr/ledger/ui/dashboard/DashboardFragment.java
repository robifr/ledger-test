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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.databinding.DashboardFragmentBinding;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {
  @Nullable private DashboardFragmentBinding _fragmentBinding;
  @Nullable private DashboardDate _date;
  @Nullable private DashboardBalance _balanceOverview;
  @Nullable private DashboardPerformance _performanceOverview;

  @Nullable private DashboardViewModel _dashboardViewModel;
  @Nullable private DashboardViewModelHandler _viewModelHandler;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding = DashboardFragmentBinding.inflate(inflater, container, false);
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);

    this._date = new DashboardDate(this);
    this._balanceOverview = new DashboardBalance(this);
    this._performanceOverview = new DashboardPerformance(this);

    // Use activity store owner because this fragment is used by bottom navigation.
    // Which to prevents view model re-instantiation.
    this._dashboardViewModel =
        new ViewModelProvider(this.requireActivity()).get(DashboardViewModel.class);
    this._viewModelHandler = new DashboardViewModelHandler(this, this._dashboardViewModel);

    this._fragmentBinding.dateChip.setOnClickListener(chip -> this._date.openDialog());
  }

  @NonNull
  public DashboardFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public DashboardBalance balanceOverview() {
    return Objects.requireNonNull(this._balanceOverview);
  }

  @NonNull
  public DashboardPerformance performanceOverview() {
    return Objects.requireNonNull(this._performanceOverview);
  }

  @NonNull
  public DashboardViewModel dashboardViewModel() {
    return Objects.requireNonNull(this._dashboardViewModel);
  }
}
