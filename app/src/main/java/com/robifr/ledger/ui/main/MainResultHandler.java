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

package com.robifr.ledger.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import java.util.Objects;

public class MainResultHandler {
  @NonNull private final MainActivity _activity;
  @NonNull private final ActivityResultLauncher<Intent> _permissionLauncher;

  public MainResultHandler(@NonNull MainActivity activity) {
    this._activity = Objects.requireNonNull(activity);
    this._permissionLauncher =
        this._activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::_onRequestPermission);
  }

  @NonNull
  public ActivityResultLauncher<Intent> permissionLauncher() {
    return this._permissionLauncher;
  }

  private void _onRequestPermission(@NonNull ActivityResult result) {
    Objects.requireNonNull(result);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      // Permission granted.
      if (Environment.isExternalStorageManager()) {
        this._activity.finish();
        this._activity.startActivity(this._activity.getIntent());

        // Denied. Retry to show dialog permission.
      } else {
        this._activity.requireStoragePermission();
      }
    }
  }
}
