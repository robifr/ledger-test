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

package com.robifr.ledger.components;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.AttrRes;
import androidx.annotation.GravityInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textview.MaterialTextView;

public class AutoScrollTextView extends MaterialTextView {
  @GravityInt private int _originalGravity;

  public AutoScrollTextView(@NonNull Context context) {
    this(context, null);
  }

  public AutoScrollTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AutoScrollTextView(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this._originalGravity = this.getGravity();

    this.setMaxLines(1);
    this.setSingleLine();
    this.setEllipsize(TextUtils.TruncateAt.MARQUEE);
    this.setMarqueeRepeatLimit(-1);
    this.setHorizontallyScrolling(true);
  }

  @Override
  public void setGravity(@GravityInt int gravity) {
    super.setGravity(gravity);
    this._originalGravity = gravity;
  }

  @Override
  protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    if (visibility == View.VISIBLE) this.setSelected(true);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    // When the text is intended to have gravity set to end/right, the left side
    // of the text may get clipped when the text stops after scroll. This ensures that when
    // the text is long enough, it will use start/left gravity.
    if (this.getWidth() - this.getPaddingLeft() - this.getPaddingRight()
        < this.getPaint().measureText(this.getText().toString())) {
      super.setGravity(Gravity.START); // Don't set `_originalGravity` to the start.
    } else {
      this.setGravity(this._originalGravity);
    }
  }

  @Override
  protected void onTextChanged(
      @NonNull CharSequence text, int start, int lengthBefore, int lengthAfter) {
    super.onTextChanged(text, start, lengthBefore, lengthAfter);
    // When an item in recycler view is deleted, the remaining items may inherit incorrect gravity
    // settings. For example, if the first item has short text (gravity set to end/right) and
    // the second item has long text (gravity set to start/left), deleting the second item can
    // cause the first item to incorrectly retain the start/left gravity due to reused item
    // from the deleted one. Setting it to their original gravity in this method ensure that such
    // thing won't happen.
    this.setGravity(this._originalGravity);
  }
}
