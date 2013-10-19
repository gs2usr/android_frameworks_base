package com.android.systemui.statusbar.phone.quicktiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import com.android.internal.util.cm.TorchConstants;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsTileContent;

public class TorchTile extends QuickSettingsTileContent implements
		View.OnClickListener {

	private static final String TAG = "QuickSettings.Torch";
	private static final boolean DEBUG = false;

	private boolean mActive = false;

	public TorchTile(Context context, View view) {
		super(context, view);

		init();
	}

	@Override
	protected void init() {
		mContentView.setOnClickListener(this);

		updateGUI();
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(TorchConstants.ACTION_TOGGLE_STATE);
		mContext.sendBroadcast(i);
	}

	private void updateGUI() {
  
		mTextView.setCompoundDrawablesWithIntrinsicBounds(0,
				mActive ? R.drawable.ic_sysbar_torch_on
						: R.drawable.ic_sysbar_torch_off, 0, 0);
		mTextView.setText(mActive ? R.string.quick_settings_torch_on
				: R.string.quick_settings_torch_off);
	}

	@Override
	public void release() {
		mContext.unregisterReceiver(mTorchReceiver);
	}

	private BroadcastReceiver mTorchReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mActive = intent.getIntExtra(
			        TorchConstants.EXTRA_CURRENT_STATE, 0) != 0;
			updateGUI();
		}
	};

	@Override
	public void refreshResources() {
		// TODO Auto-generated method stub

	}

}
