package mdev.master_j.filedownloader_is;

import android.app.IntentService;
import android.content.Intent;

public class DownloaderIntentService extends IntentService {
	static final String ACTION_STATE = "mdev.master_j.filedownloader_is.STATE";

	static final String KEY_DOWNLOADING = "mdev.master_j.filedownloader_is.DOWNLOADING";
	static final String KEY_DOWNLOADED = "mdev.master_j.filedownloader_is.DOWNLOADED";

	static final String KEY_PROGRESS_POS = "mdev.master_j.filedownloader_is.PROGRESS_POS";
	static final String KEY_PROGRESS_MAX = "mdev.master_j.filedownloader_is.PROGRESS_MAX";

	public DownloaderIntentService() {
		super("Downloader Intent Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO do i need to implement this?
	}
}
