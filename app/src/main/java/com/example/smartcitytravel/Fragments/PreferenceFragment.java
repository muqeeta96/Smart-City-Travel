package com.example.smartcitytravel.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smartcitytravel.R;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.fragment_preference, rootKey);
    }

    public Preference getDeleteAccountPreference() {
        return findPreference("delete_account");
    }

}