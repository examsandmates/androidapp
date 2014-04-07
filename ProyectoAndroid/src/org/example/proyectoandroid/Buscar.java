package org.example.proyectoandroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Buscar extends ListActivity {
	ProgressDialog dialog;
	String[] nombres;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buscar);

		/*
		 * Creamos en handler, con el que comunicamos el thread cliente con el
		 * thread principal. También es el que crea la listview.
		 */
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				 * Sacamos el array "NOMBRES" del mensaje enviado al handler.
				 * Con "NOMBRES" creamos la listview
				 */
				dialog.dismiss();
				
				String[] nombres = msg.getData().getStringArray("NOMBRES");

				setListAdapter(new ArrayAdapter<String>(Buscar.this,
						android.R.layout.simple_list_item_1, nombres));
			}
		};
		
		dialog = ProgressDialog.show(Buscar.this, "",
				"Conectando con el servidor", true);
		dialog.setCancelable(true);
		
		/* Iniciamos el thread cliente */
		HTTPThread t = new HTTPThread(nombres, handler);
		t.start();
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		String usuario_selec = (listView.getItemAtPosition(position).toString());

		Intent i = new Intent(this, Info_usuario.class);
		i.putExtra("USUARIO", usuario_selec);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_gente, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_principal:
			Intent perfilbienv = new Intent(this, Perfil_bienvenida.class);
			startActivity(perfilbienv);
			return true;
		case R.id.menu_editarperfil:
			Intent perfiledit = new Intent(this, Perfil_editar.class);
			startActivity(perfiledit);
			return true;
		case R.id.menu_grupos:
			Intent grupos = new Intent(this, Grupos.class);
			startActivity(grupos);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Creamos nuestra clase HTTPThread */
	class HTTPThread extends Thread {
		String[] nombres;
		Handler handler;

		public HTTPThread(String[] n, Handler h) {
			nombres = n;
			handler = h;
		}

		@Override
		public void run() {
			/*
			 * Creamos el HTTPClient, ejecutamos POST en la url especificada,
			 * cogemos la respuesta (comprobamos que la comunicación ha sido
			 * correcta y leemos el contenido que nos interesa de la respuesta),
			 * a través del bufferedreader rellenamos un string, con el que
			 * creamos el JSONArray, luego el JSONObject, y partir de este
			 * último, el string "NOMBRES". Por último, pasamos este string como
			 * parte del mensaje que mandaremos al handler.
			 */
			Log.d("A", "Empezando el thread");
			//Recibimos el nombre de usuario de las sharedpreferences
			SharedPreferences prefs = getSharedPreferences("MiUsuario",
					Context.MODE_PRIVATE);
			String Usuario = prefs
					.getString("user_activo", "por_defecto@email.com");
			Log.d("Probando", "Usuario activo por shared: " + Usuario);

			ArrayList<NameValuePair> nameValuePair;
			nameValuePair = new ArrayList<NameValuePair>();
			nameValuePair.add(new BasicNameValuePair("CONSULTA_USUARIO",
					Usuario));
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/Gente2.php");
			try {
				Log.d("B", "Dentro del try");
				post.setEntity(new UrlEncodedFormEntity(nameValuePair));
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

					Log.d("Mitchel", "resp= " + resp);

					/*
					 * A partir del string "resp", creamos el JSONArray, del que
					 * vamos "separando" cada JSONObject que lo compone. A su
					 * vez, de cada JSONObject extraemos el string del valor con
					 * nombre "user", y vamos rellenando el array de strings
					 * "NOMBRES"
					 */
					JSONArray json = new JSONArray(resp);

					nombres = new String[json.length()];

					for (int i = 0; i < json.length(); i++) {
						JSONObject js = json.getJSONObject(i);
						nombres[i] = js.getString("user");
						Log.d("NOMBRE", nombres[i]);
					}
					/*
					 * No podemos pasar cualquier mensaje al handler, así que lo
					 * obtenemos con "obtainMessage"
					 */
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putStringArray("NOMBRES", nombres);
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				// TODO
			}
		}
	}
}
