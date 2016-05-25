package com.pulzit.discovery.services;

import android.content.Context;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.pulzit.discovery.R;
import com.pulzit.discovery.domain.Node;
import com.pulzit.discovery.domain.OrmaDatabase;
import com.pulzit.discovery.domain.PlaceDiscovered;
import com.pulzit.discovery.global.UtilConstants;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by gastonsanguinetti on 04/05/16.
 */
public class PulzitService implements ChildEventListener {

    private static PulzitService instance;

    private Firebase historyRef;
    private OrmaDatabase discoveredPlacesLocalDb;

    protected PulzitService(Context context) {
        Firebase.setAndroidContext(context);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        discoveredPlacesLocalDb = OrmaDatabase.builder(context)
                .build();
        discoveredPlacesLocalDb.deleteFromPlaceDiscovered().execute();

        historyRef = new Firebase(UtilConstants.BASE_URL).child(UtilConstants.DISCOVERED_PLACES_REF);
        historyRef.addChildEventListener(this);
    }

    public static void init(Context context) {
        instance = new PulzitService(context);
    }

    public static PulzitService getInstance() {
        if (instance == null) {
            throw new RuntimeException("PulzitService has to be initialized first");
        }
        return instance;
    }

    public void destroy() {
        historyRef.removeEventListener(this);
    }

    public void addNode(final List<String> childsToAdd, final Node node, final String placeId,
                        final Firebase.CompletionListener completionListener) {

        followTwitterAccount(node.getNodeId(), new com.twitter.sdk.android.core.Callback<User>() {
            @Override
            public void success(Result<User> result) {
                Firebase rootRef = new Firebase(UtilConstants.BASE_URL).child(UtilConstants.NODES_REF);

                for(final String accountType : childsToAdd) {
                    Firebase dataRef = rootRef.child(accountType).child(UtilConstants.NODE_DATA_REF)
                            .child(String.valueOf(node.getNodeId()));
                    final GeoFire geoFire = new GeoFire(rootRef.child(accountType).child(UtilConstants
                            .NODE_LOCATION_REF));

                    dataRef.setValue(node.getNodeData(), new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(final FirebaseError firebaseError, final Firebase firebase) {
                            if (firebaseError == null) {
                                geoFire.setLocation(String.valueOf(node.getNodeId()),
                                        new GeoLocation(
                                                node.getNodeLocation().getNodeLatLng().latitude,
                                                node.getNodeLocation().getNodeLatLng().longitude),
                                        new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, FirebaseError error) {
                                                if (firebaseError == null) {
                                                    PlaceDiscovered placeDiscovered = new PlaceDiscovered(
                                                            placeId, node.getNodeId(),
                                                            PlaceDiscovered.STATE_DISCOVERED);
                                                    addDiscoveredPlace(placeDiscovered, new Firebase
                                                            .CompletionListener() {
                                                        @Override
                                                        public void onComplete(FirebaseError firebaseError,
                                                                               Firebase firebase) {
                                                            completionListener.onComplete(firebaseError,
                                                                    firebase);
                                                        }
                                                    });
                                                } else {
                                                    completionListener.onComplete(firebaseError, firebase);
                                                }
                                            }
                                        });
                            } else {
                                completionListener.onComplete(firebaseError, firebase);
                            }
                        }
                    });
                }
            }

