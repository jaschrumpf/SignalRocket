package com.gammazero.signalrocket;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

public class RocketMemberItem implements ClusterItem {
    public String title;
    private String snippet;
    private LatLng latLng;
    private BitmapDescriptor icon;

    public RocketMemberItem (MarkerOptions markerOptions) {
        this.latLng = markerOptions.getPosition();
        this.title = markerOptions.getTitle();
        this.snippet = markerOptions.getSnippet();
        this.icon = markerOptions.getIcon();
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void setIcon(BitmapDescriptor icon) {
        this.icon = icon;
    }
}