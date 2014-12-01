package com.example.josu.mostrarimagen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.josu.mostrarimagen.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class Principal extends Activity {

    private ImageView iv;
    private EditText etUrl, etNombre;
    private RadioButton rbPrivada, rbPublica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        iv = (ImageView)findViewById(R.id.ivFoto);
        etUrl = (EditText)findViewById(R.id.etUrl);
        etNombre = (EditText)findViewById(R.id.idGuardarComo);
        rbPrivada = (RadioButton)findViewById(R.id.rbPrivada);
        rbPublica = (RadioButton)findViewById(R.id.rbPublica);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bitmap imagen=((BitmapDrawable)iv.getDrawable()).getBitmap();
        outState.putParcelable("imagen",imagen);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        iv.setImageBitmap((Bitmap)savedInstanceState.getParcelable("imagen"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    class HiloFoto extends AsyncTask<String, Void, Bitmap> {

        private ProgressDialog dialogo;

        public Bitmap leerImagen(String urlPagina) {
            Bitmap imagen = null;
            try {
                URL url = new URL(urlPagina);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.connect();
                imagen = BitmapFactory.decodeStream(conexion.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return imagen;
        }

        @Override
        protected Bitmap doInBackground(String... params) {//Otra hebra
            String url = params[0];
            Bitmap imagen = leerImagen(url);
            return imagen;
        }

        @Override
        protected void onPostExecute(Bitmap imagen) {//Hebra UI
            super.onPostExecute(imagen);
            iv.setImageBitmap(imagen);
            try {
                guardarFoto(imagen);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void descargar(View v) throws IOException {
        String ruta = etUrl.getText().toString();
        if(ruta.endsWith(".png") || ruta.endsWith(".gif") || ruta.endsWith(".jpg")){
            HiloFoto hf = new HiloFoto();
            hf.execute(ruta);
        }
        else{
            tostada("La extensión del archivo no es válida.");
            etUrl.setText("");
            etNombre.setText("");
        }

    }

    public void guardarFoto(Bitmap foto) throws IOException {
        String url = etUrl.getText().toString();
        String nombre = url.substring(url.lastIndexOf("/") +1);
        if(etNombre.getText().toString().isEmpty())
            etNombre.setText(nombre);
        else
            nombre = etNombre.getText().toString();
        FileOutputStream fos = null;
        if(rbPrivada.isChecked())
            if(espacioSuficiente(getExternalFilesDir(Environment.DIRECTORY_DCIM))){
                fos = new FileOutputStream(new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), nombre));
                fos.flush();
            }
            else
                tostada(getResources().getString(R.string.no_espacio_privada));
        else
            if(espacioSuficiente(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))){
                fos = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), nombre));
                fos.flush();
            }
            else
                tostada(getResources().getString(R.string.no_espacio_publica));

        if(fos != null)
            if(nombre.endsWith("png"))
                foto.compress(Bitmap.CompressFormat.PNG, 100,fos);
            else
                foto.compress(Bitmap.CompressFormat.JPEG, 100, fos);
    }

    public void tostada(String cadena){
        Toast.makeText(this, cadena, Toast.LENGTH_SHORT).show();
    }

    public boolean espacioSuficiente(File f) {
        double eTotal, eDisponible, porcentaje;
        eTotal = (double) f.getTotalSpace();
        eDisponible = (double) f.getFreeSpace();
        porcentaje = (eDisponible / eTotal) * 100;
        return porcentaje > 10;
    }
}
