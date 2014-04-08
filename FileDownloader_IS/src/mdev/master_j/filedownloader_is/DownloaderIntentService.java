package mdev.master_j.filedownloader_is;

import android.app.IntentService;
import android.content.Intent;

public class DownloaderIntentService extends IntentService {
	static final String ACTION_PROGRESS = "mdev.master_j.filedownloader_is.PROGRESS";
	static final String ACTION_STATE = "mdev.master_j.filedownloader_is.STATE";

	public DownloaderIntentService(String name) {

		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO do i need to implement this?
	}
}
