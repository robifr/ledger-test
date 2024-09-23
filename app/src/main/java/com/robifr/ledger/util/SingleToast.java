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
