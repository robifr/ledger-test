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
  @Nullable private DashboardSummary _summaryOverview;
  @Nullable private DashboardBalance _balanceOverview;
  @Nullable private DashboardRevenue _revenueOverview;

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
    this._summaryOverview = new DashboardSummary(this);
    this._balanceOverview = new DashboardBalance(this);
    this._revenueOverview = new DashboardRevenue(this);

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
  public DashboardSummary summaryOverview() {
    return Objects.requireNonNull(this._summaryOverview);
  }

  @NonNull
  public DashboardBalance balanceOverview() {
    return Objects.requireNonNull(this._balanceOverview);
  }

  @NonNull
  public DashboardRevenue revenueOverview() {
    return Objects.requireNonNull(this._revenueOverview);
  }

  @NonNull
  public DashboardViewModel dashboardViewModel() {
    return Objects.requireNonNull(this._dashboardViewModel);
  }
}
