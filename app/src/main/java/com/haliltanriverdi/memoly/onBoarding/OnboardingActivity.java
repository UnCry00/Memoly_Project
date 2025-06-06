package com.haliltanriverdi.memoly.onBoarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.haliltanriverdi.memoly.LoginActivity;
import com.haliltanriverdi.memoly.onBoarding.adapter.OnboardingPagerAdapter;
import com.haliltanriverdi.memoly.databinding.ActivityOnboardingBinding;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.dotsIndicator.attachTo(binding.viewPager);

        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();

            if (current < 2) {
                binding.viewPager.setCurrentItem(current + 1);
            } else {
                // Son sayfadaysa, giriş sayfasına yönlendir
                SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("onboarding_shown", true);
                editor.apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });


    }
}


