package mdev.master_j.filedownloader_is;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloaderActivity extends Activity {
	private boolean downloading;
	private boolean downloaded;
	private int progressPos;
	private int progressMax;

	private TextView statusTextView;
	private Button actionButton;
	private ProgressBar downloadProgressBar;

	private ProgressStateReceiver progressStateReceiver;
	private boolean progressStateReceiverIsRegistered;

	private final OnClickListener ACTION_BUTTON_ON_CLICK_LISTENER = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (downloaded)
				showPicture();
			else {
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
				if (netInfo == null || !netInfo.isConnected()) {
					Log.d("mj_tag", "No internet connection");
					Toast.makeText(DownloaderActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
					downloading = false;
					downloaded = false;
					updateUI();
					return;
				}
				Intent intent = new Intent(DownloaderActivity.this, DownloaderIntentService.class);
				startService(intent);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloader);

		if (savedInstanceState != null) {
			downloading = savedInstanceState.getBoolean(DownloaderIntentService.KEY_DOWNLOADING);
			downloaded = savedInstanceState.getBoolean(DownloaderIntentService.KEY_DOWNLOADED);
			progressMax = savedInstanceState.getInt(DownloaderIntentService.KEY_PROGRESS_MAX);
			progressPos = savedInstanceState.getInt(DownloaderIntentService.KEY_PROGRESS_POS);
		}

		progressStateReceiver = new ProgressStateReceiver();

		statusTextView = (TextView) findViewById(R.id.status_textview);

		actionButton = (Button) findViewById(R.id.button_action);
		actionButton.setOnClickListener(ACTION_BUTTON_ON_CLICK_LISTENER);

		downloadProgressBar = (ProgressBar) findViewById(R.id.download_progressbar);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!progressStateReceiverIsRegistered) {
			progressStateReceiverIsRegistered = true;
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(DownloaderIntentService.ACTION_STATE);
			registerReceiver(progressStateReceiver, intentFilter);
		}

		updateUI();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (progressStateReceiverIsRegistered) {
			progressStateReceiverIsRegistered = false;
			unregisterReceiver(progressStateReceiver);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(DownloaderIntentService.KEY_DOWNLOADING, downloading);
		outState.putBoolean(DownloaderIntentService.KEY_DOWNLOADED, downloaded);
		outState.putInt(DownloaderIntentService.KEY_PROGRESS_MAX, progressMax);
		outState.putInt(DownloaderIntentService.KEY_PROGRESS_POS, progressPos);
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
			downloadProgressBar.setMax(progressMax);
			downloadProgressBar.setProgress(progressPos);
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

	private void showPicture() {
		File albumDirectory = getAlbumDirectory();
		if (!albumDirectory.canRead()) {
			Log.d("mj_tag", "Can't read from " + albumDirectory.getAbsolutePath());
			Toast.makeText(this, "Can't read from " + albumDirectory.getAbsolutePath(), Toast.LENGTH_SHORT).show();
			downloading = false;
			downloaded = false;
			updateUI();
			return;
		}

		String picureName = getString(R.string.name_local_picture);

		File pictureFile = new File(albumDirectory.getAbsolutePath() + "/" + picureName);
		if (!pictureFile.exists()) {
			Log.d("mj_tag", "Can't find picture at " + pictureFile.getAbsolutePath());
			Toast.makeText(this, "Can't find picture at " + pictureFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
			downloading = false;
			downloaded = false;
			updateUI();
			return;
		}

		Uri uri = Uri.fromFile(pictureFile);

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "image/*");
		startActivity(intent);
	}

	private File getAlbumDirectory() {
		String albumName = getString(R.string.name_local_album);
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
	}

	public class ProgressStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(DownloaderIntentService.ACTION_STATE)) {
				downloading = intent.getBooleanExtra(DownloaderIntentService.KEY_DOWNLOADING, false);
				downloaded = intent.getBooleanExtra(DownloaderIntentService.KEY_DOWNLOADED, false);
				progressMax = intent.getIntExtra(DownloaderIntentService.KEY_PROGRESS_MAX, 0);
				progressPos = intent.getIntExtra(DownloaderIntentService.KEY_PROGRESS_POS, 0);
				updateUI();
				return;
			}
		}

	}
}
