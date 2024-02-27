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

package com.robifr.ledger.ui.searchable.product.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.RecyclerViewAdapterOld;
import com.robifr.ledger.ui.RecyclerViewHolderOld;
import com.robifr.ledger.ui.searchable.SearchableNarrowCardComponent;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class SearchProductHorizontalHolder extends RecyclerViewHolderOld
    implements View.OnClickListener {
  private SearchableNarrowCardComponent _narrowCard;
  private ProductModel _productModel;

  public SearchProductHorizontalHolder(
      @NonNull AppCompatActivity activity,
      @NonNull RecyclerViewAdapterOld adapter,
      @NonNull View view) {
    super(activity, adapter, view);
    this._narrowCard = new SearchableNarrowCardComponent(this._activity, (MaterialCardView) view);

    this._narrowCard.rootLayout().setOnClickListener(this);
  }

  @Override
  public void bind(int index) {
    this._productModel = (ProductModel) this._adapter.list().get(index);
    final String price =
        CurrencyFormat.format(BigDecimal.valueOf(this._productModel.price()), "id", "ID");

    //		final ConstraintLayout.LayoutParams imageViewParams =
    // (ConstraintLayout.LayoutParams)this._narrowCard
    //			.imageComponents()
    //			.imageView()
    //			.getLayoutParams();
    //		final ShapeAppearanceModel.Builder cardShaped = ShapeAppearanceModel.builder(
    //			this._activity,
    //			com.google.android.material.R.styleable.MaterialShape_shapeAppearance,
    //			R.style.Shape_Card
    //		);
    //		final int imagePadding =
    //			(int)(this._activity.getResources().getDimension(R.dimen.cardlist_padding) / 1.5);
    //		final String imageText = this._productModel.name().trim()
    //			.substring(0, Math.min(1, this._productModel.name().length()));
    //
    ////		this._narrowCard.titleView().setText(this._productModel.name());
    ////		this._narrowCard.descriptionView().setText(price);
    //		this._narrowCard.imageComponents().rootLayout()
    //			.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
    //		this._narrowCard.imageComponents().imageView().setShapeAppearanceModel(cardShaped.build());
    //		this._narrowCard.imageComponents().textView().setText(imageText);
    //		this._narrowCard.imageComponents().textView().setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
    //
    //		imageViewParams.width = (int)TypedValue
    //			.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130f,
    // this._activity.getResources().getDisplayMetrics());
    //		imageViewParams.dimensionRatio = "1:1"; //To ensure the width match height and vice versa.
    //		this._narrowCard.imageComponents().imageView().setLayoutParams(imageViewParams);
  }

  // TODO: Implement.
  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);
  }
}
