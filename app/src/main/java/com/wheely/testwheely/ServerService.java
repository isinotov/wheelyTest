package com.wheely.testwheely;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.SugarRecord;

import java.lang.reflect.Type;
import java.util.List;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


/**
 * Created by isinotov on 17/03/2016.
 */
public class ServerService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private WebSocketConnection connection;
    private GoogleApiClient googleApiClient;
    private Location location;
    private String login, password;
    private static final String WEB_TAG = "WEB_CONNECTION";
    Gson gson = new Gson();
    Type listType = new TypeToken<List<Point>>() {
    }.getType();
    private SharedPreferences sharedpreferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedpreferences = getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE);
        login = sharedpreferences.getString(Constants.ARG_LOGIN, null);
        password = sharedpreferences.getString(Constants.ARG_PASSWORD, null);
        sharedpreferences.edit().putBoolean(Constants.IS_CONNECTED, true).apply();
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (connection == null) {
            connection = new WebSocketConnection();
            try {
                connection.connect(String.format(Constants.URL, login, password), new WebSocketHandler() {
                    @Override
                    public void onClose(int code, String reason) {
                        super.onClose(code, reason);
                        Log.d(WEB_TAG, "CLOSE");
                    }

                    @Override
                    public void onOpen() {
                        super.onOpen();
                        googleApiClient.connect();
                        Log.d(WEB_TAG, "OPEN");
                    }

                    @Override
                    public void onTextMessage(String payload) {
                        super.onTextMessage(payload);
                        List<Point> points = gson.fromJson(payload, listType);
                        for (Point p : points) {
                            SugarRecord.save(p);
                        }
                        Intent messageIntent = new Intent(Constants.ACTION);
                        sendBroadcast(messageIntent);
                        Log.d(WEB_TAG, payload);
                    }
                });
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connection != null && connection.isConnected())
            connection.disconnect();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            Point point = new Point();
            point.setLat(location.getLatitude());
            point.setLon(location.getLongitude());
            connection.sendTextMessage(gson.toJson(point));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
