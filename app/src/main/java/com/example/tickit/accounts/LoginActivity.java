package com.example.tickit.accounts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.tickit.R;
import com.example.tickit.main.MainActivity;
import com.example.tickit.databinding.ActivityLoginBinding;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private ActivityLoginBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        if(ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        // User attempts to login
        onLoginClicked();

        // User attempts to go to sign up activity
        onSignUpClicked();
    }

    private void onLoginClicked() {
        mBinding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mBinding.etLoginUsername.getText().toString();
                String password = mBinding.etLoginPassword.getText().toString();
                loginUser(username, password);
            }
        });
    }

    private void onSignUpClicked() {
        mBinding.btnLoginSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSignUpActivity();
            }
        });
    }

    private void goSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginUser(String username, String password) {

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException exception) {
                if(exception != null){
                    // Login failure
                    Log.e(TAG, "Issue with login", exception);
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