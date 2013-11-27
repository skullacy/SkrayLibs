package com.skray.skraylibs.utils;

import java.io.ByteArrayOutputStream;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * @class SkrayUtils
 * @author skullacy
 * this class is utility class of skraylibs package
 * please make static type when you write new method.
 */
public class SkrayUtils {
	
	/**
	 * @brief set enum type for method (convertBitmapToBase64String)
	 */
	public enum ImageType{
		jpg(Bitmap.CompressFormat.JPEG),
		png(Bitmap.CompressFormat.PNG);
		
		private Bitmap.CompressFormat format;
		ImageType(Bitmap.CompressFormat format){
			this.format = format;
		}
		Bitmap.CompressFormat getFormat(){
			return format;
		}
	}
	
	public static void skraylog(String tag, String msg){
		Log.i(tag, msg);
	}

	/**
	 * @param s
	 * @return Boolean type
	 */
	public static boolean isNullOrEmpty(final CharSequence s) {
        return (s == null || s.equals("") || s.equals("null") || s.equals("NULL"));
    }
	
	public static String convertBitmapToBase64String(Bitmap bitmap, SkrayUtils.ImageType type, int quality){
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		bitmap.compress(type.getFormat(), quality, bao);
		byte[] ba = bao.toByteArray();
		return Base64.encodeToString(ba, Base64.DEFAULT);
	}
	
	/**
	 * @refactoring 예정
	 * @param context
	 * @return
	 */
	public static String getAccount(Context context){
		
		AccountManager manager = AccountManager.get(context);
		Account[] accounts =  manager.getAccounts();
		for(Account account : accounts) {
			if(account.type.equals("com.google")){		//이러면 구글 계정 구분 가능
				return account.name;
			}
		}
		return null;
	}
	
	/**
	 * @refactoring 예정
	 * @param listView
	 */
	public static void setListViewHeight(ListView listView){
		ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
	}
	
	
}
