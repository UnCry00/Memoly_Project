package com.haliltanriverdi.memoly.onBoarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.haliltanriverdi.memoly.databinding.FragmentOnboarding3Binding;

public class OnboardingFragment3 extends Fragment {

    private FragmentOnboarding3Binding binding;

    public OnboardingFragment3() {
        // boş constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboarding3Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
