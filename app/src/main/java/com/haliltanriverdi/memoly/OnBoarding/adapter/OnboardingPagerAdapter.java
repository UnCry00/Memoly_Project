package com.haliltanriverdi.memoly.OnBoarding.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.haliltanriverdi.memoly.OnBoarding.OnboardingFragment1;
import com.haliltanriverdi.memoly.OnBoarding.OnboardingFragment2;
import com.haliltanriverdi.memoly.OnBoarding.OnboardingFragment3;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    public OnboardingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OnboardingFragment1();
            case 1:
                return new OnboardingFragment2();
            case 2:
                return new OnboardingFragment3();
            default:
                return new OnboardingFragment1();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
