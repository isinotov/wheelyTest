package com.wheely.testwheely;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.SugarRecord;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


/**
 * Created by isinotov on 17/03/2016.
 */
public class ServerService extends Service {

    private UpdateReceiver receiver = new UpdateReceiver();
    private WebSocketConnection connection;
    private String login, password;
    private Gson gson = new Gson();
    private SharedPreferences sharedpreferences;
    private LocationManager locationManager;
    private final static int minTime = 5000;
    private final static float minDistance = 0;
    private Type listType = new TypeToken<List<Point>>() {
    }.getType();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private CustomLocationListener gpsListener = new CustomLocationListener(),
            networkListener = new CustomLocationListener();
    private Runnable reconnect = new Runnable() {
        @Override
        public void run() {
            try {
                if (!connection.isConnected())
                    connection.connect(String.format(Constants.URL, login, password), webSocketHandler);
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        init();
        initLocationListener();
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initLocationListener() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, networkListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
    }

    private void init() {
        sharedpreferences = getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE);
        login = sharedpreferences.getString(Constants.ARG_LOGIN, null);
        password = sharedpreferences.getString(Constants.ARG_PASSWORD, null);
        sharedpreferences.edit().putBoolean(Constants.IS_CONNECTED, true).apply();
        if (connection == null) {
            connection = new WebSocketConnection();
            try {
                connection.connect(String.format(Constants.URL, login, password), webSocketHandler);
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        if (connection != null) {
            connection.disconnect();
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(gpsListener);
        locationManager.removeUpdates(networkListener);
        unregisterReceiver(receiver);
    }

    WebSocketHandler webSocketHandler = new WebSocketHandler() {
        @Override
        public void onClose(int code, String reason) {
            super.onClose(code, reason);
            Log.d("CLOSE", reason + " " + code);
            if (isNetworkConnected()) {
                if (!executor.isShutdown())
                    executor.schedule(reconnect, 5000, TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public void onOpen() {
            super.onOpen();
            Toast.makeText(ServerService.this, R.string.successful_connection, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTextMessage(String payload) {
            super.onTextMessage(payload);
            List<Point> points = gson.fromJson(payload, listType);
            SugarRecord.saveInTx(points);
            sendBroadcast(new Intent(Constants.ACTION));
        }
    };


    public class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected() && connection != null && !connection.isConnected())
                try {
                    connection.connect(String.format(Constants.URL, login, password), webSocketHandler);
                } catch (WebSocketException e) {
                    e.printStackTrace();
                }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CustomLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (connection.isConnected()) {
                Point point = new Point();
                point.setLat(location.getLatitude());
                point.setLon(location.getLongitude());
                String payload = gson.toJson(point);
                connection.sendTextMessage(payload);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
