// Esta clase es para definir el menú de opciones para versiones APIs superiores a 11.
// Para probarla, recuerda modificar la version minima en el manifest.
package org.example.proyectoandroid;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class EditarPerfilFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.opciones);
	}
}