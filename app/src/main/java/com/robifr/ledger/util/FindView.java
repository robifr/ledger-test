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

package com.robifr.ledger.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Objects;

public class FindView {
  private FindView() {}

  public static ViewGroup rootView(@NonNull AppCompatActivity activity) {
    Objects.requireNonNull(activity);

    final ViewGroup contentView = activity.findViewById(android.R.id.content);
    return (ViewGroup) contentView.getChildAt(0);
  }

  @Nullable
  public static View findParentById(@NonNull View view, int targetId) {
    Objects.requireNonNull(view);

    if (view.getId() == targetId) return view;
    return view.getParent() != null
        ? FindView.findParentById((View) view.getParent(), targetId)
        : null;
  }

  @Nullable
  public static <T extends View> T findParentByType(@NonNull View view, @NonNull Class<T> cls) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(cls);

    if (cls.isInstance(view)) return cls.cast(view);
    return view.getParent() != null
        ? FindView.findParentByType((View) view.getParent(), cls)
        : null;
  }

  @Nullable
  public static <T extends View> T findViewByType(
      @NonNull ViewGroup viewGroup, @NonNull Class<T> cls) {
    Objects.requireNonNull(viewGroup);
    Objects.requireNonNull(cls);

    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      final View view = viewGroup.getChildAt(i);

      if (cls.isInstance(view)) return cls.cast(view);

      View result;

      if (view instanceof ViewGroup) {
        result = FindView.findViewByType((ViewGroup) view, cls);

        if (result != null) return cls.cast(result);
      }
    }

    return null;
  }

  @Nullable
  public static ImageButton findToolbarNavigationButton(@NonNull Toolbar toolbar) {
    Objects.requireNonNull(toolbar);

    for (int i = 0; i < toolbar.getChildCount(); i++) {
      final View child = toolbar.getChildAt(i);

      if (child instanceof ImageButton button
          && button.getDrawable() == toolbar.getNavigationIcon()) {
        return button;
      }
    }

    return null;
  }
}
