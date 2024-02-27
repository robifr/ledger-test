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

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Objects;

public class SingleToast {
  @Nullable private static Toast _toast;

  private SingleToast() {}

  public static void show(@NonNull Context context, @NonNull String message, int duration) {
    Objects.requireNonNull(context);
    Objects.requireNonNull(message);

    if (SingleToast._toast != null) SingleToast._toast.cancel();

    SingleToast._toast = Toast.makeText(context, message, duration);
    SingleToast._toast.show();
  }
}
