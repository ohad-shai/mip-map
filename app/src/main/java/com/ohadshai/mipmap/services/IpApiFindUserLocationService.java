package com.ohadshai.mipmap.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.GsonBuilder;
import com.ohadshai.mipmap.entities.ip_api.IPLocation;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.ip_api.IpApiConsts;

/**
 * Represents a service for finding a location by IP via the "IP-API" web service.
 * Created by Ohad on 12/14/2016.
 */
public class IpApiFindUserLocationService extends IntentService {

    //region Public Members

    /**
     * Holds the finish broadcast name (called when there's a location result).
     */
    public static final String FINISH_BROADCAST_NAME = "com.ohadshai.mipmap.services.ip_api.FINISHED";

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a service for finding a location by IP.
     */
    public IpApiFindUserLocationService() {
        super("IpApiFindUserLocationService");
    }

    //endregion

    //region Events

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Gets the user's location:
            String json = Utils.Networking.sendHttpRequest(IpApiConsts.Urls.GET_USER_LOCATION, this);
            IPLocation ipLocation = new GsonBuilder().create().fromJson(json, IPLocation.class);

            // Sends the location information to broadcast:
            Intent finishBroadcast = new Intent(FINISH_BROADCAST_NAME);
            finishBroadcast.putExtra(UIConsts.Intent.IP_LOCATION_KEY, ipLocation);
            LocalBroadcastManager.getInstance(this).sendBroadcast(finishBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion

}
