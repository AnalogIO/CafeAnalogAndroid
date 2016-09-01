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

package dk.cafeanalog.networking;

import java.util.Collections;
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

    public void getTodaysOpenings(final Action1<DayOfOpenings> success, Action1<Throwable> error) {
        analogService.getOpenings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribe(
                        new Action1<List<Opening>>() {
                            @Override
                            public void call(List<Opening> openings) {
                                success.call(OpeningUtils.getTodaysOpenings(openings));
                            }
                        },
                        error);
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
