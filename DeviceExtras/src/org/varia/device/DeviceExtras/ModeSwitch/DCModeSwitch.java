/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.varia.device.DeviceExtras;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import org.varia.device.DeviceExtras.DeviceExtras;

public class DCModeSwitch implements OnPreferenceChangeListener {

    private static final String FILE = "/sys/kernel/oplus_display/dimlayer_hbm";

    public static String getFile() {
        if (FileUtils.fileWritable(FILE)) {
            return FILE;
        }
        return null;
    }

    public static boolean isSupported() {
        if (SystemProperties.get("ro.overlay.device", "").equals("instantnoodlep")) {
            return FileUtils.fileWritable(getFile());
        } else {
            return false;
        }
    }    

    public static boolean isCurrentlyEnabled(Context context) {
        return FileUtils.getFileValueAsBoolean(getFile(), false);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Boolean enabled = (Boolean) newValue;
        FileUtils.writeValue(getFile(), enabled ? "1" : "0");
        return true;
    }
}
