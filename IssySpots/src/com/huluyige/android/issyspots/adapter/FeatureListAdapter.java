package com.huluyige.android.issyspots.adapter;

import com.esri.core.map.Graphic;
import com.huluyige.android.issyspots.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FeatureListAdapter extends ArrayAdapter<Graphic> {
	private Context context;
	private Graphic[] graphics;

	public FeatureListAdapter(Context context, Graphic[] graphics) {
		super(context, R.layout.view_featurelistitem, graphics);
		this.context = context;
		this.graphics = graphics;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder holder;
		if(convertView==null){
			convertView= inflater.inflate(R.layout.view_featurelistitem, parent,
					false);
			holder = new ViewHolder();
			holder.Text=(TextView) convertView
					.findViewById(R.id.tv_featurelist);
			convertView.setTag(holder);
		}
		else{
			 holder = (ViewHolder) convertView.getTag();
		}
		holder.Text.setText(graphics[position].getAttributeValue("nom").toString());
		return convertView;
	}
	
	static class ViewHolder {   
	    TextView  Text;
	}
}
