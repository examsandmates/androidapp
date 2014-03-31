package org.example.proyectoandroid;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class Dialogo extends DialogFragment {
	@Override
	public AlertDialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage("¿Seguro que quieres salir de Exams&Mates?")
				.setTitle("Confirmación")
				.setPositiveButton("Aceptar",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent perfilbienv = new Intent(getActivity(),
										Log_in.class);
								perfilbienv.addFlags(perfilbienv.FLAG_ACTIVITY_CLEAR_TOP);
								perfilbienv.addFlags(perfilbienv.FLAG_ACTIVITY_SINGLE_TOP);
								startActivity(perfilbienv);
							}
						})
				.setNegativeButton("Cancelar",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		return builder.create();
	}
}
