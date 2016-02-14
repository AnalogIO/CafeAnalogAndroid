/*
 * Copyright 2016 Analog IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
