package dk.cafeanalog;

import android.os.AsyncTask;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

class AnalogTask extends AsyncTask<Void, Void, Boolean> {
    private final Runnable<Boolean> mPostExecute;
    private final java.lang.Runnable mCancel;

    public AnalogTask(Runnable<Boolean> postExecute, java.lang.Runnable cancel) {
        this.mPostExecute = postExecute;
        this.mCancel = cancel;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http", "cafeanalog.dk", "REST");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            try (JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()))) {
                reader.beginObject();
                while (!Objects.equals(reader.nextName(), "open")) { reader.skipValue(); }
                return reader.nextBoolean();
            }
        } catch (IOException e) {
            e.printStackTrace();
            cancel(true);
            return false;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        mPostExecute.run(aBoolean);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mCancel.run();
    }
}
