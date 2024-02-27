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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import java.util.Objects;

/**
 * This interface is useful when transferring data between fragments. By using enum package path,
 * which is unique, to generate either request — required by {@link FragmentManager} — or result key
 * — required by {@link Bundle}.
 *
 * <pre>
 * DataReceiverFragment.java
 * {@code
 *  @Override
 *  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
 *    ...
 *
 *    final FragmentResultListener listener =
 *        (requestKey, result) -> {
 *          if (requestKey.equals(DataSenderFragment.Request.REQUEST_ID.key())) {
 *            // Data received: 100
 *            Long id = result.getLong(DataSenderFragment.Result.RESULTED_ID.key());
 *            ...
 *          }
 *        });
 *
 *    //Listening request from another fragment.
 *    this.getParentFragmentManager().setFragmentResultListener(
 *       DataSenderFragment.Request.REQUEST_ID.key(),
 *       this.getViewLifecycleOwner(),
 *       listener);
 *  }
 * }
 *
 * DataSenderFragment.java
 * {@code
 *  public DataSenderFragment extends Fragment {
 *    public enum Request implements FragmentResultKey {
 *      REQUEST_ID;
 *
 *      @Override
 *      @NonNull
 *      public String key() {
 *        return FragmentResultKey.generateKey(this);
 *      }
 *    }
 *
 *    public enum Result implements FragmentResultKey {
 *      RESULTED_ID;
 *
 *      @Override
 *      @NonNull
 *      public String key() {
 *        return FragmentResultKey.generateKey(this);
 *      }
 *    }
 *  }
 * }
 *
 * Somewhere in DataSenderFragment.java
 * {@code
 *  final Bundle bundle = new Bundle();
 *  bundle.putLong(Result.RESULTED_ID.key(), 100L);
 *
 *  //Send result.
 *  this.getParentFragmentManager().setFragmentResult(Request.RESULTED_ID.key(), bundle);
 * }
 * </pre>
 *
 * @apiNote Should only be used/implemented exclusively with enum.
 */
public interface FragmentResultKey {
  /** Key from {@link FragmentResultKey#generateKey(Enum)} to transfer data between fragments. */
  @NonNull
  public String key();

  @NonNull
  static <E extends Enum<E> & FragmentResultKey> String generateKey(@NonNull E constant) {
    Objects.requireNonNull(constant);

    final String enumPath = constant.getDeclaringClass().getName();
    return enumPath + "." + constant.name();
  }
}
