package com.skray.skraylibs.view;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.skray.skraylibs.utils.SkrayUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

public class SkrayListView extends PullToRefreshListView{
	
	OnDismissCallback dismissCallback;
	OnItemClickListener itemClickCallback;
	
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
	
	public void setOnDismissHandler(OnDismissCallback callback){
		this.dismissCallback = callback;
	}
	
	
	
	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		Log.e("onItemclicklistener in skraylistview", listener.toString());
//		mRefreshableView.setOnItemClickListener(listener);
		super.setOnItemClickListener(listener);
	}

	public void setAdapter(BaseAdapter adapter){
		SwingBottomInAnimationAdapter swingBottonInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
		swingBottonInAnimationAdapter.setInitialDelayMillis(3000);
		swingBottonInAnimationAdapter.setAbsListView(mRefreshableView);
		super.setAdapter(swingBottonInAnimationAdapter);
	}

}
