/*
* Copyright (C) 2016 The OmniROM Project
* Copyright (C) 2021 The dot X Project
* Copyright (C) 2018-2021 crDroid Android Project
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

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import org.varia.support.preferences.SwitchPreference;
import androidx.preference.TwoStatePreference;

import java.util.Arrays;

import org.varia.device.DeviceExtras.Constants;
import org.varia.device.DeviceExtras.doze.DozeSettingsActivity;
import org.varia.device.DeviceExtras.FileUtils;
import org.varia.device.DeviceExtras.R;

import org.varia.support.preferences.CustomSeekBarPreference;

public class DeviceExtras extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = DeviceExtras.class.getSimpleName();

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";

    public static final String KEY_ADRENOBOOST = "adrenoboost";
    public static final String KEY_DOZE = "advanced_doze_settings";
    public static final String KEY_DC_SWITCH = "dc";
    public static final String KEY_FPS_INFO = "fps_info";
    public static final String KEY_FPS_INFO_POSITION = "fps_info_position";
    public static final String KEY_FPS_INFO_COLOR = "fps_info_color";
    public static final String KEY_FPS_INFO_TEXT_SIZE = "fps_info_text_size";
    public static final String KEY_GAME_SWITCH = "game_mode";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_OTG_SWITCH = "otg";
    public static final String KEY_TOUCHSCREEN="touchscreen";
    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_POWERSHARE_SWITCH = "powershare";
    public static final String KEY_QUIETMODE_SWITCH = "quiet_mode";
    public static final String KEY_CATEGORY_POWER = "power";
    
    private static ListPreference mFpsInfoPosition;
    private static ListPreference mFpsInfoColor;
    private static SwitchPreference mFpsInfo;
    private static TwoStatePreference mDCModeSwitch;
    private static TwoStatePreference mGameModeSwitch;
    private static TwoStatePreference mHBMModeSwitch;
    private static TwoStatePreference mOTGModeSwitch;
    private static TwoStatePreference mPowerShareModeSwitch;
    private static TwoStatePreference mQuietModeSwitch;

    private AdrenoBoostPreference mAdrenoBoost;
    private CustomSeekBarPreference mFpsInfoTextSizePreference;
    private Preference mDozeSettings;
    private ListPreference mBottomKeyPref;
    private ListPreference mMiddleKeyPref;
    private ListPreference mTopKeyPref;
    private VibratorStrengthPreference mVibratorStrength;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        addPreferencesFromResource(R.xml.main);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        // DozeSettings Activity
        mDozeSettings = (Preference)findPreference(KEY_DOZE);
        mDozeSettings.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), DozeSettingsActivity.class);
            startActivity(intent);
            return true;
        });
        
        // DC-Dimming
        mDCModeSwitch = (TwoStatePreference) findPreference(KEY_DC_SWITCH);
        if (DCModeSwitch.isSupported()) {
            mDCModeSwitch.setChecked(DCModeSwitch.isCurrentlyEnabled(this.getContext()));
            mDCModeSwitch.setOnPreferenceChangeListener(new DCModeSwitch());
        } else { 
             findPreference(KEY_DC_SWITCH).setVisible(false);
        }    

        // HBM
        mHBMModeSwitch = (TwoStatePreference) findPreference(KEY_HBM_SWITCH);
        mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported());
        mHBMModeSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceExtras.KEY_HBM_SWITCH, false));
        mHBMModeSwitch.setOnPreferenceChangeListener(this);

        // FPS
        mFpsInfo = (SwitchPreference) findPreference(KEY_FPS_INFO);
        mFpsInfo.setChecked(prefs.getBoolean(KEY_FPS_INFO, false));
        mFpsInfo.setOnPreferenceChangeListener(this);

        mFpsInfoPosition = (ListPreference) findPreference(KEY_FPS_INFO_POSITION);
        mFpsInfoPosition.setOnPreferenceChangeListener(this);

        mFpsInfoColor = (ListPreference) findPreference(KEY_FPS_INFO_COLOR);
        mFpsInfoColor.setOnPreferenceChangeListener(this);

        mFpsInfoTextSizePreference = (CustomSeekBarPreference) findPreference(KEY_FPS_INFO_TEXT_SIZE);
        mFpsInfoTextSizePreference.setOnPreferenceChangeListener(this);
        
        // GPU
        mAdrenoBoost = (AdrenoBoostPreference) findPreference(KEY_ADRENOBOOST);
        if (mAdrenoBoost != null) {
            mAdrenoBoost.setEnabled(AdrenoBoostPreference.isSupported());
        }

        // GameMode
        if (getResources().getBoolean(R.bool.config_deviceSupportsHighSampleRate)) {
        mGameModeSwitch = (TwoStatePreference) findPreference(KEY_GAME_SWITCH);
        mGameModeSwitch.setEnabled(GameModeSwitch.isSupported());
        mGameModeSwitch.setChecked(GameModeSwitch.isCurrentlyEnabled(this.getContext()));
        mGameModeSwitch.setOnPreferenceChangeListener(new GameModeSwitch());
        } else {
            getPreferenceScreen().removePreference((Preference) findPreference(KEY_TOUCHSCREEN));
        }
        
        // OTG
        mOTGModeSwitch = (TwoStatePreference) findPreference(KEY_OTG_SWITCH);
        mOTGModeSwitch.setEnabled(OTGModeSwitch.isSupported());
        mOTGModeSwitch.setChecked(OTGModeSwitch.isCurrentlyEnabled(this.getContext()));
        mOTGModeSwitch.setOnPreferenceChangeListener(new OTGModeSwitch());

        if (SystemProperties.get("ro.overlay.device", "").equals("instantnoodlep")) {
            mPowerShareModeSwitch = (TwoStatePreference) findPreference(KEY_POWERSHARE_SWITCH);
            if (PowerShareModeSwitch.isSupported()) {
                mPowerShareModeSwitch.setChecked(PowerShareModeSwitch.isCurrentlyEnabled(this.getContext()));
                mPowerShareModeSwitch.setOnPreferenceChangeListener(new PowerShareModeSwitch());
            }
            mQuietModeSwitch = (TwoStatePreference) findPreference(KEY_QUIETMODE_SWITCH);
            if (QuietModeSwitch.isSupported()) {
                mQuietModeSwitch.setChecked(QuietModeSwitch.isCurrentlyEnabled(this.getContext()));
                mQuietModeSwitch.setOnPreferenceChangeListener(new QuietModeSwitch());
            }
        } else {
            getPreferenceScreen().removePreference((PreferenceCategory) findPreference(KEY_CATEGORY_POWER));
        }

        // Slider Preferences
        initNotificationSliderPreference();

        // Vibrator
        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null) {
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        }
    }

    public static boolean isHBMModeService(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceExtras.KEY_HBM_SWITCH, false);
    }

    private void initNotificationSliderPreference() {
        registerPreferenceListener(Constants.NOTIF_SLIDER_USAGE_KEY);
        registerPreferenceListener(Constants.NOTIF_SLIDER_ACTION_TOP_KEY);
        registerPreferenceListener(Constants.NOTIF_SLIDER_ACTION_MIDDLE_KEY);
        registerPreferenceListener(Constants.NOTIF_SLIDER_ACTION_BOTTOM_KEY);

        ListPreference usagePref = (ListPreference) findPreference(
                Constants.NOTIF_SLIDER_USAGE_KEY);
        handleSliderUsageChange(usagePref.getValue());
    }

    private void registerPreferenceListener(String key) {
        Preference p = findPreference(key);
        p.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled(this.getContext()));
        mFpsInfo.setChecked(isFPSOverlayRunning());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mHBMModeSwitch) {
            Boolean enabled = (Boolean) newValue;
            FileUtils.writeValue(HBMModeSwitch.getFile(), enabled ? "1" : "0");
            Intent hbmIntent = new Intent(this.getContext(),
                    org.varia.device.DeviceExtras.HBMModeService.class);
            if (enabled) {
                this.getContext().startService(hbmIntent);
            } else {
                this.getContext().stopService(hbmIntent);
            }
            return true;
          } else if (preference == mFpsInfo) {
            boolean enabled = (Boolean) newValue;
            Intent fpsinfo = new Intent(this.getContext(),
                    org.varia.device.DeviceExtras.FPSInfoService.class);
            if (enabled) {
                this.getContext().startService(fpsinfo);
            } else {
                this.getContext().stopService(fpsinfo);
            }
            return true;
        } else if (preference == mFpsInfoPosition) {
            int position = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isPositionChanged(mContext, position)) {
                FPSInfoService.setPosition(mContext, position);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
            return true;
        } else if (preference == mFpsInfoColor) {
            int color = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isColorChanged(mContext, color)) {
                FPSInfoService.setColorIndex(mContext, color);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
            return true;
        } else if (preference == mFpsInfoTextSizePreference) {
            int size = Integer.parseInt(newValue.toString());
            Context mContext = getContext();
            if (FPSInfoService.isSizeChanged(mContext, size - 1)) {
                FPSInfoService.setSizeIndex(mContext, size - 1);
                if (isFPSOverlayRunning()) {
                    restartFpsInfo(mContext);
                }
            }
            return true;

            }

        String key = preference.getKey();
        switch (key) {
            case Constants.NOTIF_SLIDER_USAGE_KEY:
                return handleSliderUsageChange((String) newValue) &&
                        handleSliderUsageDefaultsChange((String) newValue) &&
                        notifySliderUsageChange((String) newValue);
            case Constants.NOTIF_SLIDER_ACTION_TOP_KEY:
                return notifySliderActionChange(0, (String) newValue);
            case Constants.NOTIF_SLIDER_ACTION_MIDDLE_KEY:
                return notifySliderActionChange(1, (String) newValue);
            case Constants.NOTIF_SLIDER_ACTION_BOTTOM_KEY:
                return notifySliderActionChange(2, (String) newValue);
            default:
                break;
        }

        String node = Constants.sBooleanNodePreferenceMap.get(key);
        if (!TextUtils.isEmpty(node) && FileUtils.fileWritable(node)) {
            Boolean value = (Boolean) newValue;
            FileUtils.writeValue(node, value ? "1" : "0");
            return true;
        }
        node = Constants.sStringNodePreferenceMap.get(key);
        if (!TextUtils.isEmpty(node) && FileUtils.fileWritable(node)) {
            FileUtils.writeValue(node, (String) newValue);
            return true;
        }

        return false;
    }

    @Override
    public void addPreferencesFromResource(int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        // Initialize node preferences
        for (String pref : Constants.sBooleanNodePreferenceMap.keySet()) {
            SwitchPreference b = (SwitchPreference) findPreference(pref);
            if (b == null) continue;
            String node = Constants.sBooleanNodePreferenceMap.get(pref);
            if (FileUtils.isFileReadable(node)) {
                String curNodeValue = FileUtils.readOneLine(node);
                b.setChecked(curNodeValue.equals("1"));
                b.setOnPreferenceChangeListener(this);
            } else {
                removePref(b);
            }
        }
        for (String pref : Constants.sStringNodePreferenceMap.keySet()) {
            ListPreference l = (ListPreference) findPreference(pref);
            if (l == null) continue;
            String node = Constants.sStringNodePreferenceMap.get(pref);
            if (FileUtils.isFileReadable(node)) {
                l.setValue(FileUtils.readOneLine(node));
                l.setOnPreferenceChangeListener(this);
            } else {
                removePref(l);
            }
        }
    }

    private void removePref(Preference pref) {
        PreferenceGroup parent = pref.getParent();
        if (parent == null) {
            return;
        }
        parent.removePreference(pref);
        if (parent.getPreferenceCount() == 0) {
            removePref(parent);
        }
    }

    private boolean handleSliderUsageChange(String newValue) {
        switch (newValue) {
            case Constants.NOTIF_SLIDER_FOR_NOTIFICATION:
                return updateSliderActions(
                        R.array.notification_slider_mode_entries,
                        R.array.notification_slider_mode_entry_values);
            case Constants.NOTIF_SLIDER_FOR_FLASHLIGHT:
                return updateSliderActions(
                        R.array.notification_slider_flashlight_entries,
                        R.array.notification_slider_flashlight_entry_values);
            case Constants.NOTIF_SLIDER_FOR_BRIGHTNESS:
                return updateSliderActions(
                        R.array.notification_slider_brightness_entries,
                        R.array.notification_slider_brightness_entry_values);
            case Constants.NOTIF_SLIDER_FOR_ROTATION:
                return updateSliderActions(
                        R.array.notification_slider_rotation_entries,
                        R.array.notification_slider_rotation_entry_values);
            case Constants.NOTIF_SLIDER_FOR_RINGER:
                return updateSliderActions(
                        R.array.notification_slider_ringer_entries,
                        R.array.notification_slider_ringer_entry_values);
            case Constants.NOTIF_SLIDER_FOR_NOTIFICATION_RINGER:
                return updateSliderActions(
                        R.array.notification_ringer_slider_mode_entries,
                        R.array.notification_ringer_slider_mode_entry_values);
            default:
                return false;
        }
    }

    private boolean handleSliderUsageDefaultsChange(String newValue) {
        int defaultsResId = getDefaultResIdForUsage(newValue);
        if (defaultsResId == 0) {
            return false;
        }
        return updateSliderActionDefaults(defaultsResId);
    }

    private boolean updateSliderActions(int entriesResId, int entryValuesResId) {
        String[] entries = getResources().getStringArray(entriesResId);
        String[] entryValues = getResources().getStringArray(entryValuesResId);
        return updateSliderPreference(Constants.NOTIF_SLIDER_ACTION_TOP_KEY,
                entries, entryValues) &&
            updateSliderPreference(Constants.NOTIF_SLIDER_ACTION_MIDDLE_KEY,
                    entries, entryValues) &&
            updateSliderPreference(Constants.NOTIF_SLIDER_ACTION_BOTTOM_KEY,
                    entries, entryValues);
    }

    private boolean updateSliderActionDefaults(int defaultsResId) {
        String[] defaults = getResources().getStringArray(defaultsResId);
        if (defaults.length != 3) {
            return false;
        }

        return updateSliderPreferenceValue(Constants.NOTIF_SLIDER_ACTION_TOP_KEY,
                defaults[0]) &&
            updateSliderPreferenceValue(Constants.NOTIF_SLIDER_ACTION_MIDDLE_KEY,
                    defaults[1]) &&
            updateSliderPreferenceValue(Constants.NOTIF_SLIDER_ACTION_BOTTOM_KEY,
                    defaults[2]);
    }

    private boolean updateSliderPreference(CharSequence key,
            String[] entries, String[] entryValues) {
        ListPreference pref = (ListPreference) findPreference(key);
        if (pref == null) {
            return false;
        }
        pref.setEntries(entries);
        pref.setEntryValues(entryValues);
        return true;
    }

    private boolean updateSliderPreferenceValue(CharSequence key,
            String value) {
        ListPreference pref = (ListPreference) findPreference(key);
        if (pref == null) {
            return false;
        }
        pref.setValue(value);
        return true;
    }

    private int[] getCurrentSliderActions() {
        int[] actions = new int[3];
        ListPreference p;

        p = (ListPreference) findPreference(
                Constants.NOTIF_SLIDER_ACTION_TOP_KEY);
        actions[0] = Integer.parseInt(p.getValue());

        p = (ListPreference) findPreference(
                Constants.NOTIF_SLIDER_ACTION_MIDDLE_KEY);
        actions[1] = Integer.parseInt(p.getValue());

        p = (ListPreference) findPreference(
                Constants.NOTIF_SLIDER_ACTION_BOTTOM_KEY);
        actions[2] = Integer.parseInt(p.getValue());

        return actions;
    }

    private boolean notifySliderUsageChange(String usage) {
        sendUpdateBroadcast(getActivity().getApplicationContext(), Integer.parseInt(usage),
                getCurrentSliderActions());
        return true;
    }

    private boolean notifySliderActionChange(int index, String value) {
        ListPreference p = (ListPreference) findPreference(
                Constants.NOTIF_SLIDER_USAGE_KEY);
        int usage = Integer.parseInt(p.getValue());

        int[] actions = getCurrentSliderActions();
        actions[index] = Integer.parseInt(value);

        sendUpdateBroadcast(getActivity().getApplicationContext(), usage, actions);
        return true;
    }

    public static void sendUpdateBroadcast(Context context,
            int usage, int[] actions) {
        Intent intent = new Intent(Constants.ACTION_UPDATE_SLIDER_SETTINGS);
        intent.putExtra(Constants.EXTRA_SLIDER_USAGE, usage);
        intent.putExtra(Constants.EXTRA_SLIDER_ACTIONS, actions);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        Log.d(TAG, "update slider usage " + usage + " with actions: " +
                Arrays.toString(actions));
    }

    public static void restoreSliderStates(Context context) {
        Resources res = context.getResources();
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName() + "_preferences", Context.MODE_PRIVATE);

        String usage = prefs.getString(Constants.NOTIF_SLIDER_USAGE_KEY,
                res.getString(R.string.config_defaultNotificationSliderUsage));

        int defaultsResId = getDefaultResIdForUsage(usage);
        if (defaultsResId == 0) {
            return;
        }

        String[] defaults = res.getStringArray(defaultsResId);
        if (defaults.length != 3) {
            return;
        }

        String actionTop = prefs.getString(
                Constants.NOTIF_SLIDER_ACTION_TOP_KEY, defaults[0]);

        String actionMiddle = prefs.getString(
                Constants.NOTIF_SLIDER_ACTION_MIDDLE_KEY, defaults[1]);

        String actionBottom = prefs.getString(
                Constants.NOTIF_SLIDER_ACTION_BOTTOM_KEY, defaults[2]);

        prefs.edit()
            .putString(Constants.NOTIF_SLIDER_USAGE_KEY, usage)
            .putString(Constants.NOTIF_SLIDER_ACTION_TOP_KEY, actionTop)
            .putString(Constants.NOTIF_SLIDER_ACTION_MIDDLE_KEY, actionMiddle)
            .putString(Constants.NOTIF_SLIDER_ACTION_BOTTOM_KEY, actionBottom)
            .commit();

        sendUpdateBroadcast(context, Integer.parseInt(usage), new int[] {
            Integer.parseInt(actionTop),
            Integer.parseInt(actionMiddle),
            Integer.parseInt(actionBottom)
        });
    }

    private static int getDefaultResIdForUsage(String usage) {
        switch (usage) {
            case Constants.NOTIF_SLIDER_FOR_NOTIFICATION:
                return R.array.config_defaultSliderActionsForNotification;
            case Constants.NOTIF_SLIDER_FOR_FLASHLIGHT:
                return R.array.config_defaultSliderActionsForFlashlight;
            case Constants.NOTIF_SLIDER_FOR_BRIGHTNESS:
                return R.array.config_defaultSliderActionsForBrightness;
            case Constants.NOTIF_SLIDER_FOR_ROTATION:
                return R.array.config_defaultSliderActionsForRotation;
            case Constants.NOTIF_SLIDER_FOR_RINGER:
                return R.array.config_defaultSliderActionsForRinger;
            case Constants.NOTIF_SLIDER_FOR_NOTIFICATION_RINGER:
                return R.array.config_defaultSliderActionsForNotificationRinger;
            default:
                return 0;
        }
    }

    private boolean isFPSOverlayRunning() {
        ActivityManager am = (ActivityManager) getContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                am.getRunningServices(Integer.MAX_VALUE))
            if (FPSInfoService.class.getName().equals(service.service.getClassName()))
                return true;
        return false;
   }

    private void restartFpsInfo(Context context) {
        Intent fpsinfo = new Intent(context, FPSInfoService.class);
        context.stopService(fpsinfo);
        context.startService(fpsinfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
