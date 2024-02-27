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

package com.robifr.ledger.ui.searchable;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import com.robifr.ledger.R;
import java.util.Objects;

public abstract class SearchableFragment extends Fragment
    implements SearchView.OnQueryTextListener {
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstance) {
    Objects.requireNonNull(inflater);

    this.setHasOptionsMenu(true);
    return inflater.inflate(R.layout.searchable_fragment, container, false);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    Objects.requireNonNull(menu);
    Objects.requireNonNull(inflater);

    menu.clear();
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    Objects.requireNonNull(view);

    final AppCompatActivity activity = (AppCompatActivity) this.getActivity();
    final InputMethodManager inputManager =
        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    //		final Toolbar toolbar = view.findViewById(R.id.searchable_toolbar);
    //		final SearchView searchView = view.findViewById(R.id.searchable_seachview);

    //		activity.setSupportActionBar(toolbar);
    //		searchView.setQueryHint("Search");
    //		searchView.setOnQueryTextListener(this);
    //		searchView.requestFocus();
    //		searchView.postDelayed(() ->
    //			//We need to add delay in order for the keyboard to show up.
    //			inputManager.showSoftInput(view.findFocus(), InputMethodManager.SHOW_IMPLICIT),
    //			0
    //		);

    // Remove tooltip text on back navigation.
    //		FindView.findToolbarNavigationButton(toolbar).setOnLongClickListener(v -> {
    //			TooltipCompat.setTooltipText(v, null);
    //			return false;
    //		});

    this.setVisible(true);
  }

  @Override
  public void onStop() {
    final AppCompatActivity activity = (AppCompatActivity) this.getActivity();
    //		final SearchView searchView = activity.findViewById(R.id.searchable_seachview);

    //		searchView.setQuery(null, false);
    super.onStop();
  }

  @Override
  public boolean onQueryTextChange(@NonNull String text) {
    Objects.requireNonNull(text);
    return true;
  }

  public void setVisible(boolean isVisible) {
    //		final AppCompatActivity activity = (AppCompatActivity)this.getActivity();
    //		final int statusBarColor = isVisible ? R.color.contextualtoolbar_background :
    // R.color.toolbar_background;
    //		final int navigationBottomColor = isVisible ? R.color.white : R.color.dark_white;
    //
    //		activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, statusBarColor));
    //		activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity,
    // navigationBottomColor));
  }
}
