package org.example.proyectoandroid;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Perfil_editar extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new EditarPerfilFragment())
				.commit();

	}

	// Creamos el desplegable de opciones
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_editarperfil, menu);
		return true;
	}

	// Damos acciones a las opciones
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_principal:
			onBackPressed();
			//Intent perfilbienv = new Intent(this, Perfil_bienvenida.class);
			//startActivity(perfilbienv);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	public void onBackPressed() {
		// TODO Completar el Handler
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				String actualizado = msg.getData().getString("RESPUESTA");
				Log.d("Probando", "EL STATUS DE ACTUALIZAR PERFIL ES:"
						+ actualizado);
				if (actualizado.equals("si")) {
					Intent perfilbienv = new Intent(Perfil_editar.this,
							Perfil_bienvenida.class);
					startActivity(perfilbienv);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"Ha ocurrido algún problema, vuelve a intentarlo.",
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		};

		int contador = 0;

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (pref.getBoolean("TRA", false))
			contador++;
		if (pref.getBoolean("ELE", false))
			contador++;
		if (pref.getBoolean("OAE", false))
			contador++;
		if (pref.getBoolean("EST", false))
			contador++;
		if (pref.getBoolean("SII", false))
			contador++;
		if (pref.getBoolean("MOD", false))
			contador++;
		if (pref.getBoolean("VEH", false))
			contador++;
		if (pref.getBoolean("MAQ", false))
			contador++;
		if (pref.getBoolean("CLI", false))
			contador++;
		if (pref.getBoolean("SEG", false))
			contador++;

		String[] asignaturas = new String[contador];
		String[] id_asignaturas = new String[contador];

		int aux = 0;
		while (aux < contador) {
			if (pref.getBoolean("TRA", false)) {
				asignaturas[aux] = "Ing. del transporte";
				id_asignaturas[aux] = "TRA";
				aux++;
				contador--;
			}
			if (pref.getBoolean("ELE", false)) {
				asignaturas[aux] = "Tecnologia Electrica";
				id_asignaturas[aux] = "ELE";
				aux++;
				contador--;
			}
			if (pref.getBoolean("OAE", false)) {
				asignaturas[aux] = "Admon. Empresas";
				id_asignaturas[aux] = "OAE";
				aux++;
				contador--;
			}
			if (pref.getBoolean("EST", false)) {
				asignaturas[aux] = "Teoria de estructuras";
				id_asignaturas[aux] = "EST";
				aux++;
				contador--;
			}
			if (pref.getBoolean("SII", false)) {
				asignaturas[aux] = "Sistemas informaticos industriales";
				id_asignaturas[aux] = "SII";
				aux++;
				contador--;
			}
			if (pref.getBoolean("MOD", false)) {
				asignaturas[aux] = "Modelado e identifiacion de sistemas";
				id_asignaturas[aux] = "MOD";
				aux++;
				contador--;
			}
			if (pref.getBoolean("VEH", false)) {
				asignaturas[aux] = "Vehiculos";
				id_asignaturas[aux] = "VEH";
				aux++;
				contador--;
			}
			if (pref.getBoolean("MAQ", false)) {
				asignaturas[aux] = "Maquinas hidraulicas y termicas";
				id_asignaturas[aux] = "MAQ";
				aux++;
				contador--;
			}
			if (pref.getBoolean("CLI", false)) {
				asignaturas[aux] = "Climatizacion de edificios y refrigeracion";
				id_asignaturas[aux] = "CLI";
				aux++;
				contador--;
			}
			if (pref.getBoolean("SEG", false)) {
				asignaturas[aux] = "Seg. industrial y Repres. grafica de proyecto";
				id_asignaturas[aux] = "SEG";
				aux++;
				contador--;
			}
		}

		// Obtengo el usuario activo con las sharedpreferences
		SharedPreferences prefs = getSharedPreferences("MiUsuario",
				Context.MODE_PRIVATE);
		String usuario = prefs
				.getString("user_activo", "por_defecto@email.com");
		Log.d("Probando", "Usuario activo por shared: " + usuario);

		HTTPThread t = new HTTPThread(aux, asignaturas, id_asignaturas,
				usuario, handler);
		t.start();
	}

	class HTTPThread extends Thread {
		int numero;
		String[] asignaturas;
		String[] id_asignaturas;
		String usuario;
		Handler handler;

		// Handler handler;

		public HTTPThread(int cont, String[] asig, String[] id_asig,
				String user, Handler h) {
			numero = cont;
			asignaturas = asig;
			id_asignaturas = id_asig;
			usuario = user;
			handler = h;
		}

		@Override
		public void run() {

			try {
				// Construímos nuestro JSON
				JSONArray consultaJson = new JSONArray();

				JSONObject jsonObjectUser = new JSONObject();
				jsonObjectUser.accumulate("user", usuario);
				consultaJson.put(jsonObjectUser);

				for (int i = 0; i < numero; i++) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.accumulate("id_asign", id_asignaturas[i]);
					jsonObject.accumulate("asign", asignaturas[i]);
					consultaJson.put(jsonObject);
				}

				Log.d("Probando", "Me saca: " + consultaJson.toString());

				URL obj = new URL(
						"http://examsandmates.web44.net/examapp/ActAsign.php");
				HttpURLConnection con = (HttpURLConnection) obj
						.openConnection();

				// Add reuqest header
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				String urlParameters = "Mensaje=" + consultaJson.toString();

				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(
						con.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();

				int responseCode = con.getResponseCode();
				System.out.println("\nSending 'POST' request to URL : ");
				System.out.println("Post parameters : " + urlParameters);
				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				System.out.println(response.toString());

				JSONArray json = new JSONArray(response.toString());
				JSONObject js = json.getJSONObject(0);
				String resp_act = js.getString("status");
				
				Log.d("Probando", "LO QUE ME PASA DAVID: "+resp_act);
				
				// TODO Mensaje para el handler
				Message msg = handler.obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putString("RESPUESTA", resp_act);
				msg.setData(bundle);
				handler.sendMessage(msg);

			} catch (Exception e) {

			}

		}

	}

}
