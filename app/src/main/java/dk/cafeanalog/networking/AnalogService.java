package dk.cafeanalog.networking;

import java.util.List;

import retrofit2.http.GET;
import rx.Observable;

/*
 * Created by HansP on 01-04-2016.
 */
interface AnalogService {

    @GET("api/open")
    Observable<OpeningStatus> isOpen();

    @GET("api/shifts")
    Observable<List<Opening>> getOpenings();

}
