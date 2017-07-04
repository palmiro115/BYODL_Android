package com.byodl.activities.home.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.byodl.BYODLApp;
import com.byodl.R;
import com.byodl.model.Predictions;
import com.byodl.utils.ModelHelper;

import org.tensorflow.contrib.android.Classifier;
import org.tensorflow.contrib.android.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.OnClick;

public class PredictFragment extends CameraFragment {
    private static final String TAG = PredictFragment.class.getSimpleName();
	private static final int REQUEST_DELETE_IMAGES = 1;
	private boolean needRefreshOnResume;

	@OnClick(R.id.preview)
    void onPreviewClick(){
		if (getChildFragmentManager().findFragmentByTag("HISTORY")==null) {
			PredictHistoryFragment f = new PredictHistoryFragment();
			f.setTargetFragment(this,REQUEST_DELETE_IMAGES);
			f.show(getChildFragmentManager(),"HISTORY");
		}

    }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ModelHelper.getInstance().initClassifier();
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		needRefreshOnResume = false;
	}

	@Override
    protected void fileSaved(final File file) {
	    if (ModelHelper.getInstance().getClassifier()!=null) {
			final Classifier classifier = ModelHelper.getInstance().getClassifier();
		    showDialog(R.string.predicting);
		    getBackgroundHandler().post(new Runnable() {
			    @Override
			    public void run()
			    {
				    Bitmap bmp = loadBitmap(file);
				    ModelHelper.ModelConfig config = ModelHelper.ModelConfig.getInstance();
				    Matrix m = ImageUtils.getTransformationMatrix(bmp.getWidth(),bmp.getHeight(),config.getInputSize(), config.getInputSize(),0,true);
				    Bitmap cbmp = Bitmap.createBitmap(config.getInputSize(), config.getInputSize(), Bitmap.Config.ARGB_8888);

				    Canvas c = new Canvas(cbmp);
				    c.drawBitmap(bmp,m,null);
				    final List<Classifier.Recognition> recognitions = classifier.recognizeImage(cbmp);
				    cbmp.recycle();
				    getForegroundHandler().post(new Runnable() {
					    @Override
					    public void run()
					    {
						    hideDialog();
						    bitmapRecognited(file,recognitions);
					    }


				    });
			    }
		    });
	    }
    }

	@Override
	public void onDestroy() {
		ModelHelper.getInstance().closeClassifier();
		super.onDestroy();
	}

	private void saveCroppedBitmap(Bitmap cbmp)
	{
		saveBitmapToFile("crop_"+System.currentTimeMillis()+".jpg",cbmp);
	}

	private void saveBitmapToFile(String fileName, Bitmap bmp)
	{
		FileOutputStream out = null;
		try {
			File parent = ModelHelper.getInstance().getImageFolder();
			if (parent!=null)
				fileName = parent.getAbsolutePath()+"/"+fileName;
			out = new FileOutputStream(fileName);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
			// PNG is a lossless format, the compression factor (100) is ignored
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
	}

	private void bitmapRecognited(File file, List<Classifier.Recognition> recognitions)
	{
		updateLastImage(file);
		saveRecognitedImage(file,recognitions);
		PredictResultFragment f = PredictResultFragment.newInstance(file.getAbsolutePath(),recognitions);
		f.show(getChildFragmentManager(),"PREDICTION");
	}

	private void saveRecognitedImage(File file, List<Classifier.Recognition> recognitions) {
		if (getActivity()!=null) {
			Predictions.fromPredictions(((BYODLApp) getActivity().getApplication()).getDaoSession(),file,recognitions);
		}
	}

	@Override
	protected File getLastSavedImage() {
		if (getActivity()!=null) {
			Predictions p = Predictions.getLastPredictions(((BYODLApp) getActivity().getApplication()).getDaoSession());
			if (p!=null)
				return new File(p.getFileName());
		}
		return null;
	}

	private Bitmap loadBitmap(File file)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==REQUEST_DELETE_IMAGES&&resultCode== Activity.RESULT_OK)
			needRefreshOnResume = true;
		else
			super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (needRefreshOnResume){
			updateLastImage(getLastSavedImage());
			needRefreshOnResume = false;
		}
	}
}
