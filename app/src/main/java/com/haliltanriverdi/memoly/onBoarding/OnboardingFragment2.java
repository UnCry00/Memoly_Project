package com.haliltanriverdi.memoly.onBoarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.haliltanriverdi.memoly.databinding.FragmentOnboarding2Binding;

public class OnboardingFragment2 extends Fragment {

    private FragmentOnboarding2Binding binding;

    public OnboardingFragment2() {
        // boş constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboarding2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
