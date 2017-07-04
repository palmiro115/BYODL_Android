package com.byodl.activities.home.fragments;

import android.app.ProgressDialog;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.byodl.R;

public class BaseFragment extends Fragment {
    private ProgressDialog dlg;

    protected void showDialog(@StringRes int message) {
        hideDialog();
        dlg = new ProgressDialog(getContext(), R.style.AppThemeDialog);
        dlg.setMessage(getString(message));
        dlg.setIndeterminate(true);
        dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dlg.show();

    }

    protected void showProgressDialog(@StringRes int message,int max) {
        hideDialog();
        dlg = new ProgressDialog(getContext(), R.style.AppThemeDialog);
        dlg.setMessage(getString(message));
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dlg.setMax(max);
        //dlg.setProgressPercentFormat(NumberFormat.getNumberInstance());
        dlg.show();

    }

    protected void hideDialog() {
        if (dlg!=null&&dlg.isShowing()){
            dlg.dismiss();
            dlg = null;
        }
    }
    protected void updateProgress(int progress){
        if (dlg!=null&&dlg.isShowing())
            dlg.setProgress(progress);
    }
}
