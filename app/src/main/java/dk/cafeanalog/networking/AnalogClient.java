package dk.cafeanalog.networking;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by HansP on 01-04-2016.
 */
public class AnalogClient {

    private static AnalogClient instance;
    private AnalogService analogService;

    public static AnalogClient getInstance() {
        if(instance == null) {
            return new AnalogClient();
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
        analogService.isOpen().
                subscribeOn(Schedulers.newThread()).
                subscribeOn(Schedulers.io()).
                subscribe(success, error);
    }
}
