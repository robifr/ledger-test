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
    return this instanceof Enum<?> e ? Tag.fullName(e) : Tag.fullName(this.getClass());
  }
}
