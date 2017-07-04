package com.byodl.widgets;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Item top and bottom padding decoration
 */
public class PaddingItemDecoration extends RecyclerView.ItemDecoration {

    private float paddingH = 0; //padding in pixels
    private float paddingV = 0; //padding in pixels

    public PaddingItemDecoration(float paddingH, float paddingV) {
		this.paddingH = paddingH;
        this.paddingV = paddingV;
	}

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        float leftPadding = paddingH;
        float rightPadding = paddingH;
        float topPadding = paddingV;
        float bottomPadding = paddingV;
        outRect.set(Math.round(leftPadding),Math.round(topPadding),Math.round(rightPadding), Math.round(bottomPadding));
    }
}
