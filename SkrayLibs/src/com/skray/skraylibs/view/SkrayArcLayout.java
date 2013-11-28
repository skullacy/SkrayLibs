package com.skray.skraylibs.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Animation.AnimationListener;

import com.capricorn.ArcLayout;

public class SkrayArcLayout extends ArcLayout{
	
	public SkrayArcLayout(Context context) {
		super(context);
	}
	
	public SkrayArcLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.e("in SkrayArcLayout onMeasure", "haha");
		final int radius = mRadius = computeRadius(Math.abs(mToDegrees - mFromDegrees), getChildCount(), mChildSize,
				mChildPadding, MIN_RADIUS);
		Log.e("radius", "radius : "+String.valueOf(radius));
		
		final int size = radius + mChildSize + mChildPadding + mLayoutPadding * 2;

		setMeasuredDimension(size, size);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.EXACTLY));
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.e("onLayout", "SkrayArcLayout haha!!");
		Log.e("onLayout", String.format("Left : %d , Top : %d, Right : %d, Bottom : %d  ", l,t,r,b));
		final int centerX = getWidth() - mLayoutPadding * 2 - mChildPadding * 2;
		final int centerY = getHeight() - mLayoutPadding * 2 - mChildPadding * 2;
		final int radius = mExpanded ? mRadius : 0;
		
		final int childCount = getChildCount();
		final float perDegrees = (mToDegrees - mFromDegrees) / (childCount - 1);
		
		float degrees = mFromDegrees;
		for (int i = 0; i < childCount; i++) {
			Rect frame = computeChildFrame(centerX, centerY, radius, degrees, mChildSize);
			degrees += perDegrees;
			getChildAt(i).layout(frame.left, frame.top, frame.right, frame.bottom);
		}
	}
	
	@Override
	protected void bindChildAnimation(final View child, final int index, final long duration) {
        final boolean expanded = mExpanded;
        final int centerX = getWidth() - mLayoutPadding * 2 - mChildPadding * 2;
        final int centerY = getHeight() - mLayoutPadding * 2 - mChildPadding * 2;
        final int radius = expanded ? 0 : mRadius;

        final int childCount = getChildCount();
        final float perDegrees = (mToDegrees - mFromDegrees) / (childCount - 1);
        Rect frame = computeChildFrame(centerX, centerY, radius, mFromDegrees + index * perDegrees, mChildSize);

        final int toXDelta = frame.left - child.getLeft();
        final int toYDelta = frame.top - child.getTop();

        Interpolator interpolator = mExpanded ? new AccelerateInterpolator() : new OvershootInterpolator(1.5f);
        final long startOffset = computeStartOffset(childCount, mExpanded, index, 0.1f, duration, interpolator);

        Animation animation = mExpanded ? createShrinkAnimation(0, toXDelta, 0, toYDelta, startOffset, duration,
                interpolator) : createExpandAnimation(0, toXDelta, 0, toYDelta, startOffset, duration, interpolator);

        final boolean isLast = getTransformedIndex(expanded, childCount, index) == childCount - 1;
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isLast) {
                    postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            onAllAnimationsEnd();
                        }
                    }, 0);
                }
            }
        });

        child.setAnimation(animation);
    }
	
}
