package in.dibc.kidharhai.utils;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class ApiClient {
    public static APIService getAPIService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(interceptor);

        return new Retrofit.Builder()
                .baseUrl("https://kidharhai.in")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client.build())
                .build().create(APIService.class);
    }
}
