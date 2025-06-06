package com.haliltanriverdi.memoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.haliltanriverdi.memoly.onBoarding.OnboardingActivity;

public class LauncherActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "my_prefs";
    private static final String KEY_ONBOARDING_SHOWN = "onboarding_shown";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean onboardingShown = sharedPreferences.getBoolean(KEY_ONBOARDING_SHOWN, false);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

        if (!onboardingShown) {
            // İlk açılış, onboarding göster
            startActivity(new Intent(this, OnboardingActivity.class));
        } else if (!isLoggedIn) {
            // Onboarding gösterildi ama giriş yapılmamış
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            // Giriş yapılmış, ana ekrana yönlendir
            startActivity(new Intent(this, MainActivity.class));
        }

        finish(); // LauncherActivity kapansın
    }
}
