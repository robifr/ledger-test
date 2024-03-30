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

package com.robifr.ledger.ui;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Deprecated
public class BackStackNavigation implements BackStack {
  @NonNull private final FragmentManager _fragmentManager;
  @IdRes private final int _containerId;

  /**
   * @see BackStack#navigationStacks()
   */
  @NonNull private final HashMap<String, ArrayDeque<Fragment>> _stacks = new HashMap<>();

  @Nullable private String _currentTabStackTag;

  @Deprecated
  public BackStackNavigation(@NonNull FragmentManager fragmentManager, @IdRes int containerId) {
    this._fragmentManager = Objects.requireNonNull(fragmentManager);
    this._containerId = containerId;
  }

  @Override
  @NonNull
  public Map<String, Deque<Fragment>> navigationStacks() {
    return Collections.unmodifiableMap(this._stacks);
  }

  @Override
  @Nullable
  public String currentTabStackTag() {
    return this._currentTabStackTag;
  }

  @Override
  public void addTabStack(@NonNull String... tabTags) {
    Objects.requireNonNull(tabTags);

    Arrays.stream(tabTags).forEach(tag -> this._stacks.put(tag, new ArrayDeque<>()));
  }

  @Override
  public void removeTabStack(@NonNull String tabTag) {
    Objects.requireNonNull(tabTag);

    final Deque<Fragment> selectedStack = this._stacks.get(tabTag);
    if (selectedStack == null) return;

    final FragmentTransaction transaction = this._fragmentManager.beginTransaction();

    selectedStack.forEach(transaction::remove); // Remove fragments stack from fragment manager.
    transaction.commit();
    this._stacks.remove(tabTag);

    if (tabTag.equals(this._currentTabStackTag)) this._currentTabStackTag = null;
  }

  @Override
  public void pushFragmentStack(
      @NonNull String tabTag, @NonNull Fragment fragment, @NonNull String fragmentTag) {
    Objects.requireNonNull(tabTag);
    Objects.requireNonNull(fragment);
    Objects.requireNonNull(fragmentTag);

    final Deque<Fragment> selectedStack = this._stacks.get(tabTag);
    if (selectedStack == null) return;

    final FragmentTransaction transaction = this._fragmentManager.beginTransaction();
    final Deque<Fragment> currentStack = this._stacks.get(this._currentTabStackTag);
    final Fragment fragmentOnManager = this._fragmentManager.findFragmentByTag(fragmentTag);

    // Add fragment to fragment manager so that we can display it on screen later.
    if (fragmentOnManager == null) {
      transaction.add(this._containerId, fragment, fragmentTag).hide(fragment);
    }

    // Hide current fragment on screen and show again right before committing the transaction.
    // Just to make it easier to handle which fragment to hide/show.
    if (currentStack != null && !currentStack.isEmpty()) transaction.hide(currentStack.getFirst());

    // Then after the fragment tag has been applied from the `FragmentTransaction#add()`.
    // Replace and move the fragment to top in case they already exist on stack.
    selectedStack.stream()
        .filter(frag -> fragmentTag.equals(frag.getTag()))
        .findFirst()
        .ifPresent(selectedStack::remove);
    selectedStack.push(fragment);

    // Show top fragment from current tab stack to screen.
    if (currentStack != null && !currentStack.isEmpty()) transaction.show(currentStack.getFirst());

    transaction.commit();
  }

  @Override
  public void pushFragmentStack(@NonNull String tabTag, @NonNull String fragmentTag) {
    Objects.requireNonNull(tabTag);
    Objects.requireNonNull(fragmentTag);

    final Deque<Fragment> selectedStack = this._stacks.get(tabTag);
    if (selectedStack == null) return;

    final FragmentTransaction transaction = this._fragmentManager.beginTransaction();
    final Deque<Fragment> currentStack = this._stacks.get(this._currentTabStackTag);

    // Hide current fragment on screen and show again right before committing the transaction.
    // Just to make it easier to handle which fragment to hide/show.
    if (currentStack != null && !currentStack.isEmpty()) transaction.hide(currentStack.getFirst());

    // Remove fragment with equal tag and push to top of the stack.
    selectedStack.stream()
        .filter(frag -> fragmentTag.equals(frag.getTag()))
        .findFirst()
        .ifPresent(
            frag -> {
              selectedStack.remove(frag);
              selectedStack.push(frag);
            });

    // Show top fragment from current tab stack to screen.
    if (currentStack != null && !currentStack.isEmpty()) transaction.show(currentStack.getFirst());

    transaction.commit();
  }

  @Override
  @Nullable
  public Fragment popFragmentStack(@NonNull String tabTag) {
    Objects.requireNonNull(tabTag);

    final ArrayDeque<Fragment> selectedStack = this._stacks.get(tabTag);
    if (selectedStack == null || selectedStack.isEmpty()) return null;

    final FragmentTransaction transaction = this._fragmentManager.beginTransaction();
    final Fragment topFragment = selectedStack.pop();

    // Show the current top fragment (was second in stack before),
    // in case when the fragment is pushed to current tab stack.
    if (tabTag.equals(this._currentTabStackTag) && !selectedStack.isEmpty()) {
      transaction.show(selectedStack.getFirst());
    }

    transaction.remove(topFragment).commit();
    return topFragment;
  }

  @Override
  public boolean navigateTabStack(@NonNull String tabTag) {
    Objects.requireNonNull(tabTag);

    final String oldTabStackTag = this._currentTabStackTag;
    this._currentTabStackTag = tabTag;

    final ArrayDeque<Fragment> selectedStack = this._stacks.get(tabTag);
    if (selectedStack == null || selectedStack.isEmpty()) return false;

    final FragmentTransaction transaction = this._fragmentManager.beginTransaction();
    final ArrayDeque<Fragment> currentStack = this._stacks.get(oldTabStackTag);

    // There's nothing to hide if there's no current tab stack.
    if (currentStack != null && !currentStack.isEmpty()) transaction.hide(currentStack.getFirst());

    transaction.show(selectedStack.getFirst()).commit();
    return true;
  }
}
