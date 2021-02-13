/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
 * Copyright (C) 2020 ProjectFluid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fluid.customisation.categories;

import static android.os.UserHandle.USER_CURRENT;
import static com.fluid.customisation.fragments.SystemTheme.SETTINGS_ACTIVE_OVERLAY_KEY;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import androidx.preference.*;
import android.provider.Settings;
import androidx.preference.Preference.OnPreferenceChangeListener;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.OverlayCategoryPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fluid.customisation.etc.CustomOverlayPreferenceController;

public class Themes extends DashboardFragment implements OnPreferenceChangeListener{

    public static final String TAG = "Themes";

    private static final String CUSTOM_CLOCK_FACE = Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE;
    private static final String DEFAULT_CLOCK = "com.android.keyguard.clock.DefaultClockController";

    private static final String KEY_SYSTEM_THEME = "system_icons";

    private ContentResolver mResolver;

    private ListPreference mLockClockStyles;
    private Preference mSystemTheme;
    private Context mContext;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FLUID;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mSystemTheme = (Preference) findPreference(KEY_SYSTEM_THEME);
        if (Settings.Secure.getString(getContext().getContentResolver(), SETTINGS_ACTIVE_OVERLAY_KEY) != null) {
            String inputString = Settings.Secure.getString(getContext().getContentResolver(), SETTINGS_ACTIVE_OVERLAY_KEY);
            String capitalizedString = inputString.substring(0, 1).toUpperCase() + inputString.substring(1);
            mSystemTheme.setSummary(capitalizedString);
        }

        mLockClockStyles = (ListPreference) findPreference(CUSTOM_CLOCK_FACE);
        String mLockClockStylesValue = getLockScreenCustomClockFace();
        mLockClockStyles.setValue(mLockClockStylesValue);
        mLockClockStyles.setSummary(mLockClockStyles.getEntry());
        mLockClockStyles.setOnPreferenceChangeListener(this);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockClockStyles) {
            setLockScreenCustomClockFace((String) newValue);
            int index = mLockClockStyles.findIndexOfValue((String) newValue);
            mLockClockStyles.setSummary(mLockClockStyles.getEntries()[index]);
            return true;
        }
        return false;

    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.customisation_themes;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new CustomOverlayPreferenceController(context,
                "android.theme.customization.custom_overlays"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.accent_color"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.font"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        return controllers;
    }

    private String getLockScreenCustomClockFace() {
        String value = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                CUSTOM_CLOCK_FACE, USER_CURRENT);

        if (value == null || value.isEmpty()) value = DEFAULT_CLOCK;

        try {
            JSONObject json = new JSONObject(value);
            return json.getString("clock");
        } catch (JSONException ex) {
        }
        return value;
    }

    private void setLockScreenCustomClockFace(String value) {
        try {
            JSONObject json = new JSONObject();
            json.put("clock", value);
            Settings.Secure.putStringForUser(mContext.getContentResolver(), CUSTOM_CLOCK_FACE,
                    json.toString(), USER_CURRENT);
        } catch (JSONException ex) {
        }
    }
}

