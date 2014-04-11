package org.example.proyectoandroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;

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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class Grupos_crear extends Activity {
	ProgressDialog dialog;

	// Variables tipo View para elegir y mostrar fecha y hora
	private TextView displayTime, pickTime, pDisplayDate, pPickDate;
	private int pHour, pMinute, pDay, pMonth, pYear;
	
	// Identificadores para elegir fecha u hora
	static final int TIME_DIALOG_ID = 0;
	static final int DATE_DIALOG_ID = 1;

	//Accion a realizar cuando se elige una hora
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			pHour = hourOfDay;
			pMinute = minute;
			updateDisplay();
		}
	};
	
	//Accion a realizar cuando se elige una fecha
	private DatePickerDialog.OnDateSetListener pDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			pYear = year;
			pMonth = monthOfYear;
			pDay = dayOfMonth;
			updateDisplayDate();
			
		}
	};

	// Mostramos hora elegida en el TextView
	private void updateDisplay() {
		displayTime.setText(new StringBuilder().append(pad(pHour)).append(":")
				.append(pad(pMinute)));
	}

	// Mostramos fecha elegida en el TextView
	private void updateDisplayDate() {
		pDisplayDate.setText(new StringBuilder()
				// El dato "mes" comienza en 0. Debemos sumar 1
		        .append(pDay).append("/").append(pMonth + 1).append("/")
				.append(pYear).append(" "));
	}

	
	// Anadimos 0 a los días o meses de una cifra
	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grupos_crear);

		// Inicializamos los Views a usar para el tiempo
		displayTime = (TextView) findViewById(R.id.textView7);
		pickTime = (TextView) findViewById(R.id.textView6);

		pDisplayDate = (TextView) findViewById(R.id.textView5);
		pPickDate = (TextView) findViewById(R.id.textView4);

		// Listener al pulsar eleccion de hora
		pickTime.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}
		});

		// Listener al pulsar eleccion de fecha
		pPickDate.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		// Obtenemos el tiempo actual del sistema
		final Calendar cal = Calendar.getInstance();
		pDay = cal.get(Calendar.DAY_OF_MONTH);
		pMonth = cal.get(Calendar.MONTH);
		pYear = cal.get(Calendar.YEAR);
		pHour = cal.get(Calendar.HOUR_OF_DAY);
		pMinute = cal.get(Calendar.MINUTE);

		// Mostramos el tiempo actual 
		updateDisplay();
		updateDisplayDate();

		Spinner sp = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.spinner_lista, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(adapter);
	}

	public void completarGrupo(View view) {
		Context context;
		Intent intent;
		// Handler
		Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String nombres = msg.getData().getString("STATUS");

				if (nombres.equals("si")) {
					dialog.dismiss();

					Toast toast = Toast.makeText(getApplicationContext(),
							"Has añadido tu grupo de trabajo",
							Toast.LENGTH_LONG);
					toast.show();

					Intent intent = new Intent(Grupos_crear.this, Grupos.class);
					startActivity(intent);
				} else {
					dialog.dismiss();

					Toast toast = Toast.makeText(getApplicationContext(),
							"No se ha podido crear tu grupo de trabajo",
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		};
		
		// Comprobamos que se ha completado correctamente el formulario
		EditText nombre = (EditText) findViewById(R.id.editText1);
		String consulta_nombre = (String) nombre.getText().toString();

		Spinner asign = (Spinner) findViewById(R.id.spinner1);
		String consulta_asign = (String) asign.getSelectedItem().toString();

		EditText lugar = (EditText) findViewById(R.id.editText2);
		String consulta_lugar = (String) lugar.getText().toString();

		TextView hora2 = (TextView) findViewById(R.id.textView7);
		String consulta_hora = (String) hora2.getText().toString();
		
		TextView fecha2 = (TextView) findViewById(R.id.textView5);
		String consulta_fecha = (String) fecha2.getText().toString();

		SharedPreferences prefs = getSharedPreferences("MiUsuario",
				Context.MODE_PRIVATE);
		String consulta_creador = prefs.getString("user_activo",
				"por_defecto@email.com");

		// Comprobamos si hay algún campo vacío
		if ((consulta_nombre.length() == 0) || (consulta_asign.length() == 0)
				|| (consulta_lugar.length() == 0)) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"No has completado todos los campos", Toast.LENGTH_LONG);
			toast.show();
		}

		// Mandamos los datos a servidor
		else {
			dialog = ProgressDialog.show(Grupos_crear.this, "",
					"Conectando con el servidor", true);
			dialog.setCancelable(true);

			Log.d("Probando", "VAMOS A INICIAR EL THREAD!!");
			HTTPThread t = new HTTPThread(consulta_nombre, consulta_asign,
					consulta_creador, consulta_lugar, consulta_fecha, consulta_hora, handler);
			t.start();
		}
	}

	/* Creamos nuestra clase HTTPThread */
	class HTTPThread extends Thread {
		String nombre, asignatura, creador, lugar, fecha, hora;
		Handler handler;

		public HTTPThread(String u, String v, String w, String x, String y,
				String z, Handler h) {
			nombre = u;
			asignatura = v;
			creador = w;
			lugar = x;
			fecha = y;
			hora = z;
			handler = h;
		}

		@Override
		public void run() {
			// Parejas nombre-valor con las que paso los datos al servidor
			ArrayList<NameValuePair> nameValuePair;
			
			nameValuePair = new ArrayList<NameValuePair>();
			nameValuePair
					.add(new BasicNameValuePair("CONSULTA_NOMBRE", nombre));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_ASIGN",
					asignatura));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_CREADOR",
					creador));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_LUGAR", lugar));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_FECHA", fecha));
			nameValuePair.add(new BasicNameValuePair("CONSULTA_HORA", hora));

			// Comenzamos la conexión
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/CrGrupo.php");
			
			try {
				Log.d("Probando", "Dentro del try");
			
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

					Log.d("Probando", "resp= " + resp);

					// Extramos la respuesta del JSON
					JSONArray json = new JSONArray(resp);

					String nombres;

					JSONObject js = json.getJSONObject(0);
					nombres = js.getString("status");
					Log.d("Probando", "Status devuelto: " + nombres);

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

	//Creacion de los dialogos de fecha y de hora
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, pHour, pMinute,
					true);
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, pDateSetListener, pYear, pMonth,
					pDay);
		}
		return null;
	}

}
