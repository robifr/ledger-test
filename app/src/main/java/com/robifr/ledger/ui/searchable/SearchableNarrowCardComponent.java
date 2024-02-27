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
import com.google.android.material.card.MaterialCardView;
import com.robifr.ledger.R;
import java.util.Objects;

public class SearchableNarrowCardComponent {
  private final AppCompatActivity _activity;
  private final MaterialCardView _rootLayout;
  private final ConstraintLayout _parentLayout;

  //	private final ReusableShapeableImageComponent _imageComponents;
  //	private final TextView _titleView;
  //	private final TextView _descriptionView;

  /**
   * @param rootLayout Root view of {@link R.layout#searchable_narrow_card}.
   */
  public SearchableNarrowCardComponent(
      @NonNull AppCompatActivity activity, @NonNull MaterialCardView rootLayout) {
    this._activity = Objects.requireNonNull(activity);
    this._rootLayout = Objects.requireNonNull(rootLayout);
    this._parentLayout = this._rootLayout.findViewById(R.id.searchable_narrow_constraintlayout);
    //		this._imageComponents = new ReusableShapeableImageComponent(
    //			this._activity,
    //			this._rootLayout.findViewById(R.id.constraintLayout)
    //		);
    //		this._titleView = this._rootLayout.findViewById(R.id.searchable_narrow_title_textview);
    //		this._descriptionView =
    // this._rootLayout.findViewById(R.id.searchable_narrow_description_textview);
  }

  public MaterialCardView rootLayout() {
    return this._rootLayout;
  }

  public ConstraintLayout parentLayout() {
    return this._parentLayout;
  }

  //	public ReusableShapeableImageComponent imageComponents() { return this._imageComponents; }

  //	public TextView titleView() { return this._titleView; }

  //	public TextView descriptionView() { return this._descriptionView; }
}
