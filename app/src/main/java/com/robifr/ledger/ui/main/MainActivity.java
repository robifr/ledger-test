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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.text.HtmlCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.MainActivityBinding;
import com.robifr.ledger.util.Compats;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import java.util.Objects;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity
    implements NavigationBarView.OnItemSelectedListener,
        NavController.OnDestinationChangedListener {
  @Nullable private MainActivityBinding _activityBinding;
  @Nullable private MainCreate _create;
  @Nullable private MainResultHandler _resultHandler;

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(this._activityBinding);

    final NavController navController = Navigation.findNavController(this, R.id.fragmentContainer);
    final NavDestination currentDestination =
        Objects.requireNonNull(navController.getCurrentDestination());

    navController.navigate(
        item.getItemId(),
        null,
        new NavOptions.Builder().setPopUpTo(currentDestination.getId(), true).build());
    return true;
  }

  @Override
  public void onDestinationChanged(
      @NonNull NavController navController,
      @NonNull NavDestination destination,
      @Nullable Bundle bundle) {
    Objects.requireNonNull(navController);
    Objects.requireNonNull(destination);
    Objects.requireNonNull(this._activityBinding);

    // Match selected item of bottom navigation with the visible fragment.
    // It doesn't get matched on back pressed.
    final MenuItem item =
        this._activityBinding.bottomNavigation.getMenu().findItem(destination.getId());
    if (item != null) item.setChecked(true);

    final int bottomNavigationVisibility =
        destination.getParent() != null && destination.getParent().getId() == R.id.mainGraph
            ? View.VISIBLE
            : View.GONE;
    final int createButtonVisibility =
        destination.getParent() != null
                && destination.getParent().getId() == R.id.mainGraph
                && destination.getId() != R.id.dashboardFragment
            ? View.VISIBLE
            : View.GONE;
    final TypedValue backgroundColor = new TypedValue();
    final int navigationBarColor =
        bottomNavigationVisibility == View.VISIBLE
            ? com.google.android.material.R.attr.colorSurface
            : android.R.attr.colorBackground;

    // Due to bottom navigation on main activity uses a different color — color surface.
    // Match system navigation bar color into it, otherwise with current background.
    this.getTheme().resolveAttribute(navigationBarColor, backgroundColor, true);
    this.getWindow().setNavigationBarColor(this.getColor(backgroundColor.resourceId));

    // Hide views on main activity when user navigating to another fragment other than
    // the one defined as top of the stack — queue, customer, and product — inside bottom
    // navigation.
    this._activityBinding.bottomNavigation.setVisibility(bottomNavigationVisibility);
    this._activityBinding.createButton.setVisibility(createButtonVisibility);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstance) {
    super.onCreate(savedInstance);

    this._activityBinding = MainActivityBinding.inflate(this.getLayoutInflater());
    this.setContentView(this._activityBinding.getRoot());

    this._create = new MainCreate(this);
    this._resultHandler = new MainResultHandler(this);

    this._activityBinding.createButton.setOnClickListener(button -> this._create.openDialog());
    this._activityBinding.bottomNavigation.setOnItemSelectedListener(this);
    this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedHandler());
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

    List.of(R.id.dashboardFragment, R.id.queueFragment, R.id.customerFragment, R.id.productFragment)
        .forEach(
            id ->
                this._activityBinding
                    .bottomNavigation
                    .findViewById(id)
                    .setOnLongClickListener(Compats::hideTooltipText));

    final NavHostFragment navHostFragment =
        (NavHostFragment)
            Objects.requireNonNull(
                this.getSupportFragmentManager().findFragmentById(R.id.fragmentContainer));
    navHostFragment.getNavController().addOnDestinationChangedListener(this);

    if (!Environment.isExternalStorageManager()) this.requireStoragePermission();
  }

  @NonNull
  public MainActivityBinding activityBinding() {
    return Objects.requireNonNull(this._activityBinding);
  }

  @NonNull
  public Intent requireStoragePermission() {
    Objects.requireNonNull(this._resultHandler);

    final Intent intent =
        new Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.fromParts("package", this.getPackageName(), null));

    new MaterialAlertDialogBuilder(this)
        .setTitle(R.string.main_storageAccessPermission)
        .setMessage(
            HtmlCompat.fromHtml(
                this.getString(R.string.main_storageAccessPermission_description),
                HtmlCompat.FROM_HTML_MODE_LEGACY))
        .setNegativeButton(R.string.action_denyAndQuit, (dialog, type) -> this.finish())
        .setPositiveButton(
            R.string.action_grant,
            (dialog, type) -> this._resultHandler.permissionLauncher().launch(intent))
        .setCancelable(false)
        .show();
    return intent;
  }

  private class OnBackPressedHandler extends OnBackPressedCallback {
    public OnBackPressedHandler() {
      super(true);
    }

    @Override
    public void handleOnBackPressed() {
      MainActivity.this.finish();
    }
  }
}
