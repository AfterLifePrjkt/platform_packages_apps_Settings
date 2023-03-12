/*
 * Copyright (C) 2020 Wave-OS
 * Copyright (C) 2021 ShapeShiftOS
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

package com.android.settings.deviceinfo.aboutphone;

import java.io.IOException;
import android.content.Context;
import android.os.SystemProperties;
import android.widget.TextView;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.utils.AxlSpecUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.Utils;
import com.android.settings.core.PreferenceControllerMixin;

public class AxlInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_AXL_INFO = "axl_info";

    public AxlInfoPreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference axlInfoPreference = screen.findPreference(KEY_AXL_INFO);
        final TextView processor = (TextView) axlInfoPreference.findViewById(R.id.processor_message);
        final TextView storage = (TextView) axlInfoPreference.findViewById(R.id.storage_code_message);
        final TextView battery = (TextView) axlInfoPreference.findViewById(R.id.battery_type_message);
        final TextView infoScreen = (TextView) axlInfoPreference.findViewById(R.id.screen_message);
        processor.setText(AxlSpecUtils.getProcessorModel());
        storage.setText(String.valueOf(AxlSpecUtils.getTotalInternalMemorySize()) + "GB ROM + " + String.valueOf(AxlSpecUtils.getTotalRAM()) + "GB RAM");
        battery.setText(AxlSpecUtils.getBatteryCapacity(mContext) + " mAh");
        infoScreen.setText(AxlSpecUtils.getScreenRes(mContext));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_AXL_INFO;
    }
}
