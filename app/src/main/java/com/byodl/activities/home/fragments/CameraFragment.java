package com.byodl.activities.home.fragments;


import android.Manifest;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.byodl.R;
import com.byodl.utils.ModelHelper;
import com.byodl.utils.NotificationHelper;
import com.byodl.utils.SquareCropTransformation;
import com.google.android.cameraview.CameraView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends BaseFragment {

    private static final String TAG = CameraFragment.class.getSimpleName();
    private static final String ARG_LAST_IMAGE = "arg.last.image";
    @BindView(R.id.controlFlash)
    ImageButton controlFlash;
    @BindView(R.id.controlSwitchCamera)
    ImageButton controlSwitchCamera;
    @BindView(R.id.preview)
    ImageButton preview;
    @BindView(R.id.takePhoto)
    ImageButton takePhoto;
    @BindView(R.id.camera)
    CameraView cameraView;
    private Handler backgroundHandler;
    private Handler foregroundHandler = new Handler();

    public CameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        cameraView.addCallback(callback);
        updateLastImage(getLastSavedImage());
        setButtonsEnable(false);
    }

    private CameraView.Callback callback = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
            setButtonsEnable(true);
            updateFlashControl();
            updateCameraFacing();
            cameraView.setAutoFocus(true);
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
            setButtonsEnable(false);
        }

        @Override
        public void onPictureTaken(CameraView cameraView, byte[] data) {
            super.onPictureTaken(cameraView, data);
            playCameraSound();
            trySaveImage(data);
        }
    };

    private void trySaveImage(final byte[] data) {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        saveImage(data);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        NotificationHelper.toast(getContext(),R.string.image_not_saved_permissions);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, final PermissionToken token) {
                        NotificationHelper.ask(getContext(),
                                R.string.permission_required,
                                R.string.permission_required_write,
                                R.string.cancel,
                                R.string.proceed, new NotificationHelper.OnButtonClick() {
                                    @Override
                                    public void onButtonClick(@StringRes int id) {
                                        if (id==R.string.cancel)
                                            token.cancelPermissionRequest();
                                        else if (id==R.string.proceed)
                                            token.continuePermissionRequest();
                                    }
                                });
                    }
                })
                .check();
    }
    private void saveImage(final byte[] data) {
        showDialog(R.string.saving_file);
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
	            File appDir = ModelHelper.getInstance().getImageFolder();
	            if (appDir==null){
		            hideDialog();
		            return;
	            }
                final File file = new File(appDir,"byodl_"+System.currentTimeMillis()+".jpg");
                OutputStream os = null;
                boolean isSavedSuccess = false;
                try {
                    os = new FileOutputStream(file);
                    os.write(data);
                    os.close();
                    isSavedSuccess = true;
                } catch (IOException e) {
                    Log.w(TAG, "Cannot write to " + file, e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }
	            hideDialog();
                if (isSavedSuccess) {
                    getForegroundHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            fileSaved(file);
                        }
                    });
                }
            }
        });
    }
	protected Handler getForegroundHandler(){
		return foregroundHandler;
	}
    protected void fileSaved(File file) {
        Log.d(TAG, "fileSaved: "+file.getAbsolutePath());
    }


    protected Handler getBackgroundHandler() {
        if (backgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            backgroundHandler = new Handler(thread.getLooper());
        }
        return backgroundHandler;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (backgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                backgroundHandler.getLooper().quitSafely();
            } else {
                backgroundHandler.getLooper().quit();
            }
            backgroundHandler = null;
        }
        hideDialog();
    }
    private void playCameraSound() {
        MediaActionSound sound = new MediaActionSound();
        sound.play(MediaActionSound.SHUTTER_CLICK);
    }

    private void updateCameraFacing() {
        switch (cameraView.getFacing()){
            case CameraView.FACING_FRONT:
                controlSwitchCamera.setImageResource(R.drawable.ic_camera_front_white_24dp);
                break;
            case CameraView.FACING_BACK:
                controlSwitchCamera.setImageResource(R.drawable.ic_camera_rear_white_24dp);
                break;
        }
    }

    private void updateFlashControl() {
        switch (cameraView.getFlash()){
            case CameraView.FLASH_AUTO:
                controlFlash.setImageResource(R.drawable.ic_flash_auto);
                break;
            case CameraView.FLASH_OFF:
                controlFlash.setImageResource(R.drawable.ic_flash_off);
                break;
            case CameraView.FLASH_ON:
                controlFlash.setImageResource(R.drawable.ic_flash_on);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    private void startCamera() {
        setButtonsEnable(false);
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        cameraView.start();
                        setButtonsEnable(true);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        NotificationHelper.toast(getContext(),R.string.cant_use_camera_without_permission);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, final PermissionToken token) {
                        NotificationHelper.ask(getContext(),
                                R.string.permission_required,
                                R.string.permission_required_camera,
                                R.string.cancel,
                                R.string.proceed, new NotificationHelper.OnButtonClick() {
                                    @Override
                                    public void onButtonClick(@StringRes int id) {
                                        if (id==R.string.cancel)
                                            token.cancelPermissionRequest();
                                        else if (id==R.string.proceed)
                                            token.continuePermissionRequest();
                                    }
                                });
                    }
                })
                .check();
    }

    protected void updateLastImage(File f) {
        if (f==null)
	        preview.setVisibility(View.GONE);
	    else {
	        Uri uri = Uri.fromFile(f);
	        Picasso
			        .with(getContext())
			        .load(uri)
			        .transform(new SquareCropTransformation())
			        .into(preview);

	        preview.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonsEnable(boolean enable){
        controlFlash.setEnabled(enable);
        controlSwitchCamera.setEnabled(enable);
        takePhoto.setEnabled(enable);
    }
    @OnClick(R.id.takePhoto)
    void onTakePhotoClick(){
        cameraView.takePicture();
    }
    @OnClick(R.id.controlFlash)
    void onControlFlashClick(){
        int flash = cameraView.getFlash();
        flash = (flash+1)%3;
        cameraView.setFlash(flash);
        updateFlashControl();
    }
    @OnClick(R.id.controlSwitchCamera)
    void onSwitchCameraClick(){
        int facing = cameraView.getFacing();
        if (facing==CameraView.FACING_BACK)
            facing = CameraView.FACING_FRONT;
        else
            facing = CameraView.FACING_BACK;
        cameraView.setFacing(facing);
        updateCameraFacing();
    }


    protected File getLastSavedImage(){
        return null;
    }
}
