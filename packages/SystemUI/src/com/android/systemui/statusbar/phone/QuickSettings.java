/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.systemui.statusbar.phone;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.quicktiles.*;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.QuickTileController;
import com.android.systemui.statusbar.policy.QuickTileController.QuickTileChangeCallback;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.text.util.QuickTileToken;
import android.text.util.QuickTileTokenizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 *
 */
public class QuickSettings implements QuickTileChangeCallback {
    static final boolean DEBUG_GONE_TILES = false;
    private static final String TAG = "QuickSettings";
    public static final boolean SHOW_IME_TILE = false;

    public static final boolean LONG_PRESS_TOGGLES = true;

    private Context mContext;
    private PanelBar mBar;
    private QuickSettingsContainerView mContainerView;

    private PhoneStatusBar mStatusBarService;

	@SuppressWarnings("unused")
	private QuickTileController mTileController;
	private LayoutInflater mLayoutInflater;

	private NetworkController mNetworkController;
	private BatteryController mBatteryController;
    boolean mTilesSetUp = false;

    private Handler mHandler = new Handler();

	private ArrayList<QuickSettingsTileContent> mAllCustomTiles = new ArrayList<QuickSettingsTileContent>();

	//private final HashMap<String, Boolean> mConfigs = new HashMap<String, Boolean>();
	private static final HashMap<String, Class<? extends QuickSettingsTileContent>> SETTINGS = 
			new HashMap<String, Class<? extends QuickSettingsTileContent>>();

	static {
		SETTINGS.put(Settings.System.QUICK_AIRPLANE, AirplaneModeTile.class);
		SETTINGS.put(Settings.System.QUICK_ROTATE, AutoRotateTile.class);
		SETTINGS.put(Settings.System.QUICK_BRIGHTNESS, BrightnessTile.class);
		SETTINGS.put(Settings.System.QUICK_NODISTURB, DoNotDisturbTile.class); 
		SETTINGS.put(Settings.System.QUICK_TORCH, TorchTile.class);
		SETTINGS.put(Settings.System.QUICK_SETTING, SettingsShortcutTile.class);
		SETTINGS.put(Settings.System.QUICK_WIFI, WifiTile.class); 
		SETTINGS.put(Settings.System.QUICK_VOLUME, VolumeTile.class);
		SETTINGS.put(Settings.System.QUICK_LTE, LTETile.class);
		SETTINGS.put(Settings.System.QUICK_CUSTOM, CustomTile.class);
		SETTINGS.put(Settings.System.QUICK_BLUETOOTH, BluetoothTile.class);
		SETTINGS.put(Settings.System.QUICK_ADB, WirelessADBTile.class);
		SETTINGS.put(Settings.System.QUICK_GPS, GPSModeTile.class);
		SETTINGS.put(Settings.System.QUICK_MOBILE_DATA, MobileDataTile.class);
		SETTINGS.put(Settings.System.QUICK_SYNC, SyncDataTile.class);
		SETTINGS.put(Settings.System.QUICK_MEDIA, AlbumArtTile.class);
		SETTINGS.put(Settings.System.QUICK_HOTSPOT, HotspotTile.class);
		SETTINGS.put(Settings.System.QUICK_TETHER, USBTetherTile.class);
		SETTINGS.put(Settings.System.QUICK_SIGNAL, SignalTile.class);
		SETTINGS.put(Settings.System.QUICK_BATTERY, BatteryTile.class);
		SETTINGS.put(Settings.System.QUICK_ALARM, AlarmTile.class);
		SETTINGS.put(Settings.System.QUICK_USER, UserTile.class);
		SETTINGS.put(Settings.System.QUICK_QUIET_HOURS, QuietHoursTile.class);

	}

    public QuickSettings(Context context, QuickSettingsContainerView container) {

        mContext = context;
        mContainerView = container;
    }

    void setBar(PanelBar bar) {
        mBar = bar;
    }

    public void setService(PhoneStatusBar phoneStatusBar) {
        mStatusBarService = phoneStatusBar;
    }

    public PhoneStatusBar getService() {
        return mStatusBarService;
    }

    public void setImeWindowStatus(boolean visible) {
    }

