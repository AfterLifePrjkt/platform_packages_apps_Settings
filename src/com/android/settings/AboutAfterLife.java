package com.android.settings;

import android.os.Bundle;

public class AboutAfterLife extends SettingsPreferenceFragment {
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.about_afterlife);
    }

    public int getMetricsCategory() {
        return 1150;
    }
}