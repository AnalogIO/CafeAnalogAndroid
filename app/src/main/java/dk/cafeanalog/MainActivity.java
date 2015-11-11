package dk.cafeanalog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextSwitcher;

public class MainActivity extends AppCompatActivity {

    private TextSwitcher view;
    private Communicator.AnalogTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        view = (TextSwitcher) findViewById(R.id.text_view);
        view.setFactory(new TextSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                AppCompatTextView textView = new AppCompatTextView(MainActivity.this);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
            }
        });
        view.setCurrentText(getResources().getText(R.string.is_open_analog));
        view.setInAnimation(MainActivity.this, android.R.anim.slide_in_left);
        view.setOutAnimation(MainActivity.this, android.R.anim.slide_out_right);

        findViewById(R.id.main_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatTextView tv = (AppCompatTextView) view.getNextView();
                tv.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
                view.setText(getString(R.string.refreshing_analog));
                if (task.getStatus() != AsyncTask.Status.FINISHED) {
                    task.cancel(true);
                }
                task = new AnalogActivityTask(500);
                task.execute();
            }
        });
    }

    @Override
    protected void onDestroy() {
        view = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(true);
        }
        task = new AnalogActivityTask(1000);
        task.execute();
    }

    private class AnalogActivityTask extends Communicator.AnalogTask {
        public AnalogActivityTask(long timeout) {
            super(
                    new Communicator.Runnable<Boolean>() {
                        @Override
                        public void run(Boolean param) {
                            if (view != null) { // The user might exit the application without waiting for response.
                                AppCompatTextView tv = (AppCompatTextView) view.getNextView();
                                if (param) {
                                    tv.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                                    view.setText(getResources().getText(R.string.open_analog));
                                } else {
                                    tv.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                    view.setText(getResources().getText(R.string.closed_analog));
                                }
                            }
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            view.setText(getResources().getString(R.string.error_download));
                        }
                    },
                    timeout
            );
        }
    }
}
