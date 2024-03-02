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

package com.robifr.ledger.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import java.util.Objects;

public interface StringResources {
  @NonNull
  public static String stringOf(@NonNull Context context, @NonNull StringResources resources) {
    Objects.requireNonNull(context);
    Objects.requireNonNull(resources);

    if (resources instanceof Plurals res) {
      return context.getResources().getQuantityString(res.resId, res.quantity, res.args);
    }

    return context.getString(((Strings) resources).resId, ((Strings) resources).args);
  }

  public static record Strings(@StringRes int resId, @NonNull Object... args)
      implements StringResources {}

  public static record Plurals(@PluralsRes int resId, int quantity, @NonNull Object... args)
      implements StringResources {}
}
