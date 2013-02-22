package org.open.easytrip.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.RadioGroup;


/**
 * Exact copy of WrapLinearLayout, but extending a RadioGroup
 */
public class WrapRadioGroup extends RadioGroup {
	private final static int PAD_H = 2, PAD_V = 2; // Space between child views.
	private int mHeight;

	public WrapRadioGroup(Context context) {
		super(context);
	}

	public WrapRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);
		final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
		final int count = getChildCount();
		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();
		int childHeightMeasureSpec;
		if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST)
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
		else
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		mHeight = 0;
		for(int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if(child.getVisibility() != GONE) {
				child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), childHeightMeasureSpec);
	            final LinearLayout.LayoutParams childLp = (LinearLayout.LayoutParams)child.getLayoutParams();
	            final int childw;
	            if (childLp.width == LinearLayout.LayoutParams.MATCH_PARENT || childLp.width == LinearLayout.LayoutParams.FILL_PARENT) 
	            	childw = width;
	            else
	            	childw = child.getMeasuredWidth();
				mHeight = Math.max(mHeight, child.getMeasuredHeight() + PAD_V);
				if(xpos + childw > width) {
					xpos = getPaddingLeft();
					ypos += mHeight;
				}
				xpos += childw + PAD_H;
			}
		}
		if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			height = ypos + mHeight;
		} else if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			if(ypos + mHeight < height) {
				height = ypos + mHeight;
			}
		}
		height += 5; // Fudge to avoid clipping bottom of last row.
		setMeasuredDimension(width, height);
	} // end onMeasure()

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = r - l;
		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();
		for(int i = 0; i < getChildCount(); i++) {
			final View child = getChildAt(i);
			if(child.getVisibility() != GONE) {
	            final LinearLayout.LayoutParams childLp = (LinearLayout.LayoutParams)child.getLayoutParams();
	            final int childw;
	            if (childLp.width == LinearLayout.LayoutParams.MATCH_PARENT || childLp.width == LinearLayout.LayoutParams.FILL_PARENT) 
	            	childw = width;
	            else
	            	childw = child.getMeasuredWidth();
				final int childh = child.getMeasuredHeight();
				if(xpos + childw > width) {
					xpos = getPaddingLeft();
					ypos += mHeight;
				}
				child.layout(xpos, ypos, xpos + childw, ypos + childh);
				xpos += childw + PAD_H;
			}
		}
	} // end onLayout()

}

