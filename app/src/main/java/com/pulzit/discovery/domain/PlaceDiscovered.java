package com.pulzit.discovery.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;
import com.firebase.client.annotations.Nullable;
import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import java.util.HashMap;

/**
 * Created by gastonsanguinetti on 09/05/16.
 */

@Table
public class PlaceDiscovered {

    public static final int STATE_DISCOVERED = 0;
    public static final int STATE_ACCOUNT_NOT_FOUND = 1;
    public static final int STATE_DISMISSED = 2;

    @PrimaryKey
    private String placeId;

    @Column
    @Nullable
    private Long nodeId;

    @Column
    private int discoverState;

    private HashMap<String, Object> dateLastChanged;

    public PlaceDiscovered(){}

    public PlaceDiscovered(String placeId, Long nodeId, int discoverState) {
        this.placeId = placeId;
        this.nodeId = nodeId;
        this.discoverState = discoverState;

        //Date last changed will always be set to ServerValue.TIMESTAMP
        HashMap<String, Object> dateLastChangedObj = new HashMap<String, Object>();
        dateLastChangedObj.put("date", ServerValue.TIMESTAMP);
        this.dateLastChanged = dateLastChangedObj;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public int getDiscoverState() {
        return discoverState;
    }

    public void setDiscoverState(int discoverState) {
        this.discoverState = discoverState;
    }

    public HashMap<String, Object> getDateLastChanged() {
        return dateLastChanged;
    }

    @JsonIgnore
    public long getDateLastChangedLong() {
        return (long)dateLastChanged.get("date");
    }

}
