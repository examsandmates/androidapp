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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Log_in extends Activity {
	// Ventana de progreso
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_in);
	}

	// Método ejecutado por el botón "Conectar"
	@SuppressLint("HandlerLeak")
	public void lanzarPerfilBienvenida(View view) {
		// Handler
		Handler handler = new Handler() {
			@SuppressWarnings("static-access")
			@Override
			public void handleMessage(Message msg) {
				String nombres = msg.getData().getString("STATUS");
				Log.d("Amigo", "EL STATUS DE COMPROBACION ES:" + nombres);

				if (nombres.equals("si")) {
					Intent perfilbienv = new Intent(Log_in.this,
							Perfil_bienvenida.class);
					perfilbienv.addFlags(perfilbienv.FLAG_ACTIVITY_CLEAR_TOP);
					perfilbienv.addFlags(perfilbienv.FLAG_ACTIVITY_SINGLE_TOP);

					/*
					 * Pasamos el nombre de usuario activo a la siguente
					 * activity
					 */

					EditText user = (EditText) findViewById(R.id.editText1);
					String consulta_user = (String) user.getText().toString();
					perfilbienv.putExtra("USUARIO", consulta_user);

					// TODO Hecho con sahredpreferences
					SharedPreferences prefs = getSharedPreferences(
							"MiUsuario", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("user_activo", consulta_user);
					editor.commit();
					//TODO Hecho con sharedpreferences

					dialog.dismiss();

					startActivity(perfilbienv);

				} else {
					dialog.dismiss();
					Toast toast = Toast.makeText(getApplicationContext(),
							"Nombre o pass incorrectos", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		};

		// Recupero el texto de los "editText"
		EditText user = (EditText) findViewById(R.id.editText1);
		String consulta_user = (String) user.getText().toString();
		EditText pass = (EditText) findViewById(R.id.editText2);
		String no_cod_pass = (String) pass.getText().toString();

		// Muestro Toast si alguno de los campos no está relleno
		if ((consulta_user.length() == 0) || (no_cod_pass.length() == 0)) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Nombre o contraseña no introducidos", Toast.LENGTH_LONG);
			toast.show();
		} else {

			// Codificamos el pass
			String consulta_pass = md5(no_cod_pass);

			dialog = ProgressDialog.show(Log_in.this, "",
					"Conectando con el servidor", true);
			dialog.setCancelable(true);

			// Thread de conexión con el servidor
			HTTPThread t = new HTTPThread(consulta_user, consulta_pass, handler);
			t.start();
		}
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

	// Método ejecutado por el texto "Regístrate"
	public void lanzarRegistro(View view) {
		Intent registro = new Intent(this, Registro.class);
		startActivity(registro);
	}

	public void onBackPressed() {
		finish();
		moveTaskToBack(true);
	}

	class HTTPThread extends Thread {
		String consulta_user;
		String consulta_pass;
		Handler handler;

		public HTTPThread(String u, String v, Handler h) {
			consulta_user = u;
			consulta_pass = v;
			handler = h;
		}

		@Override
		public void run() {
			// nameValuePair con la que paso el usuario a la petición
			Log.d("Probando", "Empezando el thread");
			Log.d("Probando", "User" + consulta_user);
			Log.d("Probando", "Pass" + consulta_pass);

			ArrayList<NameValuePair> nameValuePair;
			nameValuePair = new ArrayList<NameValuePair>();
			nameValuePair.add(new BasicNameValuePair("CONSULTA_USUARIO",
					consulta_user));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_PASS",
					consulta_pass));

			// Objeto HttpClient
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/Login.php");

			try {
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

					Log.d("Probando", "resp= " + resp);

					JSONArray json = new JSONArray(resp);
					JSONObject js = json.getJSONObject(0);
					String nombres;

					nombres = js.getString("status");
					Log.d("Amigo", "Status devuelto: " + nombres);

					// Mensaje para el handler
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
