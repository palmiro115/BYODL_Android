package com.byodl.network;

import com.byodl.model.api.LabelsResponse;
import com.byodl.model.api.UploadResponse;
import com.byodl.model.api.VersionResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Login api calls
 */
public interface ApiService {
    @GET("model/version")
    Call<VersionResponse> getModelVersion();

    @Multipart
    @POST("upload")
    Call<UploadResponse> uploadImage(@Part("label")String label, @Part MultipartBody.Part image);

	@GET("labels")
	Call<LabelsResponse> getLabels();
}
