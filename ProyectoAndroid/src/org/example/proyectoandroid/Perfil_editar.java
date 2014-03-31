package org.example.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_editarperfil, menu);
		return true;
	}

	// Damos acciones a las opciones
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_principal:
			Intent perfilbienv = new Intent(this, Perfil_bienvenida.class);
			startActivity(perfilbienv);
			return true;
		case R.id.menu_gente:
			Intent intentbuscar = new Intent(this, Buscar.class);
			startActivity(intentbuscar);
			return true;
		case R.id.menu_grupos:
			Toast toast = Toast.makeText(getApplicationContext(),
					"Próximamente...", Toast.LENGTH_SHORT);
			toast.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
