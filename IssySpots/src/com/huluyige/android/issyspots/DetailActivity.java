package com.huluyige.android.issyspots;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.esri.core.map.Graphic;

public class DetailActivity extends SherlockActivity {
	private TextView tv_name;
	private TextView tv_resume;
	private TextView tv_adresse;
	private TextView tv_tel;
	private TextView tv_url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getSupportActionBar().show();
		setContentView(R.layout.detail);
		tv_name = (TextView) findViewById(R.id.detail_name);
		tv_resume = (TextView) findViewById(R.id.detail_resume);
		tv_adresse = (TextView) findViewById(R.id.detail_adresse);
		tv_tel = (TextView) findViewById(R.id.detail_tel);
		tv_url = (TextView) findViewById(R.id.detail_url);

		Intent intent = getIntent();
		Graphic selectedGraphic = (Graphic) intent.getSerializableExtra("data");
		getSupportActionBar().setTitle(
				(String) selectedGraphic.getAttributeValue("nom"));

//		tv_name.setText((String) selectedGraphic.getAttributeValue("nom"));
		tv_resume.setText((String) selectedGraphic.getAttributeValue("resume"));
		String adresse= selectedGraphic.getAttributeValue("adresse")+" "+selectedGraphic.getAttributeValue("cp").toString()+" "+selectedGraphic.getAttributeValue("ville");
		tv_adresse.setText(adresse);
		tv_tel.setText(selectedGraphic.getAttributeValue("tel")+"");
		tv_url.setText(Html.fromHtml("<a href="+selectedGraphic.getAttributeValue("web")+">"+ selectedGraphic.getAttributeValue("web")+"</a>"));
		super.onCreate(savedInstanceState);
	}
}
