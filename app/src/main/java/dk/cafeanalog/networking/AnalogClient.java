package dk.cafeanalog.networking;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dk.cafeanalog.DayOfOpenings;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/*
 * Created by HansP on 01-04-2016.
 */
public class AnalogClient {

    private static AnalogClient instance;
    private final AnalogService analogService;

    public static AnalogClient getInstance() {
        if(instance == null) {
            return instance = new AnalogClient();
        } else {
            return instance;
        }
    }

    private AnalogClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://cafeanalog.dk/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        analogService = retrofit.create(AnalogService.class);
    }

    public void isOpen(Action1<OpeningStatus> success, Action1<Throwable> error) {
        analogService.isOpen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success, error);
    }

    public void getCurrentOpening(final Action1<Opening> success, Action1<Throwable> error) {
        analogService.getOpenings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribe(
                        new Action1<List<Opening>>() {
                            @Override
                            public void call(List<Opening> openings) {
                                success.call(OpeningUtils.getCurrentOpening(openings));
                            }
                        },
                        error);
    }

    public void getDaysOfOpenings(final Action1<List<DayOfOpenings>> callback, final Action1<Throwable> error) {
        analogService.getOpenings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread(), true)
                .map(new Func1<List<Opening>, List<DayOfOpenings>>() {
                    @Override
                    public List<DayOfOpenings> call(List<Opening> openings) {
                        Collections.sort(openings);

                        return OpeningUtils.getDaysOfOpenings(openings);
                    }
                })
                .subscribe(callback, error);
    }
}
