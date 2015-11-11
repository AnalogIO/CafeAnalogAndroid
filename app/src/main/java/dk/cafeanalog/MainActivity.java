package dk.cafeanalog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextSwitcher;

public class MainActivity extends AppCompatActivity {

    private TextSwitcher view;
    private AnalogActivityTask task;

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

                if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
                    tv.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
                    view.setText(getString(R.string.refreshing_analog));
                    task = new AnalogActivityTask(view, 300);
                    task.execute();
                }
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
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
            task = new AnalogActivityTask(view, 700);
            task.execute();
        }
    }

    private static class AnalogActivityTask extends Communicator.AnalogTask {
        public AnalogActivityTask(final TextSwitcher view, final long timeout) {
            super(
                    new Communicator.Runnable<Boolean>() {
                        @Override
                        public void run(final Boolean param) {
                            Handler handler = new Handler();
                            handler.postDelayed(new java.lang.Runnable() {
                                @Override
                                public void run() {
                                    if (view != null) { // The user might exit the application without waiting for response.
                                        AppCompatTextView tv = (AppCompatTextView) view.getNextView();
                                        if (param) {
                                            tv.setTextColor(view.getContext().getResources().getColor(android.R.color.holo_green_light));
                                            view.setText(view.getContext().getResources().getText(R.string.open_analog));
                                        } else {
                                            tv.setTextColor(view.getContext().getResources().getColor(android.R.color.holo_red_light));
                                            view.setText(view.getContext().getResources().getText(R.string.closed_analog));
                                        }
                                    }
                                }
                            }, timeout);
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            if (view != null)
                                view.setText(view.getContext().getResources().getString(R.string.error_download));
                        }
                    }
            );
        }
    }
}
