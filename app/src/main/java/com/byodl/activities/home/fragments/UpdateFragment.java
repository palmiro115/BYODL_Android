package com.byodl.activities.home.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byodl.R;
import com.byodl.model.api.VersionResponse;
import com.byodl.network.ApiFactory;
import com.byodl.utils.ModelHelper;
import com.byodl.utils.NotificationHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateFragment extends BaseFragment {
    private static final int REQUEST_MODEL_UPDATE = 1;
    @BindView(R.id.date)
	TextView date;
	@BindView(R.id.time)
	TextView time;

    private static final String TAG = UpdateFragment.class.getSimpleName();
    private Handler backgroundHandler;
    private Handler foregroundHandler = new Handler();


    public UpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        updateViews();
    }

    private void updateViews() {
        long dt = ModelHelper.getInstance().getLastUpdateDate();
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(dt);
        date.setText(SimpleDateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime()));
        time.setText(SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime()));
    }

    protected Handler getForegroundHandler(){
		return foregroundHandler;
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
    }
    @OnClick(R.id.buttonUpdate)
	void onUpdateClick(){
        showDialog(R.string.checking_version);
        Call<VersionResponse> call = ApiFactory.getApiService().getModelVersion();
        call.enqueue(new Callback<VersionResponse>() {
            @Override
            public void onResponse(Call<VersionResponse> call, Response<VersionResponse> response) {
                hideDialog();
                if (response.isSuccessful()){
                    if (response.body()!=null)
                        checkVersion(response.body().getTimestamp());
                }
                else{
                    NotificationHelper.alert(getContext(),getString(R.string.version_check),getString(R.string.version_check_failed,response.message()));
                }
            }

            @Override
            public void onFailure(Call<VersionResponse> call, Throwable t) {
                hideDialog();
                NotificationHelper.alert(getContext(),getString(R.string.version_check),getString(R.string.version_check_failed,t.getMessage()));
            }
        });
    }

    private void checkVersion(final String timestamp) {
        if (timestamp==null)
            NotificationHelper.alert(getContext(),R.string.version_check,R.string.error_response);
        else{
            String appVersion = ModelHelper.getInstance().getVersion();
            if (timestamp.compareTo(appVersion)>0){
                NotificationHelper.ask(getContext(), R.string.version_check, R.string.new_version_found, R.string.cancel, R.string.update, new NotificationHelper.OnButtonClick() {
                    @Override
                    public void onButtonClick(@StringRes int id) {
                        if (id==R.string.update){
                            if (getChildFragmentManager().findFragmentByTag("UPDATE_PROGRESS")==null) {
                                UpdateProgressFragment f = UpdateProgressFragment.newInstance(timestamp);
                                f.setTargetFragment(UpdateFragment.this, REQUEST_MODEL_UPDATE);
                                f.show(getChildFragmentManager(), "UPDATE_PROGRESS");
                            }
                        }

                    }
                });
            }
            else{
                NotificationHelper.alert(getContext(),R.string.version_check,R.string.new_model_version_not_found);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_MODEL_UPDATE&&resultCode== Activity.RESULT_OK){
            boolean isSuccess = data.getBooleanExtra(UpdateProgressFragment.EXTRA_SUCCESS,true);
            String message = data.getStringExtra(UpdateProgressFragment.EXTRA_MESSAGE);
            updateViews();
            if (isSuccess){
                NotificationHelper.alert(getContext(),R.string.update_model,R.string.model_updated);
            }
            else{
                NotificationHelper.alert(getContext(),getString(R.string.update_model),getString(R.string.failed_model_update,message));
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
