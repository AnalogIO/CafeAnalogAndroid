package dk.cafeanalog;

import android.content.Context;
import android.os.AsyncTask;

class AnalogTask extends AsyncTask<Void, Void, Boolean> {
    private final Action<Boolean> mPostExecute;
    private final Runnable mCancel;
    private final Context mContext;

    public AnalogTask(Context context, Action<Boolean> postExecute, Runnable cancel) {
        mContext = context;
        mPostExecute = postExecute;
        mCancel = cancel;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        switch (new AnalogDownloader(mContext).isOpen()) {
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
