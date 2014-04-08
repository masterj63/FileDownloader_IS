package mdev.master_j.filedownloader_is;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class DownloaderActivity extends Activity {
	private boolean downloading;
	private boolean downloaded;

	private ProgressStateReceiver progressStateReceiver;
	private boolean progressStateReceiverIsRegistered;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		progressStateReceiver = new ProgressStateReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!progressStateReceiverIsRegistered) {
			progressStateReceiverIsRegistered = true;
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("progress");
			intentFilter.addAction("state");
			registerReceiver(progressStateReceiver, intentFilter);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (progressStateReceiverIsRegistered) {
			progressStateReceiverIsRegistered = false;
			unregisterReceiver(progressStateReceiver);
		}
	}

	public class ProgressStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
		}

	}
}
