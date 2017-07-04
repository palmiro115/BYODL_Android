package com.byodl.services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.byodl.AppConstants;
import com.byodl.BYODLApp;
import com.byodl.R;
import com.byodl.activities.home.HomeActivity;
import com.byodl.model.Annotations;
import com.byodl.model.api.UploadResponse;
import com.byodl.network.ApiFactory;
import com.byodl.utils.ConnectionHelper;
import com.byodl.utils.ModelHelper;

import org.tensorflow.contrib.android.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncDataService extends Service {
    private static final String CLASSNAME = SyncDataService.class.getName();
    private static final String ACTION_SYNC = CLASSNAME + ".SYNC";
    private static final String TAG = SyncDataService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_LABEL_ID = 2;
    private boolean isSyncing = false;
    private int failedAttemptCount = 0;
    private int syncedAnnotationsCount = 0;
    private boolean isWaiting = false;
    private String lastErrorMessage;

    public static void startSync(Context context) {
        if (context == null)
            return;
        Intent intent = new Intent(context, SyncDataService.class);
        intent.setAction(ACTION_SYNC);
        context.startService(intent);
    }

    public SyncDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;
        if (intent != null)
            action = intent.getAction();
        if (ACTION_SYNC.equals(action))
            handleSync();
        if (isSyncing)
            return START_STICKY;
        else
            return START_NOT_STICKY;
    }

    private boolean checkStop() {
        if (!isSyncing && !isWaiting) {
            stopSelf();
            return true;
        } else {
            return false;
        }
    }

    private void handleSync() {
        if (isSyncing)
            return;
        isSyncing = true;
        final Annotations annotation = Annotations.getFirst(((BYODLApp) getApplication()).getDaoSession());
        if (annotation == null) {
            isSyncing = false;
            showAllSyncedNotificationIcon();
            checkStop();
            return;
        }
        if (!ConnectionHelper.isConnected(this)) {
            setupConnectionListener();
            showConnectionFailedNotificationIcon();
            failedAttemptCount = 0;
            isSyncing = false;
            return;
        }
        removeConnectionListener();
        if (failedAttemptCount >= AppConstants.Config.FAIL_ATTEMPTS_COUNT) {
            showSendFailedNotificationIcon();
            failedAttemptCount = 0;
            isSyncing = false;
            checkStop();
            return;
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showPermissionNotificationIcon();
            isSyncing = false;
            checkStop();
            return;
        }

        Log.d(TAG, "handleSync");
        final File f = new File(annotation.getFileName());
        if (!f.exists() || !f.isFile() || annotation.getPredictions().size() == 0) {
            Annotations.delete(((BYODLApp) getApplication()).getDaoSession(), annotation);
            isSyncing = false;
            handleSync();
            return;
        }
        final File cropped = getCroppedResizedBitmap(f);
	    if (cropped==null||!cropped.exists()){
		    Annotations.delete(((BYODLApp) getApplication()).getDaoSession(), annotation);
		    isSyncing = false;
		    handleSync();
		    return;
	    }
        MultipartBody.Part image =
                MultipartBody.Part.createFormData("image", cropped.getName(), RequestBody.create(MediaType.parse("image/*"), cropped));

        String label = annotation.getPredictions().get(0).getLabel();
        showProgressNotificationIcon(Annotations.getAnnotationsCount(((BYODLApp) getApplication()).getDaoSession()));
        Call<UploadResponse> call = ApiFactory.getApiService().uploadImage(label, image);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
	            cropped.delete();
                if (response.isSuccessful()) {
                    syncedAnnotationsCount++;
                    failedAttemptCount = 0;
                    isSyncing = false;
                    lastErrorMessage = null;
                    Annotations.delete(((BYODLApp) getApplication()).getDaoSession(), annotation);
                    f.delete();
                    handleSync();
                } else {
                    if (response.code()==404){
                        failedAttemptCount = 0;
                        Annotations.delete(((BYODLApp) getApplication()).getDaoSession(), annotation);
                        f.delete();
                        showWrongLabelNotificationIcon(annotation);
                    }
                    else {
                        failedAttemptCount++;
                        lastErrorMessage = response.message();
                    }
                    isSyncing = false;
                    handleSync();
                    Log.d(TAG, "onResponse: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Log.d(TAG, "onFailure", t);
	            cropped.delete();
                isSyncing = false;
                failedAttemptCount++;
                lastErrorMessage = t.getMessage();
                handleSync();
            }
        });

    }

	private File getCroppedResizedBitmap(File f)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(f.getAbsolutePath(),options);
		options.inSampleSize = calculateInSampleSize(options);
		options.inJustDecodeBounds = false;
		try {
			Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(),options);
			String cropFileName = saveBitmapToFile("crop_"+f.getName(),bmp);
			return cropFileName!=null?new File(cropFileName):null;
		}
		catch (OutOfMemoryError e){
			e.printStackTrace();
			return null;
		}
		catch (RuntimeException e){
			e.printStackTrace();
			return null;
		}

	}
	private static int calculateInSampleSize(BitmapFactory.Options options){
		int imageWidth = options.outWidth;
		int imageHeight = options.outHeight;
		int inSampleSize = 1;
		ModelHelper.ModelConfig config = ModelHelper.ModelConfig.getInstance();
		if (imageHeight>config.getInputSize()||imageWidth>config.getInputSize()){
			final int halfHeight = imageHeight/2;
			final int halfWidth = imageWidth/2;
			while ((halfWidth/inSampleSize)>=config.getInputSize()&&(halfHeight/inSampleSize)>=config.getInputSize())
				inSampleSize*=2;
		}
		return inSampleSize;
	}
	private String saveBitmapToFile(String fileName, Bitmap bmp)
	{
		FileOutputStream out = null;
		try {
			ModelHelper.ModelConfig config = ModelHelper.ModelConfig.getInstance();
			Matrix m = ImageUtils.getTransformationMatrix(bmp.getWidth(),bmp.getHeight(),config.getInputSize(), config.getInputSize(),0,true);
			Bitmap cbmp = Bitmap.createBitmap(config.getInputSize(), config.getInputSize(), Bitmap.Config.ARGB_8888);

			Canvas c = new Canvas(cbmp);
			c.drawBitmap(bmp,m,null);

			File parent = ModelHelper.getInstance().getImageFolder();
			if (parent!=null)
				fileName = parent.getAbsolutePath()+"/"+fileName;
			out = new FileOutputStream(fileName);
			cbmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
			cbmp.recycle();
			// PNG is a lossless format, the compression factor (100) is ignored
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void showWrongLabelNotificationIcon(Annotations annotation) {
        showWrongLabelNotification(annotation);

    }

    private void showSendFailedNotificationIcon() {
        showNotification(R.drawable.ic_notification_sync_problem,
                getString(R.string.app_name),
                lastErrorMessage != null ? getString(R.string.upload_failed_msg, lastErrorMessage) : getString(R.string.upload_failed),
                false);
    }

    private void removeConnectionListener() {
        if (isWaiting) {
            Log.d(TAG, "removeConnectionListener");
            unregisterReceiver(connectionReceiver);
            isWaiting = false;
        }
    }

    private void showConnectionFailedNotificationIcon() {
        showNotification(R.drawable.ic_notification_sync_problem,
                getString(R.string.app_name),
                getString(R.string.connection_failed),
                false);
    }

    private void showProgressNotificationIcon(long totalAnnotations) {
        String content;
        if (failedAttemptCount > 0) {
            if (totalAnnotations <= 0) {
                content = getString(R.string.syncing_annotation_attempt,failedAttemptCount+1);
            } else {
                content = getString(R.string.syncing_annotations_attempt, totalAnnotations - 1,failedAttemptCount+1);
            }

        } else {
            if (totalAnnotations <= 0) {
                content = getString(R.string.syncing_annotation);
            } else {
                content = getString(R.string.syncing_annotations, totalAnnotations - 1);
            }
        }
        showNotification(R.drawable.ic_notification_sync,
                getString(R.string.app_name),
                content,
                true);

    }

    private void showAllSyncedNotificationIcon() {
        if (syncedAnnotationsCount > 0) {
            showNotification(R.drawable.ic_notification_check,
                    getString(R.string.app_name),
                    getString(R.string.all_annotations_synced, syncedAnnotationsCount),
                    false);
            syncedAnnotationsCount = 0;
        } else {
            hideNotificationIcon();
        }
    }

    private void hideNotificationIcon() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void showPermissionNotificationIcon() {
        showNotification(R.drawable.ic_notification_sync_problem,
                getString(R.string.app_name),
                getString(R.string.need_permissions),
                false);
    }

    private void setupConnectionListener() {
        if (!isWaiting) {
            Log.d(TAG, "setupConnectionListener");
            isWaiting = true;
            registerReceiver(connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void showNotification(@DrawableRes int iconId, String title, String content, boolean isProgress) {
        PendingIntent pIntent = getNotificationPendingItem();
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(iconId)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(!isProgress)
                .setContentIntent(pIntent)
                .setContentText(content)
                .setContentTitle(title);
        if (!isProgress) {
            notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            notificationBuilder.setProgress(0, 0, false);
            notificationBuilder.setOngoing(false);
        } else {
            notificationBuilder.setProgress(0, 0, true);
            notificationBuilder.setOngoing(true);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void showWrongLabelNotification(Annotations annotation) {
        if (annotation.getPredictions().size()==0)
            return;
        PendingIntent pIntent = getNotificationPendingItem();
        Bitmap largeIcon = BitmapFactory.decodeFile(annotation.getFileName());
        String content = getString(R.string.failed_label,annotation.getPredictions().get(0).getLabel());
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_sync_problem)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentText(content)
                .setContentTitle(getString(R.string.failed_sync_annotation));
            notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            notificationBuilder.setProgress(0, 0, false);
            notificationBuilder.setOngoing(false);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(annotation.getFileName(),NOTIFICATION_LABEL_ID, notificationBuilder.build());
    }

    private PendingIntent getNotificationPendingItem() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + ConnectionHelper.isConnected(context));
            if (ConnectionHelper.isConnected(context))
                handleSync();
        }
    };
}
