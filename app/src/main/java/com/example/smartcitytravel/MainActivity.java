package com.example.smartcitytravel;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Activities.HomeActivity;
import com.example.smartcitytravel.Activities.LoginActivity;
import com.example.smartcitytravel.Util.PreferenceHandler;

public class MainActivity extends AppCompatActivity {
    private PreferenceHandler preferenceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceHandler = new PreferenceHandler();
        checkUserAlreadySignIn();
    }

    public void checkUserAlreadySignIn() {
        Boolean checkPreferenceExist = preferenceHandler.checkLoggedInAccountPreferenceExist(MainActivity.this);

        if (checkPreferenceExist) {
            moveToHomeActivity();
        } else {
            moveToLoginActivity();
        }
    }


    public void moveToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void moveToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}

