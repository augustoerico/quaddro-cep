package br.com.erico.cep;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class MainActivity extends AppCompatActivity {

    static final Integer ZIP_CODE_LENGTH = 8;

    //Passo 1 - Declarar atributos
    private EditText edtZip;
    private View mView;
    private String zip;
    private ProgressDialog progressDialog;
    private String json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Passo 2 - Vincular os componentes graficos
        edtZip = (EditText) findViewById(R.id.edtZip);
    }

    public void onSearchZip(final View view) {

        // Capturando o cep digitado e formatando
        zip = edtZip.getEditableText().toString().trim().replaceAll("\\D","");

        // Verificando
        if (zip.length() == ZIP_CODE_LENGTH) {

            // Proteger a tela dos famosos "trocentos touchs"
            this.mView = view;
            mView.setEnabled(false);

            // Elaborando o async task
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    progressDialog = new ProgressDialog(view.getContext());
                    progressDialog.setTitle("Searching address");
                    progressDialog.setMessage("Wait for it...");
                    progressDialog.setCancelable(true);
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();
                }

                @Override
                protected Void doInBackground(Void... params) {

                    // The bicho is going to get it
                    try {

                        // 1 segundo para respirar (WTF)
                        Thread.sleep(1000);

                        // Recuperando os dados enviados
                        InputStream stream = null; // recuperar e tratar a informação

                        URL url = new URL("http://api.postmon.com.br/v1/cep/" + zip);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setReadTimeout(10000);
                        connection.setConnectTimeout(15000);
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);

                        // Executar a url
                        connection.connect();

                        int responseCode = connection.getResponseCode();
                        Log.i("CEP", "Codigo de resposta: " + responseCode);

                        // Capturando os dados
                        stream = connection.getInputStream();
                        json = parseResponse(stream);

                        Log.i("CEP", json);

                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        mView.setEnabled(true);

                        try {

                            JSONObject jsonObject = new JSONObject(json);
                            Log.i("CEP", jsonObject.getString("bairro"));
                            Log.i("CEP", jsonObject.getString("cidade"));
                            Log.i("CEP", jsonObject.getString("cep"));
                            Log.i("CEP", jsonObject.getString("logradouro"));

                        } catch (Exception e) {

                        }
                    }
                }
            };

            asyncTask.execute((Void[]) null);

        } else {
            Toast.makeText(MainActivity.this, "Invalid ZIP code", Toast.LENGTH_LONG).show();
        }


    }

    private String parseResponse(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);

        String result = URLDecoder.decode(bufferedReader.readLine(), "UTF-8");
        return result;
    }
}
