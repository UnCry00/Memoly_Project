package com.haliltanriverdi.memoly.onBoarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.haliltanriverdi.memoly.databinding.FragmentOnboarding1Binding;

public class OnboardingFragment1 extends Fragment {

    private FragmentOnboarding1Binding binding;

    public OnboardingFragment1() {
        // boş constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboarding1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
