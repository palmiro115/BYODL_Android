package com.byodl.network;

/**
 */

import android.support.annotation.NonNull;

import com.byodl.AppConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Init retrofit interface and provide api services
 */
public class ApiFactory
{
	private static final String TAG = ApiFactory.class.getSimpleName();

	private static final int CONNECT_TIMEOUT = 30;
	private static final int WRITE_TIMEOUT = 120;
	private static final int TIMEOUT = 120;

	private static final OkHttpClient CLIENT;
	private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

	private static Retrofit sRetrofit;
	private static ApiService service;

	static {
		loggingInterceptor.setLevel(AppConstants.Config.API_DEBUG_LEVEL);
		CLIENT = new OkHttpClient.Builder()
				.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
				.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
				.readTimeout(TIMEOUT, TimeUnit.SECONDS)
				.addInterceptor(loggingInterceptor)
				.build();
	}

	/**
	 * Get api service
	 *
	 * @return api service
	 */
	@NonNull
	public static ApiService getApiService()
	{
		if (service ==null)
			service = getRetrofit().create(ApiService.class);
		return service;
	}

	@NonNull
	private static Retrofit getRetrofit()
	{
		if (sRetrofit==null) {
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			sRetrofit = new Retrofit.Builder()
					.baseUrl(AppConstants.Api.BASE_URL)
					.addConverterFactory(ScalarsConverterFactory.create())
					.addConverterFactory(GsonConverterFactory.create(gson))
					.client(CLIENT)
					.build();
		}
		return sRetrofit;
	}
	public static OkHttpClient getClient(){
		return CLIENT;
	}
	public static OkHttpClient getProgressClient(final ProgressResponseBody.ProgressListener progressListener){
		OkHttpClient progressClient = new OkHttpClient.Builder()
				.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
				.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
				.readTimeout(TIMEOUT, TimeUnit.SECONDS)
				.addInterceptor(loggingInterceptor)
				.addNetworkInterceptor(new Interceptor() {
					@Override public Response intercept(Chain chain) throws IOException {
						Response originalResponse = chain.proceed(chain.request());
						return originalResponse.newBuilder()
								.body(new ProgressResponseBody(originalResponse.body(), progressListener))
								.build();
					}
				})
				.build();

		return progressClient;
	}

}
