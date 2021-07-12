package com.example.tickit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.tickit.MainActivity;
import com.example.tickit.R;
import com.example.tickit.databinding.ActivityLoginBinding;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if(ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etLoginUsername.getText().toString();
                String password = binding.etLoginPassword.getText().toString();
                loginUser(username, password);
            }
        });
    }

    private void loginUser(String username, String password) {

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e != null){
                    // Login failure
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this, R.string.login_fail, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Login success
                goMainActivity();
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}