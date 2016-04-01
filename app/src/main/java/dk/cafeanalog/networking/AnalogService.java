package dk.cafeanalog.networking;

import java.util.List;

import dk.cafeanalog.Opening;
import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by HansP on 01-04-2016.
 */
public interface AnalogService {

    @GET("api/open")
    Observable<OpeningStatus> isOpen();

    @GET("api/shifts")
    Observable<List<Opening>> getOpenings();

}
