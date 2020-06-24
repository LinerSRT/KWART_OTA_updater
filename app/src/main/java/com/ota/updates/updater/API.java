package com.ota.updates.updater;

import android.os.Environment;
import android.support.annotation.NonNull;

import com.ota.updates.updater.data.OTAPackage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public class API {
    private Retrofit ipAdapter;
    private Retrofit otaAdapter;
    private OkHttpClient okHttpClient;

    public API() {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        ipAdapter = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(AESCrypt.decryptRecursive(Constants.IPDATA, Constants.DECRYPT_KEY_LEN))
                .build();
    }

    public void getOTA(final OTA<OTAPackage> listener) {
        if (otaAdapter == null) {
            getServerURL(new OTA<String>() {
                @Override
                public void onSuccess(String result) {
                    otaAdapter = new Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(okHttpClient)
                            .baseUrl("http://"+Objects.requireNonNull(result))
                            .build();
                    APICall apiCall = otaAdapter.create(APICall.class);
                    Call<OTAPackage> call = apiCall.getOTAPackage("Leo"); //PropUtils.get("ro.ota.device")
                    call.enqueue(new Callback<OTAPackage>() {
                        @Override
                        public void onResponse(@NonNull Call<OTAPackage> call, @NonNull Response<OTAPackage> response) {
                            if (response.isSuccessful()) {
                                listener.onSuccess(response.body());
                            } else {
                                listener.onFail();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<OTAPackage> call, @NonNull Throwable t) {
                            listener.onFail();
                        }
                    });
                }

                @Override
                public void onFail() {

                }
            });
        } else {
            APICall apiCall = otaAdapter.create(APICall.class);
            Call<OTAPackage> call = apiCall.getOTAPackage("Leo"); //PropUtils.get("ro.ota.device")
            call.enqueue(new Callback<OTAPackage>() {
                @Override
                public void onResponse(@NonNull Call<OTAPackage> call, @NonNull Response<OTAPackage> response) {
                    if (response.isSuccessful()) {
                        listener.onSuccess(response.body());
                    } else {
                        listener.onFail();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OTAPackage> call, @NonNull Throwable t) {
                    listener.onFail();
                }
            });
        }
    }

    public void downloadOTA(final String device, final String filename, final OTA<File> listener){
        APICall apiCall = otaAdapter.create(APICall.class);
        Call<ResponseBody> call = apiCall.downloadOTA(device, filename); //PropUtils.get("ro.ota.device")
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        File path = Environment.getExternalStorageDirectory();
                        File file = new File(path, filename);
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(Objects.requireNonNull(response.body()).bytes());
                        listener.onSuccess(file);
                    }
                    catch (Exception ex){
                        listener.onFail();
                    }
                } else {
                    listener.onFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                listener.onFail();
            }
        });
    }

    public void getServerURL(final OTA<String> listener) {
        APICall apiCall = ipAdapter.create(APICall.class);
        Call<String> call = apiCall.getResponse("IPAdress.data");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    listener.onSuccess(response.body());
                    otaAdapter = new Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(okHttpClient)
                            .baseUrl("http://"+Objects.requireNonNull(response.body()))
                            .build();
                } else {
                    listener.onFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                listener.onFail();
            }
        });
    }

    public interface APICall {
        @GET
        Call<String> getResponse(@Url String url);

        @GET("OTA/{device}/updates/{filename}")
        Call<ResponseBody> downloadOTA(@Path("device") String device, @Path("filename") String filename);

        @GET("OTA/{device}/ota.json")
        Call<OTAPackage> getOTAPackage(@Path("device") String device);

    }

    public interface OTA<T> {
        void onSuccess(T result);
        void onFail();
    }
}
