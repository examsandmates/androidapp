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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Log_in extends Activity {
	//Definimos el objeto de ventana de progreso
	ProgressDialog dialog;

	// Método que crea el layout.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_in);
	}

	// Método que ejecuta el botón "Conectar"
	public void lanzarPerfilBienvenida(View view) {
		Context context;
		Intent intent;
		// Creamos el handler
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String nombres = msg.getData().getString("STATUS");
				Log.d("Amigo", "EL STATUS DE COMPROBACION ES:" + nombres);
				if (nombres.equals("si")) {
					Intent perfilbienv = new Intent(Log_in.this,
							Perfil_bienvenida.class);
					perfilbienv.addFlags(perfilbienv.FLAG_ACTIVITY_CLEAR_TOP);
					perfilbienv.addFlags(perfilbienv.FLAG_ACTIVITY_SINGLE_TOP);
					// Pasamos el nombre de usuario activo a la siguente
					// activity
					EditText user = (EditText) findViewById(R.id.editText1);
					String consulta_user = (String) user.getText().toString();
					perfilbienv.putExtra("USUARIO", consulta_user);
					Log.d("Amigo", "Enviando desde LOG IN " + consulta_user);
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
			// Ahora codifico el pass
			String consulta_pass = md5(no_cod_pass);
			// PROGRESSDIALOG
			dialog = ProgressDialog.show(Log_in.this, "", "Conectando con el servidor",true);
			dialog.setCancelable(true);
			// Llama al thread de conexión con el servidor
			HTTPThread t = new HTTPThread(consulta_user, consulta_pass, handler);
			t.start();
			// Intent perfilbienv = new Intent(this, Perfil_bienvenida.class);
			// startActivity(perfilbienv);
			// }
		}
	}

	// Método que codifica nuestro pass antes de enviarlo
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

	// Método que lanza la pantalla de registro
	public void lanzarRegistro(View view) {
		Intent registro = new Intent(this, Registro.class);
		startActivity(registro);
	}

	public void onBackPressed() {
		finish();
		moveTaskToBack(true);
	}

	/* Creamos nuestra clase HTTPThread */
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
			// Pareja nombre-valor con la que paso el usuario a la petición
			Log.d("Amigo", "Empezando el thread");
			Log.d("Amigo", "User" + consulta_user);
			Log.d("Amigo", "Pass" + consulta_pass);
			ArrayList<NameValuePair> nameValuePair;
			nameValuePair = new ArrayList<NameValuePair>();
			nameValuePair.add(new BasicNameValuePair("CONSULTA_USUARIO",
					consulta_user));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_PASS",
					consulta_pass));
			Log.d("Amigo", "Dentro del try" + consulta_pass);
			// Comenzamos la conexión
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/Login.php");
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
					// COMENTO DE AQUÍ EN ADELANTE PORQUE PARA EL LOGIN MI
					// RESPUESTA SERÁ DISTINTA

					String resp = "";
					String linea;

					while ((linea = reader.readLine()) != null) {
						resp += linea;
					}

					Log.d("Amigo", "resp= " + resp);

					// A partir del string "resp", creamos el JSONArray, del que
					// vamos "separando" cada JSONObject que lo compone. A su
					// vez, de cada JSONObject extraemos el string del valor con
					// nombre "user", y vamos rellenando el array de string
					// "NOMBRES"

					JSONArray json = new JSONArray(resp);

					String nombres;

					JSONObject js = json.getJSONObject(0);
					nombres = js.getString("status");
					Log.d("Amigo", "Status devuelto: " + nombres);
					// for (int i = 1; i < json.length(); i++) {
					// JSONObject js2 = json.getJSONObject(i);
					// nombres[i - 1] = js2.getString("subj");
					// Log.d("ASIGNATURAS", nombres[i - 1]);
					// }

					// No podemos pasar cualquier mensaje al handler, así que
					// obtenemos con "obtainMessage"
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