            @Override
            public void failure(TwitterException e) {
                completionListener.onComplete(new FirebaseError(FirebaseError.UNKNOWN_ERROR,
                        "Cant Follow Tw account"), null);
            }
        });


    }

    public void removeNode(Context context, Long nodeId, String placeId) {
        Firebase rootRef = new Firebase(UtilConstants.BASE_URL).child(UtilConstants.NODES_REF);

        CharSequence[] childs = context.getResources().getTextArray(R.array.channels_entries);
        for(int i=0; i<childs.length; i++) {
            rootRef.child(childs[i].toString()).child(UtilConstants.NODE_DATA_REF)
                    .child(String.valueOf(nodeId)).setValue(null);
            rootRef.child(childs[i].toString()).child(UtilConstants.NODE_LOCATION_REF)
                    .child(String.valueOf(nodeId)).setValue(null);
        }

        unfollowTwitterAccount(nodeId, null);
    }

    public void followTwitterAccount(long userId, Callback<User> cb) {
        TwitterUsersClient twitterUsersClient = new TwitterUsersClient(Twitter.
                getSessionManager().getActiveSession());
        twitterUsersClient.getUserService().follow(userId, cb);
    }

    public void unfollowTwitterAccount(long userId, Callback<User> cb) {
        TwitterUsersClient twitterUsersClient = new TwitterUsersClient(Twitter.
                getSessionManager().getActiveSession());
        twitterUsersClient.getUserService().unfollow(userId, cb);
    }

    public void getCandidateTwitterAccountsForPlace(String placeQuery,
                                                    com.twitter.sdk.android.core.Callback<List<User>> cb) {
        TwitterUsersClient twitterUsersClient = new TwitterUsersClient(Twitter.
                getSessionManager().getActiveSession());
        twitterUsersClient.getUserService().search(placeQuery, cb);
    }

    public void findUserById(long userId, Callback<User> cb) {
        TwitterUsersClient twitterUsersClient = new TwitterUsersClient(Twitter.
                getSessionManager().getActiveSession());
        twitterUsersClient.getUserService().show(userId, cb);
    }

    public void addDiscoveredPlace(PlaceDiscovered placeDiscovered, Firebase.CompletionListener
            completionListener) {
        Firebase rootRef = new Firebase(UtilConstants.BASE_URL).child(UtilConstants
                .DISCOVERED_PLACES_REF);
        rootRef.child(placeDiscovered.getPlaceId()).setValue(placeDiscovered, completionListener);
    }

    public void getDiscoveryPlace(String placeId, final Callback<User> cb) {
        Firebase placeRef = new Firebase(UtilConstants.BASE_URL).child(UtilConstants
                .DISCOVERED_PLACES_REF).child(placeId);
        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChildren() &&
                        dataSnapshot.getValue(PlaceDiscovered.class).getNodeId()!=null) {
                   findUserById(dataSnapshot.getValue(PlaceDiscovered.class).getNodeId(), cb);
                } else {
                    cb.failure(RetrofitError.unexpectedError(null, new Exception("no account")));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                cb.failure(RetrofitError.unexpectedError(null, new Exception("no account")));
            }
        });
    }

    public void addPlaceToUserHistory(String userId, String placeId, Firebase.CompletionListener
            completionListener) {
        Firebase rootRef = new Firebase(UtilConstants.BASE_URL).child(UtilConstants.USER_HISTORY_REF)
                .child(userId);
        rootRef.setValue(placeId, completionListener);
    }

    public void addPlaceToBlockedList(String placeId, Firebase.CompletionListener
            completionListener) {
        PlaceDiscovered blockedPlace = new PlaceDiscovered(placeId, null,
                PlaceDiscovered.STATE_DISMISSED);
        addDiscoveredPlace(blockedPlace, completionListener);
    }

    public void addPlaceToAccountNotFoundList(String placeId, Firebase.CompletionListener
            completionListener) {
        PlaceDiscovered blockedPlace = new PlaceDiscovered(placeId, null,
                PlaceDiscovered.STATE_ACCOUNT_NOT_FOUND);
        addDiscoveredPlace(blockedPlace, completionListener);
    }

    public boolean isPlaceDiscovered(String placeId) {
        int count = discoveredPlacesLocalDb.selectFromPlaceDiscovered().placeIdEq(placeId).count();
        return count > 0;
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Long res = discoveredPlacesLocalDb.insertIntoPlaceDiscovered(dataSnapshot.getValue(PlaceDiscovered.class));
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        PlaceDiscovered placeDiscovered = dataSnapshot.getValue(PlaceDiscovered.class);
        discoveredPlacesLocalDb.updatePlaceDiscovered().nodeId(placeDiscovered.getNodeId())
                .discoverState(placeDiscovered.getDiscoverState())
                .execute();
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        discoveredPlacesLocalDb.deleteFromPlaceDiscovered().placeIdEq(dataSnapshot
                .getValue(PlaceDiscovered.class)
                .getPlaceId());
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

    @Override
    public void onCancelled(FirebaseError firebaseError) {}
}