    void setup(final NetworkController networkController, final BatteryController batteryController) {

		mNetworkController = networkController;
		mBatteryController = batteryController;
        setupQuickSettings();
        updateResources();

		mTileController = new QuickTileController(mContext, new Handler(), this);

		mTilesSetUp = true;
    }

    public void setupQuickSettings() {
        // Setup the tiles that we are going to be showing (including the temporary ones)
        mLayoutInflater = LayoutInflater.from(mContext);
        addCustomTiles(mContainerView, mLayoutInflater);

		for(QuickSettingsTileContent qs: mAllCustomTiles){
			if(qs instanceof WifiTile){
				mNetworkController.addNetworkSignalChangedCallback((WifiTile)qs);
            }
			if(qs instanceof SignalTile){
				mNetworkController.addNetworkSignalChangedCallback((SignalTile)qs);
            }
			if(qs instanceof BatteryTile){
				mBatteryController.addStateChangedCallback((BatteryTile)qs);
            }
        }
    }

	private void addCustomTiles(final ViewGroup parent, final LayoutInflater inflater) {
		String savedSettings = Settings.System.getString(mContext.getContentResolver(), 
				Settings.System.QUICK_SETTINGS_TILES, Settings.System.QUICK_TILES_DEFAULT);
		if (savedSettings == "") {
			savedSettings = Settings.System.QUICK_TILES_DEFAULT;
		}

		final String settings = savedSettings;
		
		for(QuickTileToken token : QuickTileTokenizer.tokenize(settings)) {
			//Boolean enabled = mConfigs.getNonNull(token.getName(), true);
			if(SETTINGS.containsKey(token.getName())){
				try {
					QuickSettingsTileContent pref = inflateNewTile(token);
					if (pref != null) {
						mAllCustomTiles.add(pref);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}         
			}
		}
    }

    void updateResources() {

		// update custom tiles
		for(QuickSettingsTileContent qs : mAllCustomTiles){
			try{
				qs.refreshResources();
			}catch(Exception e) {
				Log.e(TAG, "Error on refresh ("+((qs==null)?"None selected":qs.getTag())+")");
			}
		}
        ((QuickSettingsContainerView)mContainerView).updateResources();
        mContainerView.requestLayout();
    }

	public void removeAll() {
		if(mAllCustomTiles.isEmpty()) return;
		
		for(QuickSettingsTileContent qs: mAllCustomTiles) {
			if(qs instanceof WifiTile) {
				mNetworkController.removeNetworkSignalChangedCallback((WifiTile)qs);
			}
			if(qs instanceof SignalTile) {
				mNetworkController.removeNetworkSignalChangedCallback((SignalTile)qs);
			}
			if(qs instanceof BatteryTile) {
				mBatteryController.removeStateChangedCallback((BatteryTile)qs);
			}
			try {
				qs.release();
				qs = null;
			} catch(Exception e) {
				Log.e(TAG, "Error on remove ("+((qs==null)?"None selected":qs.getTag())+")");
			}
		}
		mContainerView.removeAllViews();
		mAllCustomTiles.clear();
	}

	private QuickSettingsTileContent inflateNewTile(QuickTileToken token){
		QuickSettingsTileContent pref;
		try {
			// inflate the setting
			final QuickSettingsTileView tileView = (QuickSettingsTileView)mLayoutInflater.inflate(R.layout.quick_settings_tile, mContainerView, false);
			tileView.setContent(R.layout.quick_settings_tile_general, mLayoutInflater);
			// get the tile size from the setting
			tileView.setRowSpan(token.getRows());
			tileView.setColumnSpan(token.getColumns());
			//tileView.setColor(mEggEnabled);
			tileView.setTag(token);

			Class<?> cls = SETTINGS.get(token.getName());

			Constructor<?> con = cls.getConstructor(new Class[]{Context.class, View.class});
			pref = (QuickSettingsTileContent)con.newInstance(new Object[]{mContext, tileView.getChildAt(0)});

			pref.setDimensions(token.getRows(), token.getColumns());
			pref.mCallBack = new QuickSettingsTileContent.TileCallback(){

				@Override
				public void changeSize(int height, int width) {
					// removing and adding the view to take advantage
					// of the layout transitions
					mContainerView.removeView(tileView);
					tileView.setRowSpan(height);
					tileView.setColumnSpan(width);
					final int position = QuickSettingsTileHelper.getPosition(mContext,tileView.getTag());
					if(position>mContainerView.getChildCount() || position < 0){
						mContainerView.addView(tileView);
					}else{
						mContainerView.addView(tileView, position);
					}
				}

				@Override
				public void show(boolean visible) {
					if(visible && mContainerView.indexOfChild(tileView)==-1){
						final int position = QuickSettingsTileHelper.getPosition(mContext, tileView.getTag());
						if(position>mContainerView.getChildCount() || position < 0){
							mContainerView.addView(tileView);
						}else{
							mContainerView.addView(tileView, position);
						}
					}else if (!visible && mContainerView.indexOfChild(tileView)!=-1){
						mContainerView.removeView(tileView);
					}
				}

				@Override
				public void refreshTiles() {
					updateResources();
				}

			};
			
			mContainerView.addView(tileView);
			return pref;
		}catch(Exception e){
			e.printStackTrace();
			return null; 
		}
	}

	@Override
	public void onColumnsChange() {
		mContainerView.updateResources();
	}

	@Override
	public void onTileChange(List<QuickTileToken> newList) {
		
		if(!mTilesSetUp) return;

		List<View> keeperViews = new ArrayList<View>();
		List<QuickSettingsTileContent> keeperContents = new ArrayList<QuickSettingsTileContent>();

		// unhide tiles
		for(QuickSettingsTileContent pref: mAllCustomTiles){
			pref.mCallBack.show(true);
		}
		if(mContainerView.getChildCount() != mAllCustomTiles.size()){
			Log.e(TAG, "onTileChange: array sizes do not match, exiting");
			return;
		}
		// find out which ones we need to keep
		for(QuickTileToken token: newList){
			for(int c = 0; c < mContainerView.getChildCount(); c++){
				final QuickTileToken tag = (QuickTileToken)mContainerView.getChildAt(c).getTag();
				if(tag.getName().equals(token.getName())){
					keeperViews.add(mContainerView.getChildAt(c));
					keeperContents.add(mAllCustomTiles.get(c));
					mContainerView.removeViewAt(c);
					mAllCustomTiles.remove(c);
				}
			}
		}
		// get rid of the ones we do not need
		removeAll();
		
		// add back existing and new tiles in correct order
		for(QuickTileToken token: newList){
			int position = getOldTileLocation(keeperViews, keeperContents, token);
			if(position > -1){
				QuickSettingsTileView oldTileView = (QuickSettingsTileView)keeperViews.get(position);
				QuickSettingsTileContent pref = (QuickSettingsTileContent)keeperContents.get(position);
				
				oldTileView.setRowSpan(token.getRows());
				oldTileView.setColumnSpan(token.getColumns());
				pref.setDimensions(token.getRows(), token.getColumns());
				mContainerView.addView(oldTileView);
				mAllCustomTiles.add(pref);
			}else{
				QuickSettingsTileContent pref = inflateNewTile(token);
				mAllCustomTiles.add(pref);
				
				if(pref instanceof WifiTile){
					mNetworkController.addNetworkSignalChangedCallback((WifiTile)pref);
				}
				if(pref instanceof SignalTile){
					mNetworkController.addNetworkSignalChangedCallback((SignalTile)pref);
				}
				if(pref instanceof BatteryTile){
					mBatteryController.addStateChangedCallback((BatteryTile)pref);
				}
			}
		}
		
		keeperViews.clear();
		keeperContents.clear();
		updateResources();
	}
	
	private int getOldTileLocation(List<View> keeperViews, 
		List<QuickSettingsTileContent> keeperContents, QuickTileToken token){
		
		for(int v = 0; v < keeperViews.size(); v++){
			if(keeperViews.get(v) instanceof QuickSettingsTileView){
				QuickSettingsTileView tile = (QuickSettingsTileView)keeperViews.get(v);
				final QuickTileToken tag = (QuickTileToken)tile.getTag();
				if(tag.getName().equals(token.getName())){
					return v;
				}
			}
		}
		return -1;
	}
}
