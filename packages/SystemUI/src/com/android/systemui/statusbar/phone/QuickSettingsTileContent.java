/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.app.ActivityManagerNative;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.settings.ToggleSlider;

/**
 * This class holds the preference views
 */
public abstract class QuickSettingsTileContent {
    protected FrameLayout mContentView;
    protected TileCallback mCallBack;
    protected Context mContext;
    protected LinearLayout mRSSILayout;
    protected ImageView mRSSIImage;
    protected ImageView mRSSIImageOverlay;
    protected TextView mRSSITextView;
    protected ImageView mBatteryImageView;
    protected ImageView mImageView;
    protected TextView mTextView;
    protected ToggleSlider mSlider;
    protected String mTag;
    
    protected int mOriginalRows = 1;
    protected int mOriginalColumns = 1;
    
    public interface TileCallback {
        public void changeSize(int height, int width);
        public void refreshTiles();
        public void show(boolean visible);
    }

    public QuickSettingsTileContent(Context context, View view) {
        mContext = context;
        mTag = "Preference";
        mContentView = (FrameLayout)view;
        mRSSILayout = (LinearLayout)mContentView.findViewById(R.id.rssi);
        setupViews();
    }

    private void setupViews(){
    	mRSSITextView = (TextView)mContentView.findViewById(R.id.rssi_tv);
    	mRSSIImage = (ImageView)mContentView.findViewById(R.id.rssi_image);
    	mRSSIImageOverlay = (ImageView)mContentView.findViewById(R.id.rssi_overlay_image);
    	mBatteryImageView = (ImageView)mContentView.findViewById(R.id.quick_settings_battery_image);
        mImageView = (ImageView)mContentView.findViewById(R.id.quick_settings_iv);
        mTextView = (TextView)mContentView.findViewById(R.id.quick_settings_tv);
        mSlider = (ToggleSlider)mContentView.findViewById(R.id.tile_slider);
    }
    
    protected abstract void init();
    public abstract void release();
    public abstract void refreshResources();
    
    public StatusBarManager getStatusBarManager() {
        return (StatusBarManager)mContext.getSystemService(Context.STATUS_BAR_SERVICE);
    }
    
    public String getTag(){
        return mTag;
    }
    
    public void setDimensions(int height, int width){
    	mOriginalRows = height;
    	mOriginalColumns = width;
    }
    
    protected void launchActivity(Intent intent) throws ActivityNotFoundException {
    	// We take this as a good indicator that Setup is running and we shouldn't
        // allow you to go somewhere else
        if (!isDeviceProvisioned()) return;
    	try {
			// The intent we are sending is for the application, which
			// won't have permission to immediately start an activity after
			// the user switches to home. We know it is safe to do at this
			// point, so make sure new activity switches are now allowed.
			ActivityManagerNative.getDefault().resumeAppSwitches();
			// Also, notifications can be launched from the lock screen,
			// so dismiss the lock screen when the activity starts.
			ActivityManagerNative.getDefault()
					.dismissKeyguardOnNextActivity();
		} catch (RemoteException e) {
		}

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
		
		getStatusBarManager().collapsePanels();
    }
    
    private boolean isDeviceProvisioned(){
    	final boolean provisioned = 0 != Settings.Global.getInt(
                mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
    	return provisioned;
    }
    
    public static class State {
    	public int iconId;
    	public String label;
    	public boolean enabled = false;
    }
    
}