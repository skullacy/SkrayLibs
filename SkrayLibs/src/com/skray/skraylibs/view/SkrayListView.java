package com.skray.skraylibs.view;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.AnimationStyle;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;

public class SkrayListView extends PullToRefreshListView{
	
	public SkrayListView(Context context) {
		super(context);
	}

	public SkrayListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SkrayListView(Context context, Mode mode) {
		super(context, mode);
	}

	public SkrayListView(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}

}
