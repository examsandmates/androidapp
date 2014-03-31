package org.example.proyectoandroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Registro extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registro);
	}

	public void completarRegistro(View view) {
		Context context;
		Intent intent;
		// Handler
		Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String nombres = msg.getData().getString("STATUS");

				if (nombres.equals("si")) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"El registro se ha completado correctamente",
							Toast.LENGTH_LONG);
					toast.show();

					Intent intent = new Intent(Registro.this,
							Log_in.class);
					startActivity(intent);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"No se ha completado correctamente el registro",
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		};
		// Comprobamos que se ha completado correctamente el registro
		EditText nick = (EditText) findViewById(R.id.editText1);
		String consulta_nick = (String) nick.getText().toString();
		EditText nombre = (EditText) findViewById(R.id.editText2);
		String consulta_nombre = (String) nombre.getText().toString();
		EditText mail = (EditText) findViewById(R.id.editText3);
		String consulta_mail = (String) mail.getText().toString();
		EditText pass1 = (EditText) findViewById(R.id.editText4);
		String consulta_pass1 = (String) pass1.getText().toString();
		EditText pass2 = (EditText) findViewById(R.id.editText5);
		String consulta_pass2 = (String) pass2.getText().toString();

		Log.d("Amigo", "Comprobando:" + consulta_nick);
		Log.d("Amigo", "Comprobando:" + consulta_nombre);
		Log.d("Amigo", "Comprobando:" + consulta_mail);
		Log.d("Amigo", "Comprobando:" + consulta_pass1);
		Log.d("Amigo", "Comprobando:" + consulta_pass2);
		// Comprobamos si hay alg�n campo vac�o
		if ((consulta_nick.length() == 0) || (consulta_nombre.length() == 0)
				|| (consulta_mail.length() == 0)
				|| (consulta_pass1.length() == 0)
				|| (consulta_pass2.length() == 0)) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"No has completado todos los campos", Toast.LENGTH_LONG);
			toast.show();
			// Comprobamos que las contrase�as coinciden
		} else if (consulta_pass1.equals(consulta_pass2) == false) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Las contrase�as no coinciden", Toast.LENGTH_LONG);
			toast.show();
		}
		// Codificamos la contrase�a y mandamos los datos a servidor
		else {
			String pass_md5 = md5(consulta_pass1);
			Log.d("Amigo", "VAMOS A INICIAR EL THREAD!!");
			HTTPThread t = new HTTPThread(consulta_nick, consulta_nombre,
					consulta_mail, pass_md5, handler);
			t.start();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registro, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static String md5(String s) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes(), 0, s.length());
			String hash = new BigInteger(1, digest.digest()).toString(16);
			return hash;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/* Creamos nuestra clase HTTPThread */
	class HTTPThread extends Thread {
		String nick, nombre, mail, pass;
		Handler handler;

		public HTTPThread(String u, String v, String w, String x, Handler h) {
			nick = u;
			nombre = v;
			mail = w;
			pass = x;
			handler = h;
		}

		@Override
		public void run() {
			// Parejas nombre-valor con las que paso los datos al servidor
			ArrayList<NameValuePair> nameValuePair;
			nameValuePair = new ArrayList<NameValuePair>();
			nameValuePair.add(new BasicNameValuePair("CONSULTA_NICK", nick));
			nameValuePair
					.add(new BasicNameValuePair("CONSULTA_NOMBRE", nombre));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_MAIL", mail));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_PASS", pass));
			// Comenzamos la conexi�n
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/Register.php");
			try {
				Log.d("Amigo", "Dentro del try");
				// A�adimos el nameValuePair al POST, dentro del try porque
				// puede dar una excepcion
				post.setEntity(new UrlEncodedFormEntity(nameValuePair));
				HttpResponse response = client.execute(post);
				if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					// Tomamos la respuesta del servdor
					String resp = "";
					String linea;

					while ((linea = reader.readLine()) != null) {
						resp += linea;
					}

					Log.d("Amigo", "resp= " + resp);

					// Extramos la respuesta del JSON
					JSONArray json = new JSONArray(resp);

					String nombres;

					JSONObject js = json.getJSONObject(0);
					nombres = js.getString("status");
					Log.d("Amigo", "Status devuelto: " + nombres);

					// Definimos el mensaje para el handler
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("STATUS", nombres);
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				// TODO
			}
		}
	}
}
