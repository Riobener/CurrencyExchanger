package com.riobener.currencyexchanger.repository.network;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConverterLogic {
    private ConverterApi api;

    public ConverterLogic() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://v6.exchangerate-api.com/v6/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ConverterApi.class);
    }

    public LiveData<Double> getValue(String convertFrom, String convertTo) {
        MutableLiveData<Double> convertRate = new MutableLiveData<>();
        Call<ResponseValue> call = api.getMultiplier(convertFrom, convertTo, "5503eeedc050d2263f5a1b6b");
        call.enqueue(new Callback<ResponseValue>() {

            @Override
            public void onResponse(@NonNull Call<ResponseValue> call, @NonNull Response<ResponseValue> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseValue responseValue = response.body();
                    convertRate.setValue((double) responseValue.getConversion_rate());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseValue> call, Throwable t) {
                return;
            }
        });
        return convertRate;
    }
}

