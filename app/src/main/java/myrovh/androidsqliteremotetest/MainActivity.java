package myrovh.androidsqliteremotetest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    //UI Resources
    TextView statusText;
    TextView example1Text;
    TextView example2Text;

    //Date Resources
    String fileName = "testbase.sqlite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.statusLabel);
        example1Text = (TextView) findViewById(R.id.textExample1);
        example2Text = (TextView) findViewById(R.id.textExample2);
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void ClickRefresh(View view) {
        // Gets the URL from the UI's text field.
        statusText.setText("Clicked");
        String stringUrl = "http://ctoggha.nsupdate.info/datatest";
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            statusText.setText("Is Connected");
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            statusText.setText("No network connection available.");
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private File downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("Http Response", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            File contentFile = readIt(is);
            return contentFile;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public File readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        File file = new File(getBaseContext().getFilesDir(), fileName);
        FileOutputStream os;
        try {
            os = openFileOutput(fileName, Context.MODE_PRIVATE);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = stream.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
        } catch (IOException e) {
            Log.e("IOException", "error");
        }
        finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("IOException", "error");
                }
            }
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("IOException", "error");
                }
            }
        }
        return file;
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                Log.e("Error", "Unable to retrieve web page. URL may be invalid.");
                return new File(getBaseContext().getFilesDir(), fileName);
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(File result) {
            example1Text.setText(result.toString());
            SQLiteDatabase database = SQLiteDatabase.openDatabase(result.toString(), null, SQLiteDatabase.OPEN_READWRITE);
            Cursor namePointer = database.rawQuery("SELECT value FROM \"table\" WHERE 1", null);
            //Cursor namePointer = database.query("table", new String[]{"value"}, null, null, null, null, null);
            namePointer.moveToFirst();
            example2Text.setText(namePointer.getString(namePointer.getColumnIndexOrThrow("value")));
            namePointer.close();
        }
    }
}
