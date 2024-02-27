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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.robifr.ledger.R;
import java.util.Objects;

public class SearchableListComponent {
  private final AppCompatActivity _activity;
  private final ConstraintLayout _rootLayout;

  //	private final TextView _titleView;
  //	private final MaterialButton _seeMoreButton;
  //	private final RecyclerView _recyclerView;

  /**
   * @param rootLayout Root view of {@link R.layout#searchable_list_layout}.
   */
  public SearchableListComponent(
      @NonNull AppCompatActivity activity, @NonNull ConstraintLayout rootLayout) {
    this._activity = Objects.requireNonNull(activity);
    this._rootLayout = Objects.requireNonNull(rootLayout);
    //		this._titleView = this._rootLayout.findViewById(R.id.searchable_list_title_textview);
    //		this._seeMoreButton =
    // this._rootLayout.findViewById(R.id.searchable_list_seemore_materialbutton);
    //		this._recyclerView = this._rootLayout.findViewById(R.id.searchable_list_recyclerview);
  }

  public ConstraintLayout rootLayout() {
    return this._rootLayout;
  }

  //	public TextView titleView() { return this._titleView; }
  //
  //	public MaterialButton seeMoreButton() { return this._seeMoreButton; }
  //
  //	public RecyclerView recyclerView() { return this._recyclerView; }
}
