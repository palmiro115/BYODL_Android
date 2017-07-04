package com.byodl.activities.home.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.byodl.AppConstants;
import com.byodl.R;
import com.byodl.model.api.LabelsResponse;
import com.byodl.network.ApiFactory;
import com.byodl.network.ProgressResponseBody;
import com.byodl.utils.ModelHelper;
import com.byodl.utils.NotificationHelper;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Response;

public class UpdateProgressFragment extends DialogFragment
{
	private static final String ARG_VERSION = "version";
	public static final String EXTRA_SUCCESS = "is.success";
	public static final String EXTRA_MESSAGE = "message";
	private String version;

	@BindView(R.id.progress)
	ProgressBar progress;
	private AsyncTask<Void, Integer, String> updateTask;
	private AlertDialog askDialog;

	public UpdateProgressFragment()
	{
		// Required empty public constructor
	}

	public static UpdateProgressFragment newInstance(@NonNull String version)
	{
		UpdateProgressFragment fragment = new UpdateProgressFragment();
		fragment.setCancelable(false);
		Bundle args = new Bundle();
		args.putString(ARG_VERSION, version);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			version = getArguments().getString(ARG_VERSION);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_update_progress, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this,view);
		updateModel(version);
	}
	@Override
	public void onResume() {
		ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
		int width = getResources().getDisplayMetrics().widthPixels;
		TypedValue tempVal = new TypedValue();
		getResources().getValue(R.dimen.progress_dialog_width_percent, tempVal, true);
		params.width = (int)(width*tempVal.getFloat());
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		super.onResume();
	}
	@OnClick(R.id.buttonCancel)
	void onCancelClick(){
		askCancel();
	}

	private void askCancel() {
		askDialog = NotificationHelper.ask(getContext(), R.string.model_update, R.string.do_you_want_cancel_update,
				R.string.cancel_update, R.string.continue_update, new NotificationHelper.OnButtonClick() {
					@Override
					public void onButtonClick(@StringRes int id) {
						if (id==R.string.cancel_update){
							askDialog = null;
							dismiss();
						}
					}
				});
	}

	private void updateModel(final String timestamp) {
		updateTask = new ModelDownloadTask(timestamp).execute();
	}
	private class ModelDownloadTask extends AsyncTask<Void,Integer,String> {
		private final String version;
		private boolean dialogShown = false;

		public ModelDownloadTask(String version){
			this.version = version;
		}

		@Override
		protected String doInBackground(Void... params) {
			final Request request = new Request.Builder().url(AppConstants.Api.MODEL_LINK)
					.build();
			try {
				okhttp3.Response response = ApiFactory.getProgressClient(new ProgressResponseBody.ProgressListener() {
					@Override
					public void update(long bytesRead, long contentLength, boolean done) {
						publishProgress((int)bytesRead,(int)contentLength);
					}
				}).newCall(request).execute();
				if (isCancelled())
					return null;
				if (response.isSuccessful()) {
					BufferedSink sink = null;
					try {
						sink = Okio.buffer(Okio.sink(ModelHelper.getInstance().getDownloadModelFile()));
						sink.writeAll(response.body().source());
						if (isCancelled())
							return null;
						ModelHelper.getInstance().updateDownloadedModel(version);
						updateLabels();
						return null;
					}
					catch (IOException e){
						e.printStackTrace();
						return e.getMessage();
					}
					finally {
						if (sink!=null){
							try {
								sink.flush();
								sink.close();
							}
							catch (IOException ignore){}
						}
					}

				}
				else{
					return response.message();
				}

			}
			catch (IOException e){
				e.printStackTrace();
				return e.getMessage();
			}

		}


		private void updateLabels()
		{
			Call<LabelsResponse> call = ApiFactory.getApiService().getLabels();
			try {
				Response<LabelsResponse> response = call.execute();
				if (response.isSuccessful()&&response.body().getLabels()!=null){
					ModelHelper.getInstance().checkAndUpdate(response.body().getLabels());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(String message) {
			updateTask = null;
			if (!isCancelled()&&isAdded())
				postResult(message);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (!dialogShown && values.length>1){
				dialogShown = true;
				progress.setMax(values[1]/1000);
			}
			if (values.length>0){
				progress.setProgress(values[0]/1000);
			}
		}
	}

	private void postResult(String message) {
		if (getTargetFragment()!=null){
			Intent intent = new Intent();
			intent.putExtra(EXTRA_SUCCESS,message==null);
			if (message!=null)
				intent.putExtra(EXTRA_MESSAGE,message);
			getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK,intent);
		}
		dismiss();
	}

	@Override
	public void onDestroy() {
		if (askDialog!=null&&askDialog.isShowing())
			askDialog.dismiss();
		if (updateTask!=null)
			updateTask.cancel(true);
		super.onDestroy();
	}
}
