package com.wheely.testwheely;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by isinotov on 22/03/2016.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText login, password;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        login = (EditText) findViewById(R.id.loginEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        signInButton = (Button) findViewById(R.id.enterButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                intent.putExtra(MapsActivity.ARG_LOGIN, login.getText().toString());
                intent.putExtra(MapsActivity.ARG_PASSWORD, password.getText().toString());
                startActivity(intent);
            }
        });
    }
}
