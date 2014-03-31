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
import android.widget.Toast;

public class Buscar extends ListActivity {
	String[] nombres;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buscar);

		/*
		 * Creamos en handler, con el que comunicamos el thread cliente con el
		 * thread principal. Tambi�n es el que crea la listview.
		 */
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				 * Sacamos el array "NOMBRES" del mensaje enviado al handler.
				 * Con "NOMBRES" creamos la listview
				 */
				String[] nombres = msg.getData().getStringArray("NOMBRES");

				setListAdapter(new ArrayAdapter<String>(Buscar.this,
						android.R.layout.simple_list_item_1, nombres));
			}
		};
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
			Toast toast = Toast.makeText(getApplicationContext(),
					"Pr�ximamente...", Toast.LENGTH_SHORT);
			toast.show();
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
			 * cogemos la respuesta (comprobamos que la comunicaci�n ha sido
			 * correcta y leemos el contenido que nos interesa de la respuesta),
			 * a trav�s del bufferedreader rellenamos un string, con el que
			 * creamos el JSONArray, luego el JSONObject, y partir de este
			 * �ltimo, el string "NOMBRES". Por �ltimo, pasamos este string como
			 * parte del mensaje que mandaremos al handler.
			 */
			Log.d("A", "Empezando el thread");
			// Recibimos el nombre de usuario de la activity anterior
			Bundle bundle2 = getIntent().getExtras();
			String Usuario = bundle2.getString("USUARIO");

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
					 * No podemos pasar cualquier mensaje al handler, as� que lo
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
