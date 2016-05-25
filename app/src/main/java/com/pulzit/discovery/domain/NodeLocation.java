package com.pulzit.discovery.domain;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by gastonsanguinetti on 07/05/16.
 */
public class NodeLocation {

    private LatLng nodeLatLng;

    public NodeLocation() {}

    public NodeLocation(LatLng nodeLatLng) {
        this.nodeLatLng = nodeLatLng;
    }

    public LatLng getNodeLatLng() {
        return nodeLatLng;
    }

    public void setNodeLatLng(LatLng nodeLatLng) {
        this.nodeLatLng = nodeLatLng;
    }
}
