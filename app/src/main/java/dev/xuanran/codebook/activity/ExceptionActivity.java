package dev.xuanran.codebook.activity;

import static dev.xuanran.codebook.util.DateUtils.getNowTime;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import dev.xuanran.codebook.R;

public class ExceptionActivity extends Activity {

	private String log;
	private String savePath;
	private boolean isSave = true;

	private static final int REQUEST_CODE_SAVE_LOG = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_DeviceDefault);
		setTitle(R.string.program_exception);

		Intent intent = getIntent();

		ScrollView scrollView = new ScrollView(this);
		HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
		TextView messageTextView = new TextView(this);

		horizontalScrollView.addView(messageTextView);
		scrollView.addView(horizontalScrollView, -1, -1);
		setContentView(scrollView);

		log = intent.getStringExtra(Intent.EXTRA_TEXT);
		messageTextView.setText(log);
		messageTextView.setTextIsSelectable(true);

		int padding = dp2px(16);
		messageTextView.setPadding(padding, padding, padding, padding);

		scrollView.setFillViewport(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(R.string.save_log)
				.setOnMenuItemClickListener(this::onSaveLogClick)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(R.string.restart)
				.setOnMenuItemClickListener(this::onRestartClick)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	private boolean onSaveLogClick(MenuItem item) {
		if (isSave) {
			openFileChooser();
			return false;
		}
		Toast.makeText(this, R.string.log_saved, Toast.LENGTH_LONG).show();
		return false;
	}

	private void openFileChooser() {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TITLE, "error_log_" + getNowTime() + ".txt");
		startActivityForResult(intent, REQUEST_CODE_SAVE_LOG);
	}


	private boolean onRestartClick(MenuItem item) {
		startActivity(new Intent(this, MainActivity.class));
		finish();
		return false;
	}

	private void showSaveLogDialog(String savedPath) {
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.save_log)
				.setMessage(getString(R.string.log_saved_path) + savedPath)
				.setCancelable(false)
				.setNegativeButton(R.string.delete, (dialog, which) -> {
					deleteAllFiles(new File(savePath));
					Toast.makeText(this, R.string.log_deleted, Toast.LENGTH_LONG).show();
				})
				.setPositiveButton(R.string.cancel, null)
				.show();
	}

	public static int dp2px(float dpValue) {
		float scale = Resources.getSystem().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_SAVE_LOG && resultCode == RESULT_OK) {
			if (data != null) {
				Uri uri = data.getData();
				saveLogToFile(uri);
				isSave = false;
			}
		}
	}

	private void saveLogToFile(Uri uri) {
		try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
			if (outputStream != null) {
				outputStream.write(log.getBytes());
				Toast.makeText(this, R.string.log_saved, Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
		}
	}


	public static String saveErrorMessage(String message, String path) {
		String filepath = path + getNowTime() + ".log";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try (FileWriter fw = new FileWriter(filepath)) {
			fw.write(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filepath;
	}

	static void deleteAllFiles(File root) {
		File[] files = root.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteAllFiles(file);
				}
				file.delete();
			}
		}
	}
}
