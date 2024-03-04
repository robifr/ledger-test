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

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationBarView;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.MainActivityBinding;
import com.robifr.ledger.ui.BackStack;
import com.robifr.ledger.ui.BackStackNavigation;
import com.robifr.ledger.ui.main.customer.CustomerFragment;
import com.robifr.ledger.ui.main.product.ProductFragment;
import com.robifr.ledger.ui.main.queue.QueueFragment;
import com.robifr.ledger.util.Compats;
import com.robifr.ledger.util.Enums;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
    implements BackStack, NavigationBarView.OnItemSelectedListener {
  public enum BottomNavigationTabTag {
    QUEUE(R.id.queue),
    CUSTOMER(R.id.customer),
    PRODUCT(R.id.productLayout);

    @IdRes private final int _resourceId;

    private BottomNavigationTabTag(int resourceId) {
      this._resourceId = resourceId;
    }

    @IdRes
    public int resourceId() {
      return this._resourceId;
    }
  }

  @Nullable private MainActivityBinding _activityBinding;
  @Nullable private BackStackNavigation _backStackNavigation;
  @Nullable private MainCreate _create;

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    final int nightThemeFlags =
        this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

    if (nightThemeFlags == Configuration.UI_MODE_NIGHT_YES) this.setTheme(R.style.AppTheme_Dark);
    else this.setTheme(R.style.AppTheme_Light);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    Objects.requireNonNull(item);

    return switch (item.getItemId()) {
      case android.R.id.home -> {
        this.onBackPressed();
        yield true;
      }

      case R.id.search -> {
        // Add search fragment on top of current fragment,
        // without removing current displayed fragment.
        //				this.getSupportFragmentManager()
        //					.beginTransaction()
        //					.add(R.id.main_framelayout, this._searchFragment, SearchFragment.class.toString())
        //					.addToBackStack(SearchFragment.class.toString())
        //					.hide(this._lastNavigatedFragment)
        //					.show(this._searchFragment)
        //					.commit();
        yield true;
      }

      default -> super.onOptionsItemSelected(item);
    };
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    //		if (!this._searchFragment.isVisible()) super.onBackPressed();
    //
    //		//Return to horizontal search result (initial one) when user are on vertically oriented
    // view.
    //		//Happened because clicked "see more" button, or the one with right-sided arrow icon.
    //		if (this._searchFragment.searchCustomerList().isVerticallyOriented()
    //			|| this._searchFragment.searchProductList().isVerticallyOriented()) {
    //			this._searchFragment.searchCustomerList().setVerticallyOriented(false);
    //			this._searchFragment.searchProductList().setVerticallyOriented(false);
    //
    //		//Close search fragment.
    //		} else {
    //			this._searchFragment.setVisible(false);
    //			this.getSupportFragmentManager().popBackStack();
    //		}
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(this._activityBinding);

    final BottomNavigationTabTag tab =
        Enums.valueOf(
            item.getItemId(), BottomNavigationTabTag.class, BottomNavigationTabTag::resourceId);

    // Prevent recursive.
    if (tab == null || tab.toString().equals(this.currentTabStackTag())) return true;

    // Note: Using `bottomNavigation.setSelectedItemId()` can often result in an infinite loop,
    //    even if the code placement is correct.
    this._activityBinding.bottomNavigation.getMenu().findItem(item.getItemId()).setChecked(true);
    this.navigateTabStack(tab.toString());
    return false;
  }

  @Override
  @NonNull
  public Map<String, Deque<Fragment>> navigationStacks() {
    Objects.requireNonNull(this._backStackNavigation);

    return this._backStackNavigation.navigationStacks();
  }

  @Override
  @Nullable
  public String currentTabStackTag() {
    Objects.requireNonNull(this._backStackNavigation);

    return this._backStackNavigation.currentTabStackTag();
  }

  @Override
  public void addTabStack(@NonNull String... tags) {
    Objects.requireNonNull(tags);
    Objects.requireNonNull(this._backStackNavigation);

    this._backStackNavigation.addTabStack(tags);
  }

  @Override
  public void removeTabStack(@NonNull String tag) {
    Objects.requireNonNull(tag);
    Objects.requireNonNull(this._backStackNavigation);

    this._backStackNavigation.removeTabStack(tag);
  }

  @Override
  public void pushFragmentStack(@NonNull String tabTag, @NonNull String fragmentTag) {
    Objects.requireNonNull(tabTag);
    Objects.requireNonNull(fragmentTag);
    Objects.requireNonNull(this._backStackNavigation);

    this._backStackNavigation.pushFragmentStack(tabTag, fragmentTag);
    this._onBackStackChanged(tabTag);
  }

  @Override
  public void pushFragmentStack(
      @NonNull String tabTag, @NonNull Fragment fragment, @NonNull String fragmentTag) {
    Objects.requireNonNull(tabTag);
    Objects.requireNonNull(fragment);
    Objects.requireNonNull(fragmentTag);
    Objects.requireNonNull(this._backStackNavigation);

    this._backStackNavigation.pushFragmentStack(tabTag, fragment, fragmentTag);
    this._onBackStackChanged(tabTag);
  }

  @Override
  @Nullable
  public Fragment popFragmentStack(@NonNull String tabTag) {
    Objects.requireNonNull(tabTag);
    Objects.requireNonNull(this._backStackNavigation);

    final Fragment poppedFragment = this._backStackNavigation.popFragmentStack(tabTag);

    this._onBackStackChanged(tabTag);
    return poppedFragment;
  }

  @Override
  public boolean navigateTabStack(@NonNull String tabTag) {
    Objects.requireNonNull(tabTag);
    Objects.requireNonNull(this._activityBinding);
    Objects.requireNonNull(this._backStackNavigation);

    final BottomNavigationTabTag bottomNavStack = BottomNavigationTabTag.valueOf(tabTag);

    // Select bottom navigation item when navigating between tab stack.
    if (bottomNavStack != null) {
      this._activityBinding
          .bottomNavigation
          .getMenu()
          .findItem(bottomNavStack.resourceId())
          .setChecked(true);
    }

    return this._backStackNavigation.navigateTabStack(tabTag);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstance) {
    this.onConfigurationChanged(this.getResources().getConfiguration());
    super.onCreate(savedInstance);

    this._activityBinding = MainActivityBinding.inflate(this.getLayoutInflater());
    this.setContentView(this._activityBinding.getRoot());

    this._create = new MainCreate(this);
    this._backStackNavigation =
        new BackStackNavigation(this.getSupportFragmentManager(), R.id.fragmentContainer);

    this._activityBinding.createButton.setOnClickListener(button -> this._create.openDialog());
    this._activityBinding.bottomNavigation.setOnItemSelectedListener(this);

    // Hide annoying tooltip text.
    this._activityBinding
        .bottomNavigation
        .findViewById(R.id.queue)
        .setOnLongClickListener(Compats::hideTooltipText);
    this._activityBinding
        .bottomNavigation
        .findViewById(R.id.customer)
        .setOnLongClickListener(Compats::hideTooltipText);
    this._activityBinding
        .bottomNavigation
        .findViewById(R.id.productLayout)
        .setOnLongClickListener(Compats::hideTooltipText);

    if (savedInstance == null) {
      final QueueFragment queueFragment =
          (QueueFragment)
              new QueueFragment.Factory()
                  .instantiate(this.getClassLoader(), QueueFragment.class.getName());
      final CustomerFragment customerFragment =
          (CustomerFragment)
              new CustomerFragment.Factory()
                  .instantiate(this.getClassLoader(), CustomerFragment.class.getName());
      final ProductFragment productFragment =
          (ProductFragment)
              new ProductFragment.Factory()
                  .instantiate(this.getClassLoader(), ProductFragment.class.getName());

      this.addTabStack(
          BottomNavigationTabTag.QUEUE.toString(),
          BottomNavigationTabTag.CUSTOMER.toString(),
          BottomNavigationTabTag.PRODUCT.toString());
      this.pushFragmentStack(
          BottomNavigationTabTag.QUEUE.toString(), queueFragment, QueueFragment.class.toString());
      this.pushFragmentStack(
          BottomNavigationTabTag.CUSTOMER.toString(),
          customerFragment,
          CustomerFragment.class.toString());
      this.pushFragmentStack(
          BottomNavigationTabTag.PRODUCT.toString(),
          productFragment,
          CustomerFragment.class.toString());
      this.navigateTabStack(BottomNavigationTabTag.QUEUE.toString());
    }
  }

  private void _onBackStackChanged(@NonNull String tabTag) {
    Objects.requireNonNull(tabTag);
    Objects.requireNonNull(this._activityBinding);

    final BottomNavigationTabTag bottomNavTabStack =
        Enums.nameOf(tabTag, BottomNavigationTabTag.class);
    final Deque<Fragment> bottomNavStack =
        bottomNavTabStack != null
            ? this.navigationStacks().get(bottomNavTabStack.toString())
            : null;
    final boolean isOnTopBottomNavStack = bottomNavStack != null && bottomNavStack.size() <= 1;
    final int mainActivityViewsVisibility = isOnTopBottomNavStack ? View.VISIBLE : View.GONE;

    final TypedValue backgroundColor = new TypedValue();
    final int navigationBarColor =
        mainActivityViewsVisibility == View.VISIBLE
            ? com.google.android.material.R.attr.colorSurface
            : android.R.attr.colorBackground;

    // Due to bottom sheet navigation on main activity uses a different color — color surface.
    // Match system navigation bar color into it, otherwise with current background.
    this.getTheme().resolveAttribute(navigationBarColor, backgroundColor, true);
    this.getWindow().setNavigationBarColor(this.getColor(backgroundColor.resourceId));

    // Hide views on main activity when user navigating to another fragment other than
    // the one defined as top of the stack — queue, customer, and product — inside bottom
    // navigation.
    this._activityBinding.bottomNavigation.setVisibility(mainActivityViewsVisibility);
    this._activityBinding.createButton.setVisibility(mainActivityViewsVisibility);
  }
}
