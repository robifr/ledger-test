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

package com.robifr.ledger.assetbinding;

import android.content.Context;
import android.util.TypedValue;
import android.webkit.JavascriptInterface;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import com.robifr.ledger.R;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class JsInterface {
  @NonNull public static final String NAME = "Android";

  @NonNull private final Context _context;

  public JsInterface(@NonNull Context context) {
    this._context = Objects.requireNonNull(context);
  }

  @NonNull
  public static String argbToRgbaHex(@ColorInt int hexColor) {
    final int a = (hexColor >> 24) & 0xFF;
    final int r = (hexColor >> 16) & 0xFF;
    final int g = (hexColor >> 8) & 0xFF;
    final int b = hexColor & 0xFF;
    return String.format("#%02X%02X%02X%02X", r, g, b, a);
  }

  public static int dpToCssPx(@NonNull Context context, float dp) {
    Objects.requireNonNull(context);

    return (int) (dp / context.getResources().getDisplayMetrics().density);
  }

  @NonNull
  @JavascriptInterface
  public String colorHex(@NonNull String colorName) {
    Objects.requireNonNull(colorName);

    // Attempt to directly resolve the color.
    int colorRes =
        this._context
            .getResources()
            .getIdentifier(colorName, "color", this._context.getPackageName());
    if (colorRes == 0) {
      // If it fails, resolve it as an attribute.
      final int attrRes =
          this._context
              .getResources()
              .getIdentifier(colorName, "attr", this._context.getPackageName());
      TypedValue typedValue = new TypedValue();

      if (attrRes != 0 && this._context.getTheme().resolveAttribute(attrRes, typedValue, true)) {
        colorRes = typedValue.resourceId;
      }
    }

    final int color = colorRes != 0 ? colorRes : R.color.black;
    return JsInterface.argbToRgbaHex(this._context.getColor(color));
  }

  @NonNull
  @JavascriptInterface
  public String formatCurrencyWithUnit(
      double amount, @NonNull String language, @NonNull String country, @NonNull String symbol) {
    return CurrencyFormat.formatWithUnit(BigDecimal.valueOf(amount), language, country, symbol);
  }
}
