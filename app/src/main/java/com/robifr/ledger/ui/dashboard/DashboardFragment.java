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
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.DashboardFragmentBinding;
import com.robifr.ledger.ui.dashboard.viewmodel.DashboardViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {
  @Nullable private DashboardFragmentBinding _fragmentBinding;

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

    // Use activity store owner because this fragment is used by bottom navigation.
    // Which to prevents view model re-instantiation.
    this._dashboardViewModel =
        new ViewModelProvider(this.requireActivity()).get(DashboardViewModel.class);
    this._viewModelHandler = new DashboardViewModelHandler(this, this._dashboardViewModel);

    this._fragmentBinding.totalDeposits.title.setText(this.getString(R.string.text_total_deposits));
    this._fragmentBinding.totalDebts.title.setText(this.getString(R.string.text_total_debts));
    this._fragmentBinding.totalDebts.cardView.setCardBackgroundColor(
        this.requireContext().getColor(R.color.light_red_15));
  }

  @NonNull
  public DashboardFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }
}
