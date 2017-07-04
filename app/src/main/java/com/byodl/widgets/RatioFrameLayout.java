package com.byodl.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.byodl.R;


/**
 * Frame layout with aspect ratio
 */
public class RatioFrameLayout extends FrameLayout {
    public static final int AUTO_HEIGHT = 0;
    public static final int AUTO_WIDTH = 1;
    @SuppressWarnings("unused")
    public static final int AUTO_AUTO = 2;
	public static final float ZERO_RATIO = 0f;
	private float ratio = ZERO_RATIO;
	private int adjust = AUTO_HEIGHT;

	public RatioFrameLayout(Context context) {
		super(context);
		init(context,null);
	}

	public RatioFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}
	public RatioFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs);
	}
	private void init(Context context,AttributeSet attrs) {
		TypedArray arr = context.obtainStyledAttributes(attrs,
				R.styleable.RatioFrameLayout);
		ratio = arr.getFloat(R.styleable.RatioFrameLayout_width_height_ratio,ZERO_RATIO);
        adjust = arr.getInt(R.styleable.RatioFrameLayout_auto_adjust,AUTO_HEIGHT);
		arr.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (ratio!=ZERO_RATIO){
			// get maximum view width
            if (adjust==AUTO_HEIGHT) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                if (width != 0) {
                    int height = (int) (width / ratio);

                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                            MeasureSpec.EXACTLY);
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                            MeasureSpec.EXACTLY);
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
            else if (adjust==AUTO_WIDTH) {
                int height = MeasureSpec.getSize(heightMeasureSpec);
                if (height != 0) {
                    int width = (int) (height * ratio);

                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                            MeasureSpec.EXACTLY);
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                            MeasureSpec.EXACTLY);
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
            else{
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int ratioHeight = (int)(width/ratio);
                int height = MeasureSpec.getSize(heightMeasureSpec);
                int ratioWidth = (int)(height*ratio);
                if (width!=0&&ratioHeight<height||height==0)
                    height = ratioHeight;
                else if (ratioWidth < width || width == 0)
                    width = ratioWidth;
                else
                    return;
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                        MeasureSpec.EXACTLY);
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                        MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

		}
	}
    @SuppressWarnings("unused")
    public void setRatio(float ratio){
        this.ratio = ratio;
        invalidate();
    }
    @SuppressWarnings("unused")
    public void setAutoAdjust(int adjust){
        this.adjust = adjust;
    }
}
