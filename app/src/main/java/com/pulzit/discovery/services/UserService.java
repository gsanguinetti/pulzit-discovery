package com.pulzit.discovery.services;

import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

// example users/show service endpoint
public interface UserService {
    @GET("/1.1/users/show.json")
    void show(@Query("user_id") long id, Callback<User> cb);

    @GET("/1.1/users/search.json")
    void search(@Query("q") String query, Callback<List<User>> cb);

    @POST("/1.1/friendships/create.json")
    void follow(@Query("user_id") long id, Callback<User> cb);

    @POST("/1.1/friendships/destroy.json")
    void unfollow(@Query("user_id") long id, Callback<User> cb);

}