package org.example.proyectoandroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Grupos extends ListActivity {
	ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grupos);

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				String[] grupos = msg.getData().getStringArray("GRUPOS");

				dialog.dismiss();
				
				setListAdapter(new ArrayAdapter<String>(Grupos.this,
						android.R.layout.simple_list_item_1, grupos));
			}
		};

		dialog = ProgressDialog.show(Grupos.this, "",
				"Conectando con el servidor", true);
		dialog.setCancelable(true);
		
		HTTPThread t = new HTTPThread(handler);
		t.start();

	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		String grupo_selec = (listView.getItemAtPosition(position).toString());

		Intent i = new Intent(this, Grupos_info.class);
		i.putExtra("GRUPO", grupo_selec);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.grupos, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_creagrupo:
			Intent creagrupo = new Intent(this, Grupos_crear.class);
			startActivity(creagrupo);

			return true;
		case R.id.menu_principal:
			Intent perfiledit = new Intent(this, Perfil_bienvenida.class);
			startActivity(perfiledit);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Creamos nuestra clase HTTPThread */
	class HTTPThread extends Thread {
		Handler handler;
		String[] grupos;

		public HTTPThread(Handler h) {
			handler = h;
		}

		@Override
		public void run() {

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/Grupos.php");
			
			try {
				Log.d("Probando", "Dentro del try");
				post.setEntity(null);
				HttpResponse response = client.execute(post);
				
				if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					
					String resp = "";
					String linea;

					while ((linea = reader.readLine()) != null) {
						resp += linea;
					}

					Log.d("Probando", "resp= " + resp);

					JSONArray json = new JSONArray(resp);

					grupos = new String[json.length()];

					for (int i = 0; i < json.length(); i++) {
						JSONObject js = json.getJSONObject(i);
						grupos[i] = js.getString("name");
						Log.d("Probando", grupos[i]);
					}

					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putStringArray("GRUPOS", grupos);
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
			}
		}
	}
}
