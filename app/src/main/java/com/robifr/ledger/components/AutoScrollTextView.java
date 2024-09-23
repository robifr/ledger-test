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
