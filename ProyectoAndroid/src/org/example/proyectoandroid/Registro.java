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

public class Registro extends Activity {
	
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registro);
	}

	public void completarRegistro(View view) {
		Context context;
		Intent intent;
		
		Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String nombres = msg.getData().getString("STATUS");

				if (nombres.equals("si")) {
					
					dialog.dismiss();

					Toast toast = Toast.makeText(getApplicationContext(),
							"El registro se ha completado correctamente",
							Toast.LENGTH_LONG);
					toast.show();

					Intent intent = new Intent(Registro.this, Log_in.class);
					startActivity(intent);
					
				} else {
					
					dialog.dismiss();
					
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

		// Comprobamos si hay algún campo vacío
		if ((consulta_nick.length() == 0) || (consulta_nombre.length() == 0)
				|| (consulta_mail.length() == 0)
				|| (consulta_pass1.length() == 0)
				|| (consulta_pass2.length() == 0)) {
			
			Toast toast = Toast.makeText(getApplicationContext(),
					"No has completado todos los campos", Toast.LENGTH_LONG);
			toast.show();
			
			// Comprobamos que las contraseñas coinciden
		} else if (consulta_pass1.equals(consulta_pass2) == false) {
			
			Toast toast = Toast.makeText(getApplicationContext(),
					"Las contraseñas no coinciden", Toast.LENGTH_LONG);
			toast.show();
			
		}
		
		// Codificamos la contraseña y mandamos los datos a servidor
		else {
			String pass_md5 = md5(consulta_pass1);

			dialog = ProgressDialog.show(Registro.this, "",
					"Conectando con el servidor", true);
			dialog.setCancelable(true);

			HTTPThread t = new HTTPThread(consulta_nick, consulta_nombre,
					consulta_mail, pass_md5, handler);
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
			
			// Llamos al thread
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/Register.php");
			
			try {
				post.setEntity(new UrlEncodedFormEntity(nameValuePair));
				HttpResponse response = client.execute(post);
				
				if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					
					//Tomamos la respuesta del servidor
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

					// Definimos el mensaje para el handler
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("STATUS", nombres);
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
			}
		}
	}
}
