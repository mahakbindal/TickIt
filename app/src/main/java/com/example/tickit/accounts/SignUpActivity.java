package com.example.tickit.accounts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.tickit.R;
import com.example.tickit.main.MainActivity;
import com.example.tickit.databinding.ActivitySignUpBinding;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";
    private ActivitySignUpBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        onSignUpClicked();
    }

    private void onSignUpClicked() {
        mBinding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mBinding.etSignUpUsername.getText().toString();
                String password = mBinding.etSignUpPassword.getText().toString();
                String confirmPassword = mBinding.etSignUpConfirmPassword.getText().toString();
                // Error: empty field
                if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, R.string.empty_field, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Error: passwords don't match
                if(!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, R.string.password_mismatch, Toast.LENGTH_SHORT).show();
                    return;
                }

                signUpAndGoToMainActivity(username, password);
            }
        });
    }

    private void signUpAndGoToMainActivity(String username, String password) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Issue with signing up");
                    Toast.makeText(SignUpActivity.this, R.string.signup_fail, Toast.LENGTH_SHORT).show();
                    return;
                }
                goLoginActivity();
                Toast.makeText(SignUpActivity.this, R.string.signup_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}