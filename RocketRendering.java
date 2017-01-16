package com.gammazero.signalrocket;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by Jamie on 1/7/2017.
 */

public class RocketRendering {
    public class rRendering extends DefaultClusterRenderer<RocketMemberItem> {

        public rRendering(Context context, GoogleMap map,
                           ClusterManager<RocketMemberItem> clusterManager) {
            super(context, map, clusterManager);
        }


        protected void onBeforeClusterItemRendered(RocketMemberItem item, MarkerOptions markerOptions) {

            markerOptions.icon(item.getIcon());
            markerOptions.snippet(item.getSnippet());
            markerOptions.title(item.getTitle());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }
}
