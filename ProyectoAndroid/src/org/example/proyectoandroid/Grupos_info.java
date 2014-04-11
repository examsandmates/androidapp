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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class Grupos_info extends Activity {

	// Variables en las que guardaremos los datos de cada grupo
	String gr_nom, gr_cre, gr_asign, gr_lug, gr_fec, gr_hor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grupos_info);

		// Recibimos y mostramos el nombre del grupo (de la actividad anterior)
		Bundle bundle = getIntent().getExtras();
		gr_nom = bundle.getString("GRUPO");
		
		TextView text_name = (TextView) findViewById(R.id.textView1);
		text_name.setText("Información del grupo "+gr_nom);

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				
				// Almancenamos en strings mis datos del servidor
				String gr_cre = msg.getData().getString("GR_CRE");
				String gr_asign = msg.getData().getString("GR_ASIGN");
				String gr_lug = msg.getData().getString("GR_LUG");
				String gr_fec = msg.getData().getString("GR_FEC");
				String gr_hor = msg.getData().getString("GR_HOR");
				
				// Modificamos los textviews para mostrar los datos del grupo
				TextView text_creat = (TextView) findViewById(R.id.textView2);
				text_creat.setText("Creador del grupo: " + gr_cre);
				
				TextView text_sign = (TextView) findViewById(R.id.textView3);
				text_sign.setText("Asignatura: " + gr_asign);
				
				TextView text_lug = (TextView) findViewById(R.id.textView4);
				text_lug.setText("Lugar: " + gr_lug);
				
				TextView text_fec = (TextView) findViewById(R.id.textView5);
				text_fec.setText("Día: " + gr_fec);
				
				TextView text_time = (TextView) findViewById(R.id.textView6);
				text_time.setText("Hora: " + gr_hor);

			}
		};

		HTTPThread t = new HTTPThread(gr_nom, handler);
		t.start();
	}

	/* Creamos nuestra clase HTTPThread */
	class HTTPThread extends Thread {
		String gr_consulta;
		Handler handler;

		public HTTPThread(String u, Handler h) {
			gr_consulta = u;
			handler = h;
		}

		@Override
		public void run() {
			// Pareja nombre-valor con la que paso el grupo a la petición
			ArrayList<NameValuePair> nameValuePair;
			nameValuePair = new ArrayList<NameValuePair>();
			Log.d("Probando", "gr_consulta vale: " + gr_consulta);
			nameValuePair.add(new BasicNameValuePair("CONSULTA_NOMBRE",
					gr_consulta));
			
			// Comenzamos la conexión
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://examsandmates.web44.net/examapp/CoGrupo.php");
			
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
					gr_cre = js.getString("creator");
					gr_asign = js.getString("subj");
					gr_lug = js.getString("place");
					gr_fec = js.getString("date");
					gr_hor = js.getString("time");

					// Mensaje para el handler
					Message msg = handler.obtainMessage();
					
					Bundle bundle = new Bundle();
					
					bundle.putString("GR_CRE", gr_cre);
					bundle.putString("GR_ASIGN", gr_asign);
					bundle.putString("GR_LUG", gr_lug);
					bundle.putString("GR_FEC", gr_fec);
					bundle.putString("GR_HOR", gr_hor);
					
					msg.setData(bundle);
					handler.sendMessage(msg);

				}
			} catch (Exception e) {
			}
		}
	}
}
