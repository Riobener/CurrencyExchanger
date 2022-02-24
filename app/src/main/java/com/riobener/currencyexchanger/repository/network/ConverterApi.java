package com.riobener.currencyexchanger.repository.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ConverterApi {
    @GET("{token}/pair/{firstValue}/{lastValue}")
    Call<ResponseValue> getMultiplier(@Path("firstValue") String value,
                                      @Path("lastValue") String lastValue,
                                      @Path("token") String token);
}
