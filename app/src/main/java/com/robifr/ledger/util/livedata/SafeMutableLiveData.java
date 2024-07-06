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

package com.robifr.ledger.util.livedata;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.Objects;

public class SafeMutableLiveData<T> extends MutableLiveData<T> implements SafeLiveData<T> {
  public SafeMutableLiveData(@NonNull T value) {
    super(value);
  }

  /**
   * @return Current value
   */
  @NonNull
  @Override
  public T getValue() {
    return Objects.requireNonNull(super.getValue());
  }

  @Override
  public void setValue(@NonNull T value) {
    Objects.requireNonNull(value);

    super.setValue(value);
  }

  @Override
  public void postValue(@NonNull T value) {
    Objects.requireNonNull(value);

    super.postValue(value);
  }

  @Override
  public void observe(@NonNull LifecycleOwner owner, @NonNull SafeObserver<? super T> observer) {
    Objects.requireNonNull(owner);
    Objects.requireNonNull(owner);

    super.observe(
        owner,
        value -> {
          if (value != null) observer.onChanged(value);
        });
  }

  @Override
  public void observeForever(@NonNull SafeObserver<? super T> observer) {
    Objects.requireNonNull(observer);

    super.observeForever(
        value -> {
          if (value != null) observer.onChanged(value);
        });
  }

  @NonNull
  @Override
  public LiveData<T> toLiveData() {
    return this;
  }
}
