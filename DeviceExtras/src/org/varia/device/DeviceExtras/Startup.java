/*
* Copyright (C) 2013 The OmniROM Project
* Copyright (C) 2021 The dot OS Project
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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.PreferenceManager;

public class Startup extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        ComponentName DCModeTile = new ComponentName("org.varia.device.DeviceExtras",
    "org.varia.device.DeviceExtras.DCModeTileService");
        ComponentName PowerShareTile = new ComponentName("org.varia.device.DeviceExtras",
    "org.varia.device.DeviceExtras.PowerShareTileService");
        ComponentName QuietModeTile = new ComponentName("org.varia.device.DeviceExtras",
    "org.varia.device.DeviceExtras.QuietModeTileService");
        if (SystemProperties.get("ro.overlay.device", "").equals("instantnoodlep")) {
            pm.setComponentEnabledSetting(DCModeTile,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(PowerShareTile,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(QuietModeTile,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(DCModeTile,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(PowerShareTile,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(QuietModeTile,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);
        }    
        boolean enabled = false;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        enabled = sharedPrefs.getBoolean(DeviceExtras.KEY_HBM_SWITCH, false);
        if (enabled) {
        restore(HBMModeSwitch.getFile(), enabled);
               }
        enabled = sharedPrefs.getBoolean(DeviceExtras.KEY_DC_SWITCH, false);
        if (enabled) {
        restore(DCModeSwitch.getFile(), enabled);
               }
        enabled = sharedPrefs.getBoolean(DeviceExtras.KEY_FPS_INFO, false);
        if (enabled) {
            context.startService(new Intent(context, FPSInfoService.class));
               }
        enabled = sharedPrefs.getBoolean(DeviceExtras.KEY_GAME_SWITCH, false);
        if (enabled) {
        restore(GameModeSwitch.getFile(), enabled);
               }
        enabled = sharedPrefs.getBoolean(DeviceExtras.KEY_OTG_SWITCH, false);
        if (enabled) {
        restore(OTGModeSwitch.getFile(), enabled);
               }
        enabled = sharedPrefs.getBoolean(DeviceExtras.KEY_POWERSHARE_SWITCH, false);
        if (enabled) {
        restore(PowerShareModeSwitch.getFile(), enabled);
               }
        enabled = sharedPrefs.getBoolean(DeviceExtras.KEY_QUIETMODE_SWITCH, false);
        if (enabled) {
        restore(QuietModeSwitch.getFile(), enabled);
               }
        AdrenoBoostPreference.restore(context);
        DeviceExtras.restoreSliderStates(context);
        org.varia.device.DeviceExtras.doze.DozeUtils.checkDozeService(context);
        VibratorStrengthPreference.restore(context);
    }

    private void restore(String file, boolean enabled) {
        if (file == null) {
            return;
        }
        if (enabled) {
            FileUtils.writeValue(file, "1");
        }
    }

    private void restore(String file, String value) {
        if (file == null) {
            return;
        }
        FileUtils.writeValue(file, value);
    }
}
