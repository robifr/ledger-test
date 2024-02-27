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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.Deque;
import java.util.Map;

public interface BackStack {
  /**
   * Map of back stacks where the key is the tab tag, and stack of fragments as value. It's when
   * each tab have their own back stack, mostly for bottom navigation. Do note that each tab can't
   * have multiple fragments with the same tag.
   *
   * <pre>
   *  tab 1 = fragment 1 -> fragment 2
   *  tab 2 = fragment 3 -> fragment 4 -> fragment 5
   *  tab 3 = fragment 6
   * </pre>
   */
  @NonNull
  public Map<String, Deque<Fragment>> navigationStacks();

  @Nullable
  public String currentTabStackTag();

  public void addTabStack(@NonNull String... tabTags);

  public void removeTabStack(@NonNull String tabTag);

  /**
   * Adds a new fragment to both specified tab stack and {@link FragmentManager}. If a fragment with
   * the same tag (identified by {@code fragmentTag}) already exists in the tab stack, it will be
   * replaced and moved to the top without being added multiple times.
   */
  public void pushFragmentStack(
      @NonNull String tabTag, @NonNull Fragment fragment, @NonNull String fragmentTag);

  /**
   * Unlike {@link BackStack#pushFragmentStack(String, Fragment, String)}, this method will only
   * searches for a fragment in the specified tab stack (identified by {@code fragmentTag}) and
   * moves it to the top without adding it multiple times. In short, only use this method if the
   * fragment already pushed beforehand with {@link BackStack#pushFragmentStack(String, Fragment,
   * String)} method.
   */
  public void pushFragmentStack(@NonNull String tabTag, @NonNull String fragmentTag);

  /** Pops and removes the top fragment from the specified tab stack. */
  @Nullable
  public Fragment popFragmentStack(@NonNull String tabTag);

  /**
   * Navigates between tab stacks, displaying the first fragment of the specified tab stack on the
   * screen.
   *
   * @return {@code true} if the operation was successful, {@code false} otherwise.
   */
  public boolean navigateTabStack(@NonNull String tabTag);
}
