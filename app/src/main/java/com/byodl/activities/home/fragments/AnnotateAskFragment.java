package com.byodl.activities.home.fragments;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.byodl.BYODLApp;
import com.byodl.R;
import com.byodl.activities.home.adapters.LabelSelectAdapter;
import com.byodl.model.Annotations;
import com.byodl.services.SyncDataService;
import com.byodl.utils.ModelHelper;
import com.byodl.utils.SquareCropTransformation;
import com.byodl.widgets.PaddingItemDecoration;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AnnotateAskFragment extends DialogFragment implements LabelSelectAdapter.OnLabelSelectedListener
{
	private static final String ARG_IMAGE_PATH = "path";

	private String imagePath;

	@BindView(R.id.image)
	ImageView image;
	@BindView(R.id.labels)
	RecyclerView labelsView;
	@BindView(R.id.layoutButtons)
	ViewGroup layoutButtons;
	@BindView(R.id.buttonSave)
	Button buttonSave;

	private LabelSelectAdapter adapter;

	public AnnotateAskFragment()
	{
		// Required empty public constructor
	}

	public static AnnotateAskFragment newInstance(@NonNull String imagePath)
	{
		AnnotateAskFragment fragment = new AnnotateAskFragment();
		Bundle args = new Bundle();
		args.putString(ARG_IMAGE_PATH, imagePath);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			imagePath = getArguments().getString(ARG_IMAGE_PATH);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_photo_result, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this,view);
		layoutButtons.setVisibility(View.VISIBLE);
		adapter = new LabelSelectAdapter(this);
		adapter.setData(ModelHelper.getInstance().getLabels());
		labelsView.setAdapter(adapter);
		LinearLayoutManager lm = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
		lm.setAutoMeasureEnabled(true);
		labelsView.addItemDecoration(new PaddingItemDecoration(getResources().getDimensionPixelOffset(R.dimen.labels_margin),getResources().getDimensionPixelOffset(R.dimen.labels_margin)));
		labelsView.setLayoutManager(lm);
		Picasso
				.with(getContext())
				.load("file:"+imagePath)
				.transform(new SquareCropTransformation())
				.resizeDimen(R.dimen.predict_result_image_size,R.dimen.predict_result_image_size)
				.centerInside()
				.noPlaceholder()
				.noFade()
				.into(image);
		updateSaveButton();
	}
	@OnClick(R.id.close)
	void onCloseClick(){
		deleteFile();
		dismiss();
	}

	private void deleteFile()
	{
		File f = new File(imagePath);
		if (f.exists())
			f.delete();
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
	@OnClick(R.id.buttonSave)
	void onSaveClick(){
		if (getActivity()!=null) {
			String selected = adapter.getSelected();

			Annotations.fromLabels(((BYODLApp)getActivity().getApplication()).getDaoSession(),
					new File(imagePath),
					Collections.singleton(selected));
			SyncDataService.startSync(getContext());
		}
		dismiss();
	}
	@OnClick(R.id.buttonCancel)
	void onCancelClick(){
		deleteFile();
		dismiss();
	}

	@Override
	public void onLabelSelected(String label)
	{
		updateSaveButton();
	}

	private void updateSaveButton()
	{
		buttonSave.setEnabled(adapter.getSelected()!=null);
	}
}
