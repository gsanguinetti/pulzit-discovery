package com.pulzit.discovery.services;

import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public class TwitterUsersClient extends TwitterApiClient {
    public TwitterUsersClient(Session session) {
        super(session);
    }

    public UserService getUserService() {
        return getService(UserService.class);
    }
}
