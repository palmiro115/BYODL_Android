package com.byodl.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.byodl.AppConstants;
import com.byodl.model.DaoSession;
import com.byodl.model.Labels;

import org.tensorflow.contrib.android.Classifier;
import org.tensorflow.contrib.android.TensorFlowImageClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ModelHelper {
    private static final String PREF_MODEL_VERSION = "pref.model.version";
    private static final String TAG = ModelHelper.class.getSimpleName();
	private static final String PREF_UPDATE_DATE = "pref.model.update.date";
	private static ModelHelper sInstance;
    private InitTask task;
	private List<String> labels;
    private Classifier classifier = null;
    private int numInits = 0;


	public static class ModelConfig{
		private static ModelConfig sInstance = new ModelConfig();
		private static final int INPUT_SIZE = 299;
		private static final int IMAGE_MEAN = 128;
		private static final float IMAGE_STD = 128;
		private static final String INPUT_NAME = "batch_processing/Reshape";
		private static final String OUTPUT_NAME = "inception_v3/logits/predictions";

		public int getInputSize()
		{
			return INPUT_SIZE;
		}

		public int getImageMean()
		{
			return IMAGE_MEAN;
		}

		public float getImageStd()
		{
			return IMAGE_STD;
		}

		public String getInputName()
		{
			return INPUT_NAME;
		}

		public String getOutputName()
		{
			return OUTPUT_NAME;
		}

		public static ModelConfig getInstance(){
			return sInstance;
		}

	}

    public List<String> getLabels()
	{
		return labels;
	}

    public interface OnInitializedListener{
        void onInitialized(boolean success);
    }
    private WeakReference<Context> contextRef;
    private SharedPreferences settings;
    private WeakReference<DaoSession> sessionRef;
    public static ModelHelper init(Context context,DaoSession session){
        if (sInstance == null)
            sInstance = new ModelHelper(context,session);
        return sInstance;
    }
    public static ModelHelper getInstance(){
        return sInstance;
    }
    private ModelHelper(Context context,DaoSession session){
        contextRef = new WeakReference<>(context.getApplicationContext());
        sessionRef = new WeakReference<>(session);
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
    }
    private boolean isInitialized(){
        File model = getModelFile();
        if (model==null)
            return false;
        loadLabels();
        return model.exists()&&model.isFile()&&getLabels()!=null&&getLabels().size()>0;
    }

    public File getModelFile() {
        Context context = getContext();
        if (context==null)
            return null;
        return new File(context.getFilesDir(), AppConstants.Model.FILENAME);
    }
    private File getTempModelFile() {
        Context context = getContext();
        if (context==null)
            return null;
        return new File(context.getFilesDir(), "tmp_"+AppConstants.Model.FILENAME);
    }

    public boolean initialize(OnInitializedListener listener) {
        if (isInitialized() && isVersionSaved()) {
	        loadLabels();
            if (listener != null)
                listener.onInitialized(true);
            return true;
        }
        if (task == null) {
            task = new InitTask(listener);
            task.execute();
        } else {
            task.setListener(listener);
        }
        return false;
    }

	private void loadLabels()
	{
		List<Labels> l = Labels.getLabels(getSession());
		if (l!=null){
			labels = new ArrayList<>();
			for (Labels label:l){
				labels.add(label.getLabel());
			}
		}
	}

	private boolean isVersionSaved() {
        return settings.getString(PREF_MODEL_VERSION,null)!=null;
    }

    public Context getContext(){
        return contextRef.get();
    }
    private class InitTask extends AsyncTask<Void,Void,Boolean>{
        private OnInitializedListener listener;

        public InitTask(OnInitializedListener listener){
            this.listener = listener;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean ret = copyAssetFile(AppConstants.Model.FILENAME);
            if (ret) {
                List<String> labels = getAssetsLabels();
                saveLabels(labels);
                String version = getAssetsModelVersion();
                settings.edit().putString(PREF_MODEL_VERSION,version).apply();
	            settings.edit().putLong(PREF_UPDATE_DATE,System.currentTimeMillis()).apply();
            }
            return ret;
        }

        private String getAssetsModelVersion() {
            List<String> file = loadAssetFile(AppConstants.Model.MODEL_VERSION_FILENAME);
            if (file!=null&&file.size()>0)
                return file.get(0);
            return null;
        }

        private List<String> getAssetsLabels() {
            return loadAssetFile(AppConstants.Model.LABELS_FILE_NAME);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            boolean ret = isInitialized();
            if (listener!=null){
                listener.onInitialized(ret);
            }
        }

        public void setListener(OnInitializedListener listener) {
            this.listener = listener;
        }
    }

    private void saveLabels(List<String> labels) {
        Labels.update(getSession(),labels);
	    if (labels!=null) {
		    this.labels = new ArrayList<>();
		    this.labels.addAll(labels);
	    }
    }
    private List<String> loadAssetFile(String fileName){
        Context context = getContext();
        if (context==null)
            return null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
            String label;
            List<String> result = new ArrayList<>();
            while ((label = reader.readLine())!=null){
                result.add(label.trim());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (reader!=null)
                try{
                    reader.close();
                }
                catch (IOException ignore){}
        }

    }
    private boolean copyAssetFile(String fileName) {
        Context context = getContext();
        if (context==null)
            return false;
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        String tmpFileName = fileName+"_tmp";
        try {
            in = assetManager.open(fileName);

            File outFile = new File(context.getFilesDir(), tmpFileName);
            if (outFile.exists())
                outFile.delete();

            out = new FileOutputStream(outFile);
            copyFile(in, out);
            File file = new File(context.getFilesDir(), fileName);
            if (file.exists())
                file.delete();
            outFile.renameTo(file);
            return true;
        } catch(IOException e) {
            Log.e(TAG, "Failed to copy assets file", e);
        }
        finally {
            if (in!=null)
                try{
                    in.close();
                }
                catch (IOException ignore){};
            if (out!=null)
                try{
                    out.flush();
                    out.close();
                }
                catch (IOException ignore){}
        }
        return false;
    }
    public String getVersion(){
        return settings.getString(PREF_MODEL_VERSION,null);
    }
    public long getLastUpdateDate(){
	    return settings.getLong(PREF_UPDATE_DATE,System.currentTimeMillis());
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    private DaoSession getSession(){
        return sessionRef.get();
    }
    public File getDownloadModelFile() {
        Context context = getContext();
        if (context==null)
            return null;
        return new File(context.getFilesDir(), "dwl_"+AppConstants.Model.FILENAME);
    }
    public void updateDownloadedModel(String version) {
        int openedConnections = numInits;
        closeClassifier();

        File f = getModelFile();
        File downloaded = getDownloadModelFile();
        if (downloaded.exists()){
            if (f.exists())
                f.delete();
            downloaded.renameTo(f);
            settings.edit().putString(PREF_MODEL_VERSION,version).apply();
            settings.edit().putLong(PREF_UPDATE_DATE,System.currentTimeMillis()).apply();

        }
        if (numInits!=0){
            initClassifier();
            numInits = openedConnections;
        }
    }
    public void initClassifier(){
        if (classifier==null&&getContext()!=null) {
            numInits = 1;
            int numClasses = ModelHelper.getInstance().getLabels() != null ? ModelHelper.getInstance().getLabels().size() : 0;

            try {
	            ModelConfig config = ModelConfig.getInstance();
                classifier = TensorFlowImageClassifier.create(getContext().getAssets(), numClasses,
		                config.getInputSize(), config.getImageMean(), config.getImageStd(),
		                config.getInputName(),config.getOutputName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            numInits++;
        }
    }
    public Classifier getClassifier(){
        return classifier;
    }
    public void closeClassifier() {
        if (classifier != null) {
            numInits--;
            if (numInits <= 0 && classifier != null) {
                classifier.close();
                classifier = null;
            }
        }
    }
	public boolean checkAndUpdate(List<String> newLabels){
		if (newLabels==null)
			return false;
		List<String> oldLabels = getLabels();

		boolean needUpdate = false;
		if (oldLabels!=null){
			List<String> mutableNewLabels = new ArrayList<>(newLabels);
			for (String label:oldLabels){
				if (!mutableNewLabels.remove(label)){
					needUpdate = true;
					break;
				}
			}
			needUpdate|=mutableNewLabels.size()>0;
		}
		else{
			needUpdate = true;
		}
		if (needUpdate) {
			saveLabels(newLabels);
			return true;
		}
		return false;
	}
	public File getImageFolder(){
		File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		if (!root.exists()) {
			if (!root.mkdir()) {
				return null;
			}
		}
		File appDir = new File(root,"BYODL");
		if (!appDir.exists()){
			if (!appDir.mkdir()) {
				return null;
			}
		}
		return appDir;
	}

}
