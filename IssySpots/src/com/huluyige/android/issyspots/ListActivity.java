package com.huluyige.android.issyspots;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.huluyige.android.issyspots.adapter.FeatureListAdapter;

public class ListActivity extends SherlockActivity {

	private String type;
	private String whereClause;
	private String url;
	private Graphic[] graphics;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getSupportActionBar().show();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		listView = (ListView) findViewById(R.id.listView1);
		Intent intent = getIntent();

		type = intent.getStringExtra("type");
		url = intent.getStringExtra("url");
		if (type.equals("Tous les types")) {
			whereClause = "1=1";
		} else {
			whereClause = MainActivity.getRightWhereClause(type);
		}
		new IssyQueryTask().execute(url);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(ListActivity.this, graphics[arg2].getAttributeValue("nom").toString(), Toast.LENGTH_SHORT).show();
			}
		});

	}

	protected void updateData(FeatureSet results) {
		this.graphics = results.getGraphics();
		FeatureListAdapter adapter = new FeatureListAdapter(this,
				graphics);
		listView.setAdapter(adapter);
	}

	private class IssyQueryTask extends AsyncTask<String, Integer, FeatureSet> {

		@Override
		protected FeatureSet doInBackground(String... params) {
			QueryTask task = new QueryTask(params[0]);
			Query query = new Query();
			query.setWhere(whereClause);
			String[] outFields = { "objectid", "nom" };
			query.setOutFields(outFields);
			query.setReturnGeometry(true);
			FeatureSet results = null;
			try {
				results = task.execute(query);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return results;
		}

		@Override
		protected void onPostExecute(FeatureSet results) {
			updateData(results);
			super.onPostExecute(results);
		}
	}

}
