package com.byodl.activities.home.fragments;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.byodl.BYODLApp;
import com.byodl.R;
import com.byodl.activities.home.adapters.HistoryAdapter;
import com.byodl.model.DaoSession;
import com.byodl.model.Prediction;
import com.byodl.model.Predictions;
import com.byodl.widgets.PaddingItemDecoration;

import java.io.File;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PredictHistoryFragment extends DialogFragment implements HistoryAdapter.OnSelectionChangedListener {
	private String imagePath;

	@BindView(R.id.predicts)
	RecyclerView predicts;
	@BindView(R.id.buttonDelete)
	Button deleteButton;

	private HistoryAdapter adapter;

	public PredictHistoryFragment()
	{
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_predict_history, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this,view);
		createAdapter();
	}

	private void createAdapter() {
		if (adapter==null)
			adapter = new HistoryAdapter(this);
		predicts.setLayoutManager(new GridLayoutManager(getContext(),2));
		predicts.addItemDecoration(new PaddingItemDecoration(getResources().getDimensionPixelSize(R.dimen.history_items_padding_h),0));
		predicts.setAdapter(adapter);
		if (getActivity()!=null) {
			List<Predictions> predictions = ((BYODLApp) getActivity().getApplication()).getDaoSession().getPredictionsDao().loadAll();
			adapter.setItems(predictions);
			updateDeleteButton();
		}

	}

	private void updateDeleteButton() {
		if (adapter==null||adapter.getSelectionsCount()==0)
			deleteButton.setEnabled(false);
		else
			deleteButton.setEnabled(true);
	}

	@OnClick({R.id.close,R.id.buttonCancel})
	void onCloseClick(){
		dismiss();
	}

	@Override
	public void onResume() {
		ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
		int width = getResources().getDisplayMetrics().widthPixels;
		TypedValue tempVal = new TypedValue();
		getResources().getValue(R.dimen.dialog_width_percent, tempVal, true);
		params.width = (int)(width*tempVal.getFloat());
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		super.onResume();
	}
	@OnClick(R.id.buttonDelete)
	void onDeleteClick(){
		if (getActivity()==null)
			return;
		DaoSession session = ((BYODLApp)getActivity().getApplication()).getDaoSession();

		Set<Predictions> predictions = adapter.getSelected();
		for (Predictions p:predictions){
			List<Prediction> pred = p.getPredictions();
			for (Prediction pr:pred){
				session.getPredictionDao().delete(pr);
			}
			session.getPredictionsDao().delete(p);
			File f = new File(p.getFileName());
			if (f.exists())
				f.delete();
			adapter.removeItem(p);
		}
		if (getTargetFragment()!=null)
			getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK,null);
		dismiss();
	}

	@Override
	public void onSelectionChanged(Predictions predictions, boolean isSelected) {
		updateDeleteButton();
	}
}
