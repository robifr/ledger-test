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
import androidx.lifecycle.LiveData;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link LiveData} is good for maintaining lifecycle-aware state, like on configuration changes
 * (device rotation). But for events like displaying a snackbar, toast, and navigation, the observer
 * should only observe once and they shouldn't observe after configuration changes. This event will
 * let multiple observers to observe the changes for once.
 *
 * <pre>
 * MainFragment.java
 * {@code
 *  viewModel.snackbar().observe(this, new LiveDataEvent.Observer<>(
 *      message ->
 *          //Message received: "Hello"
 *          Snackbar.make(this, message, Snackbar.LENGTH_LONG).show());
 * }
 *
 * MainViewModel.java
 * {@code
 *  public class MainViewModel extends ViewModel {
 *    @NonNull private MutableLiveData<LiveDataEvent<String>> _snackbar = new MutableLiveData<>();
 *    ...
 *  }
 * }
 *
 * Somewhere in MainViewModel.java
 * {@code this._snackbar.setValue(new LiveDataEvent<>("Hello")); }
 * </pre>
 */
public class LiveDataEvent<T> {
  @Nullable private final T _value;
  private boolean _isHandled = false;

  public LiveDataEvent(@Nullable T value) {
    this._value = value;
  }

  @Nullable
  public T getValueIfNotHandled() {
    if (this._isHandled) return null;

    this._isHandled = true;
    return this._value;
  }

  @Nullable
  public T value() {
    return this._value;
  }

  public boolean isHandled() {
    return this._isHandled;
  }

  public interface Handler<T> {
    void onEventUnHandled(@Nullable T value);
  }

  public static class Observer<T> implements androidx.lifecycle.Observer<LiveDataEvent<T>> {
    @NonNull private final Handler<T> _handler;

    public Observer(@NotNull Handler<T> handler) {
      this._handler = Objects.requireNonNull(handler);
    }

    @Override
    public void onChanged(@NonNull LiveDataEvent<T> event) {
      Objects.requireNonNull(event);

      this._handler.onEventUnHandled(event.getValueIfNotHandled());
    }
  }
}
