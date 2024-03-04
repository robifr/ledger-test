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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.robifr.ledger.R;
import com.robifr.ledger.data.QueueFilters;
import com.robifr.ledger.data.QueueSortMethod;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.databinding.ListableFragmentBinding;
import com.robifr.ledger.ui.main.queue.filter.QueueFilter;
import com.robifr.ledger.ui.main.queue.recycler.QueueAdapter;
import com.robifr.ledger.ui.main.queue.viewmodel.QueueViewModel;
import java.util.Objects;
import java.util.Set;

public class QueueFragment extends Fragment {
  @Nullable private ListableFragmentBinding _fragmentBinding;
  @Nullable private QueueSort _sort;
  @Nullable private QueueFilter _filter;
  @Nullable private QueueAdapter _adapter;
  @Nullable private QueueResultHandler _resultHandler;

  @Nullable private QueueViewModel _queueViewModel;
  @Nullable private QueueViewModelHandler _viewModelHandler;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this._fragmentBinding = ListableFragmentBinding.inflate(inflater, container, false);
    return this._fragmentBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._fragmentBinding);

    this._sort = new QueueSort(this);
    this._filter = new QueueFilter(this);
    this._adapter = new QueueAdapter(this);
    this._resultHandler = new QueueResultHandler(this);
    this._queueViewModel =
        new ViewModelProvider(
                this.requireActivity(), new QueueViewModel.Factory(this.requireContext()))
            .get(QueueViewModel.class);
    this._viewModelHandler = new QueueViewModelHandler(this, this._queueViewModel);

    this._fragmentBinding.toolbar.getMenu().clear();
    this._fragmentBinding.toolbar.inflateMenu(R.menu.reusable_toolbar_main);
    this._fragmentBinding.toolbar.setNavigationIcon(null);
    this._fragmentBinding.toolbar.setTitle(this.getString(R.string.app_name));
    this._fragmentBinding.sortByChip.setOnClickListener(chip -> this._sort.openDialog());
    this._fragmentBinding.filtersChip.setOnClickListener(chip -> this._filter.openDialog());
    this._fragmentBinding.recyclerView.setLayoutManager(
        new LinearLayoutManager(this.requireContext()));
    this._fragmentBinding.recyclerView.setAdapter(this._adapter);
    this._fragmentBinding.recyclerView.setItemViewCacheSize(0);

    final QueueFilters initialFilters =
        QueueFilters.toBuilder()
            .setNullCustomerShown(true)
            .setFilteredDate(QueueFilters.DateRange.ALL_TIME)
            .setFilteredDateStartEnd(QueueFilters.DateRange.ALL_TIME.dateStartEnd())
            .setFilteredStatus(Set.of(QueueModel.Status.values()))
            .build();

    this._queueViewModel.onQueuesChanged(this._queueViewModel.fetchAllQueues());
    this._queueViewModel.onSortMethodChanged(
        new QueueSortMethod(QueueSortMethod.SortBy.CUSTOMER_NAME, true));
    this._queueViewModel.filterView().onFiltersChanged(initialFilters);
  }

  @NonNull
  public ListableFragmentBinding fragmentBinding() {
    return Objects.requireNonNull(this._fragmentBinding);
  }

  @NonNull
  public QueueSort sort() {
    return Objects.requireNonNull(this._sort);
  }

  @NonNull
  public QueueFilter filter() {
    return Objects.requireNonNull(this._filter);
  }

  @NonNull
  public QueueAdapter adapter() {
    return Objects.requireNonNull(this._adapter);
  }

  @NonNull
  public QueueViewModel queueViewModel() {
    return Objects.requireNonNull(this._queueViewModel);
  }

  public static class Factory extends FragmentFactory {
    @Override
    @NonNull
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
      Objects.requireNonNull(classLoader);
      Objects.requireNonNull(className);

      return (className.equals(QueueFragment.class.getName()))
          ? new QueueFragment()
          : super.instantiate(classLoader, className);
    }
  }
}
