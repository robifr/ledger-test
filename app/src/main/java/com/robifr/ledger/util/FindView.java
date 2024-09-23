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
