package com.wheely.testwheely;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by isinotov on 22/03/2016.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText loginEditText, passwordEditText;
    private Button signInButton;
    private static final int MY_PERMISSIONS_REQUEST_GET_FINE_LOCATION = 7;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loginEditText = (EditText) findViewById(R.id.loginEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        signInButton = (Button) findViewById(R.id.enterButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity activity = LoginActivity.this;
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(activity, R.string.location_permission_explanation, Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_GET_FINE_LOCATION);
                    }
                } else {
                    startMaps();
                }
            }
        });
        sharedPreferences = getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE);
        boolean isConnected = sharedPreferences.
                getBoolean(Constants.IS_CONNECTED, false);
        if (isConnected)
            startMaps();
    }

    private void startMaps() {
        String login = loginEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        sharedPreferences.edit().putString(Constants.ARG_LOGIN, login).putString(Constants.ARG_PASSWORD, password).apply();
        startActivity(new Intent(this, MapsActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startMaps();
                }
                break;
            }
        }
    }
}
