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

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import androidx.annotation.NonNull;
import com.robifr.ledger.util.Tag;
import java.util.Objects;

public class LocalWebChrome extends WebChromeClient {
  @Override
  public boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage) {
    Objects.requireNonNull(consoleMessage);

    final String message =
        consoleMessage.message()
            + " at "
            + consoleMessage.sourceId()
            + ":"
            + consoleMessage.lineNumber();

    switch (consoleMessage.messageLevel()) {
      case ERROR -> Log.e(Tag.simpleName(LocalWebChrome.class), message);
      default -> Log.d(Tag.simpleName(LocalWebChrome.class), message);
    }

    return super.onConsoleMessage(consoleMessage);
  }
}
