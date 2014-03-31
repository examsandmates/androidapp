package org.example.proyectoandroid;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Perfil_bienvenida extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.perfil_bienvenida);
	}

	// En onResume tenemos el c�digo que actualiza la listview cada vez que
	// entramos a Perfil_editar y pulsamos "atr�s".
	@Override
	protected void onResume() {
		int contador = 0;
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		// Todo el c�digo siguente lee el archivo de preferencias y carga en
		// lista las asignaturas marcadas

		// Comprobamos qu� cantidad de asignaturas tenemos marcadas
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
		// Creamos el vector de strings
		String[] asignaturas = new String[contador];
		// Si no tenemos asignaturas, asignamos un texto
		if (contador == 0) {
			ListView lv = getListView();
			lv.setEmptyView(findViewById(R.id.emptyListView));
		}
		// Si tenemos asignaturas, rellenamos el array
		else {
			int aux = 0;
			while (aux < contador) {
				if (pref.getBoolean("TRA", false)) {
					asignaturas[aux] = "Ing. del transporte";
					aux++;
					contador--;
				}
				if (pref.getBoolean("ELE", false)) {
					asignaturas[aux] = "Tecnolog�a El�ctrica";
					aux++;
					contador--;
				}
				if (pref.getBoolean("OAE", false)) {
					asignaturas[aux] = "Admon. Empresas";
					aux++;
					contador--;
				}
				if (pref.getBoolean("EST", false)) {
					asignaturas[aux] = "Teor�a de estructuras";
					aux++;
					contador--;
				}
				if (pref.getBoolean("SII", false)) {
					asignaturas[aux] = "Sistemas inform�ticos industriales";
					aux++;
					contador--;
				}
				if (pref.getBoolean("MOD", false)) {
					asignaturas[aux] = "Modelado e identifiaci�n de sistemas";
					aux++;
					contador--;
				}
				if (pref.getBoolean("VEH", false)) {
					asignaturas[aux] = "Veh�culos";
					aux++;
					contador--;
				}
				if (pref.getBoolean("MAQ", false)) {
					asignaturas[aux] = "M�quinas hidr�ulicas y t�rmicas";
					aux++;
					contador--;
				}
				if (pref.getBoolean("CLI", false)) {
					asignaturas[aux] = "Climatizaci�n de edificios y refrigeraci�n";
					aux++;
					contador--;
				}
				if (pref.getBoolean("SEG", false)) {
					asignaturas[aux] = "Seg. industrial y Repres. gr�fica de proyecto";
					aux++;
					contador--;
				}
			}
		}
		// Creamos la listview con el vector de strings
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.test_list_item, asignaturas));

		super.onResume();
	}

	// Cuadro de di�logo para confirmar la salida de la app
	public void onBackPressed() {
		FragmentManager fragmentManager = getFragmentManager();
		Dialogo dialogo = new Dialogo();
		dialogo.show(fragmentManager, "tagAlerta");
	}

	// Creamos el desplegable de opciones
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_bienvenida, menu);
		return true;
	}

	// Damos acciones a las opciones
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_editarperfil:
			Intent perfiledit = new Intent(this, Perfil_editar.class);
			startActivity(perfiledit);
			return true;
		case R.id.menu_gente:
			Intent intentbuscar = new Intent(this, Buscar.class);
			// Recibimos el usuario de la activity anterior
			Bundle bundle = getIntent().getExtras();
			String Usuario = bundle.getString("USUARIO");
			Log.d("Amigo", "Recibiendo de LOG IN Y ENVIANDO A BUSCAR: "
					+ Usuario);
			intentbuscar.putExtra("USUARIO", Usuario);
			startActivity(intentbuscar);
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
}
