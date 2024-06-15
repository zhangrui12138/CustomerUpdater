package com.hongyao.hyupdater.internetmodel;

import com.hongyao.hyupdater.internetmodel.beans.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IUserService {
    @GET("content/file/info/1/{version}/{serialno}/{model}")
    Call<User> getUser(@Path("version") String version,@Path("serialno") String serialno,@Path("model") String model,@Query("language") String language);

    @GET
    Call<ResponseBody> downloadFileWithc(@Url String urlString);
}
