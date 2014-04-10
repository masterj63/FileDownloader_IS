package mdev.master_j.filedownloader_is;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DownloaderIntentService extends IntentService {
	static final String ACTION_STATE = "mdev.master_j.filedownloader_is.DownloaderIntentService.ACTION_STATE";
	static final String ACTION_TOAST = "mdev.master_j.filedownloader_is.DownloaderIntentService.ACTION_TOAST";

	static final String EXTRA_TOAST = "mdev.master_j.filedownloader_is.DownloaderIntentService.EXTRA_TOAST";

	static final String EXTRA_DOWNLOAD_STATE = "mdev.master_j.filedownloader_is.DownloaderIntentService.EXTRA_DOWNLOAD_STATE";

	static final String EXTRA_PROGRESS_POS = "mdev.master_j.filedownloader_is.DownloaderIntentService.EXTRA_PROGRESS_POS";
	static final String EXTRA_PROGRESS_MAX = "mdev.master_j.filedownloader_is.DownloaderIntentService.EXTRA_PROGRESS_MAX";

	enum DownloadState {
		IDLE, IN_PROGRESS, DONE
	}

	private static final int BUFFER_SIZE_BYTES = 1024 * 100;
	private static final int NOTIFICATION_ID = 20;
	private static final int NOTIFICATION_UPDATE_PERIOD = 80;

	public DownloaderIntentService() {
		super("Downloader Intent Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final String DOWNLOADING_LABEL = getString(R.string.label_downloading);
		final String FAILURE_LABEL = getString(R.string.label_download_failure);
		final String SUCCESS_LABEL = getString(R.string.label_download_success);

		int loaded = 0;
		int total = 0;
		String pictureUrl = getString(R.string.url_picture);
		File pictureFile = null;
		try {
			URL url = new URL(pictureUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// init
			conn.setReadTimeout(5000);
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// start
			conn.connect();
			int response = conn.getResponseCode();
			Log.d("mj_tag", "The response is: " + response);
			// save
			InputStream inStream = conn.getInputStream();

			File albumDirectory = getAlbumDirectory();
			if (!albumDirectory.mkdirs() && !albumDirectory.exists()) {
				toastText("Cannot access " + albumDirectory.getAbsolutePath());
				sendState(DownloadState.IDLE, 0, 0);
				return;
			}
			// TODO do i really need the following line?
			sendState(DownloadState.IN_PROGRESS, 0, 0);

			String pictureName = getString(R.string.name_local_picture);
			pictureFile = new File(albumDirectory.getAbsolutePath() + "/" + pictureName);
			OutputStream outStream = new FileOutputStream(pictureFile);

			loaded = 0;
			total = conn.getContentLength();
			byte buffer[] = new byte[BUFFER_SIZE_BYTES];
			int bytesRead;
			int counter = NOTIFICATION_UPDATE_PERIOD;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				loaded += bytesRead;
				outStream.write(buffer, 0, bytesRead);

				sendState(DownloadState.IN_PROGRESS, total, loaded);

				if (counter % NOTIFICATION_UPDATE_PERIOD == 0)
					showNotification(loaded, total, true, DOWNLOADING_LABEL);
				counter++;
			}
			outStream.flush();
			outStream.close();
		} catch (MalformedURLException e) {
			Log.d("mj_tag", "MalformedURLException", e);
			e.printStackTrace();
		} catch (ProtocolException e) {
			Log.d("mj_tag", "ProtocolException", e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("mj_tag", "ProtocolException", e);
			e.printStackTrace();
		}

		if (loaded != total) {
			showNotification(loaded, total, false, FAILURE_LABEL);

			toastText("downloading error");
			sendState(DownloadState.IDLE, 0, 0);
		} else {
			showNotification(loaded, total, false, SUCCESS_LABEL);

			scanMedia(pictureFile);

			sendState(DownloadState.DONE, 0, 0);
		}
	}

	private void showNotification(int loaded, int total, boolean ongoing, String title) {
		int percents = (int) Math.ceil(100d * loaded / total);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentText(percents + " %")
				.setContentTitle(title).setSmallIcon(R.drawable.ic_launcher).setOngoing(ongoing);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// TODO set notification action depending on ongoing value
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	private void scanMedia(File file) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(Uri.fromFile(file));
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private File getAlbumDirectory() {
		String albumName = getString(R.string.name_local_album);
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
	}

	private void sendState(DownloadState downloadState, int max, int pos) {
		Intent intent = new Intent(ACTION_STATE);
		intent.putExtra(EXTRA_DOWNLOAD_STATE, downloadState);
		intent.putExtra(EXTRA_PROGRESS_MAX, max);
		intent.putExtra(EXTRA_PROGRESS_POS, pos);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void toastText(String text) {
		Log.d("mj_tag", text);
		Intent intent = new Intent(ACTION_TOAST);
		intent.putExtra(EXTRA_TOAST, text);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
