package mdev.master_j.filedownloader_is;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloaderActivity extends Activity {
	private boolean downloading;
	private boolean downloaded;

	private TextView statusTextView;
	private Button actionButton;
	private ProgressBar downloadProgressBar;

	private ProgressStateReceiver progressStateReceiver;
	private boolean progressStateReceiverIsRegistered;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloader);

		progressStateReceiver = new ProgressStateReceiver();

		statusTextView = (TextView) findViewById(R.id.status_textview);

		actionButton = (Button) findViewById(R.id.button_action);

		downloadProgressBar = (ProgressBar) findViewById(R.id.download_progressbar);
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

	private void updateUI() {
		if (!downloading && !downloaded) {
			statusTextView.setText(R.string.status_textview_idle);
			actionButton.setText(R.string.button_action_download);
			actionButton.setEnabled(true);
			downloadProgressBar.setVisibility(View.INVISIBLE);
			return;
		}
		if (downloading && !downloaded) {
			statusTextView.setText(R.string.status_textview_loading);
			actionButton.setText(R.string.button_action_download);
			actionButton.setEnabled(false);
			downloadProgressBar.setVisibility(View.VISIBLE);
			return;
		}
		if (!downloading && downloaded) {
			statusTextView.setText(R.string.status_textview_loaded);
			actionButton.setText(R.string.button_action_open);
			actionButton.setEnabled(true);
			downloadProgressBar.setVisibility(View.INVISIBLE);
			return;
		}
		if (downloading && downloaded) {
			String text = "both downloading and downloaded are true";
			IllegalStateException exception = new IllegalStateException(text);
			Log.d("mj_tag", text, exception);
			throw exception;
		}
	}

	public class ProgressStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
		}

	}
}
