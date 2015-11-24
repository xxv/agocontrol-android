package com.agocontrol.agocontrol;

import android.preference.PreferenceActivity;

import java.util.List;


public class FragmentPreferences extends PreferenceActivity {
    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        return true;
    }
}
