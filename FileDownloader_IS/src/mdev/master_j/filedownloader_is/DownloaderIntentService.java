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
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class DownloaderIntentService extends IntentService {
	static final String ACTION_STATE = "mdev.master_j.filedownloader_is.STATE";

	static final String KEY_DOWNLOADING = "mdev.master_j.filedownloader_is.DOWNLOADING";
	static final String KEY_DOWNLOADED = "mdev.master_j.filedownloader_is.DOWNLOADED";

	static final String KEY_PROGRESS_POS = "mdev.master_j.filedownloader_is.PROGRESS_POS";
	static final String KEY_PROGRESS_MAX = "mdev.master_j.filedownloader_is.PROGRESS_MAX";

	private static final int BUFFER_SIZE_BYTES = 1024 * 100;

	public DownloaderIntentService() {
		super("Downloader Intent Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
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
				sendState(false, false, 0, 0);
				return;
			}
			sendState(true, false, 0, 0);

			String pictureName = getString(R.string.name_local_picture);
			pictureFile = new File(albumDirectory.getAbsolutePath() + "/" + pictureName);
			OutputStream outStream = new FileOutputStream(pictureFile);

			loaded = 0;
			total = conn.getContentLength();
			byte buffer[] = new byte[BUFFER_SIZE_BYTES];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				loaded += bytesRead;
				outStream.write(buffer, 0, bytesRead);

				sendState(true, false, loaded, total);
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
			toastText("downloading error");
			sendState(false, false, 0, 0);
		} else {
			Intent mediaintent = new Intent();
			mediaintent.setAction(Intent.ACTION_MEDIA_MOUNTED);
			mediaintent.setData(Uri.fromFile(pictureFile));
			sendBroadcast(mediaintent);

			sendState(false, true, 0, 0);
		}
	}

	private File getAlbumDirectory() {
		String albumName = getString(R.string.name_local_album);
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
	}

	private void sendState(boolean downloading, boolean downloaded, int max, int pos) {
		Intent intent = new Intent(ACTION_STATE);
		intent.putExtra(KEY_DOWNLOADING, downloading);
		intent.putExtra(KEY_DOWNLOADED, downloaded);
		intent.putExtra(KEY_PROGRESS_MAX, max);
		intent.putExtra(KEY_PROGRESS_POS, pos);
		sendBroadcast(intent);
	}

	private void toastText(String text) {
		Log.d("mj_tag", text);
	}
}
