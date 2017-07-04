package com.byodl.utils;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class SquareCropTransformation implements Transformation
{

	private int mWidth;
	private int mHeight;

	@Override public Bitmap transform(Bitmap source) {
		int size = Math.min(source.getWidth(), source.getHeight());

		mWidth = 0;
		mHeight = 0;

		Bitmap bitmap = Bitmap.createBitmap(source, mWidth, mHeight, size, size);
		if (bitmap != source) {
			source.recycle();
		}

		return bitmap;
	}

	@Override public String key() {
		return "CropSquareTransformation(width=" + mWidth + ", height=" + mHeight + ")";
	}
}