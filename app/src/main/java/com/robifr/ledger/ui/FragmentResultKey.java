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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.robifr.ledger.util.Tag;

/**
 * This interface is useful when transferring data between fragments. By using enum package path,
 * which is unique, to generate either request (required by {@link FragmentManager}) or result key
 * (required by {@link Bundle}).
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
 *      REQUEST_ID
 *    }
 *
 *    public enum Result implements FragmentResultKey {
 *      RESULTED_ID
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
 */
public interface FragmentResultKey {
  /** Generated key from {@link Tag#fullName(Class)} to transfer data between fragments. */
  @NonNull
  public default String key() {
    return this instanceof Enum e ? Tag.fullName(e) : Tag.fullName(this.getClass());
  }
}
