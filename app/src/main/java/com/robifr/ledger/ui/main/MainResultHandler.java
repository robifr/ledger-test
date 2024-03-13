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

package com.robifr.ledger.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
            new ActivityResultContracts.StartActivityForResult(), new PermissionResultListener());
  }

  @NonNull
  public ActivityResultLauncher<Intent> permissionLauncher() {
    return this._permissionLauncher;
  }

  private class PermissionResultListener implements ActivityResultCallback<ActivityResult> {
    @Override
    public void onActivityResult(@NonNull ActivityResult result) {
      Objects.requireNonNull(result);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Permission granted.
        if (Environment.isExternalStorageManager()) {
          MainResultHandler.this._activity.finish();
          MainResultHandler.this._activity.startActivity(
              MainResultHandler.this._activity.getIntent());

          // Denied. Retry to show dialog permission.
        } else {
          MainResultHandler.this._activity.requireStoragePermission();
        }
      }
    }
  }
}
