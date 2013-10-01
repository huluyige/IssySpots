package com.huluyige.android.issyspots.views;


import com.huluyige.android.issyspots.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalloutView extends LinearLayout{

	private String name;
	private String category;
	private TextView tvName;
	private TextView tvCategory;
	private Context context;
	
	public CalloutView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root=(View) inflater.inflate(R.layout.callout_content,null);
	}

}
