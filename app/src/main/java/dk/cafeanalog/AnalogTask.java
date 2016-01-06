package dk.cafeanalog;

import android.os.AsyncTask;

class AnalogTask extends AsyncTask<Void, Void, Boolean> {
    private final Runnable<Boolean> mPostExecute;
    private final java.lang.Runnable mCancel;

    public AnalogTask(Runnable<Boolean> postExecute, java.lang.Runnable cancel) {
        this.mPostExecute = postExecute;
        this.mCancel = cancel;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        switch (new AnalogDownloader().isOpen()) {
            case OPEN:
                return true;
            case CLOSED:
                return false;
            case UNKNOWN:
            default:
                cancel(true);
                return false;
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
