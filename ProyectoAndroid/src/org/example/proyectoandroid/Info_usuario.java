package org.example.proyectoandroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.R.color;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Info_usuario extends ListActivity {
	// Definimos las variables en las que guardaremos los datos de cada usuario
	String[] nombres;
	String usuario_nombre;
	String usuario_contacto;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_usuario);

		TextView texto;
		// Recibimos el nombre de usuario de la actividad anterior
		Bundle bundle = getIntent().getExtras();
		String Usuario = bundle.getString("USUARIO");
		// Mostramos por pantalla de qué usuario es el perfil
		texto = (TextView) findViewById(R.id.TextView01);
		texto.setText("Este es el perfil de " + Usuario);

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String[] nombres = msg.getData().getStringArray("NOMBRES");
				String usuario_nombre = msg.getData().getString(
						"USUARIO_NOMBRE");
				String usuario_contacto = msg.getData().getString(
						"USUARIO_CONTACTO");
				// Mostramos por pantalla el nombre de usuario
				TextView texto_nombre_usuario;
				texto_nombre_usuario = (TextView) findViewById(R.id.TextView02);
				texto_nombre_usuario.setText("Nombre de usuario: "
						+ usuario_nombre);
				// Mostramos por pantalla el mail de contacto
				TextView texto_contacto_usuario;
				texto_contacto_usuario = (TextView) findViewById(R.id.TextView03);
				texto_contacto_usuario.setText("Dirección de contacto: "
						+ usuario_contacto);
				texto_contacto_usuario.setTextColor(Color.WHITE);

				// Mostramos por pantalla una listview con sus asignaturas
				// Me da error al intentar mostrar el StringArray "nombres"
				// directamente. No sé por qué.
				int tam = nombres.length;
				String[] asignaturas = new String[tam - 1];
				for (int i = 0; i < tam - 1; i++) {
					asignaturas[i] = nombres[i];
				}
				setListAdapter(new ArrayAdapter<String>(Info_usuario.this,
						android.R.layout.test_list_item, asignaturas));
			}
		};
		HTTPThread t = new HTTPThread(Usuario, handler);
		t.start();
	}

	public void lanzarCorreo(View v) {
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		String[] recipients = new String[] { usuario_contacto, "", };
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Exams&Mates: petición de contacto.");
		emailIntent
				.putExtra(android.content.Intent.EXTRA_TEXT,
						"¡Hola! Te he encontrado con Exams&Mates y quiero contactar contigo.");
		emailIntent.setType("message/rfc822");
		final PackageManager pm = getPackageManager();
		final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent,
				0);
		ResolveInfo best = null;
		for (final ResolveInfo info : matches)
			if (info.activityInfo.packageName.endsWith(".gm")
					|| info.activityInfo.name.toLowerCase().contains("gmail"))
				best = info;
		if (best != null)
			emailIntent.setClassName(best.activityInfo.packageName,
					best.activityInfo.name);
		startActivity(Intent.createChooser(emailIntent, "Send mail client :"));
		finish();
	}

	/* Creamos nuestra clase HTTPThread */
	class HTTPThread extends Thread {
		String consulta;
		Handler handler;

		public HTTPThread(String u, Handler h) {
			consulta = u;
			handler = h;
		}

		@Override
		public void run() {
			// Pareja nombre-valor con la que paso el usuario a la petición
			Log.d("A", "Empezando el thread");
			ArrayList<NameValuePair> nameValuePair;
			nameValuePair = new ArrayList<NameValuePair>();
			nameValuePair.add(new BasicNameValuePair("CONSULTA_USUARIO",
					consulta));
			// Comenzamos la conexión
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/DatosUser.php");
			try {
				Log.d("B", "Dentro del try");
				// Añadimos el nameValuePair al POST, dentro del try porque
				// puede dar una excepcion
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

					Log.d("C", "resp= " + resp);

					/*
					 * A partir del string "resp", creamos el JSONArray, del que
					 * vamos "separando" cada JSONObject que lo compone. A su
					 * vez, de cada JSONObject extraemos el string del valor con
					 * nombre "user", y vamos rellenando el array de strings
					 * "NOMBRES"
					 */
					JSONArray json = new JSONArray(resp);

					String[] nombres = new String[json.length()];
					Log.d("OBJETOS JSON", "El número es: " + json.length());

					JSONObject js = json.getJSONObject(0);
					usuario_nombre = js.getString("name");
					usuario_contacto = js.getString("email");
					Log.d("NOMBRE", usuario_nombre);
					Log.d("EMAIL", usuario_contacto);

					for (int i = 1; i < json.length(); i++) {
						JSONObject js2 = json.getJSONObject(i);
						nombres[i - 1] = js2.getString("subj");
						Log.d("ASIGNATURAS", nombres[i - 1]);
					}

					// No podemos pasar cualquier mensaje al handler, así que lo
					// obtenemos con "obtainMessage"
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("USUARIO_NOMBRE", usuario_nombre);
					bundle.putString("USUARIO_CONTACTO", usuario_contacto);
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
