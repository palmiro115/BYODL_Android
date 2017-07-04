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
import android.widget.ImageView;

import com.byodl.R;
import com.byodl.activities.home.adapters.LabelViewAdapter;
import com.byodl.utils.SquareCropTransformation;
import com.byodl.widgets.PaddingItemDecoration;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

import org.tensorflow.contrib.android.Classifier;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PredictResultFragment extends DialogFragment
{
	private static final String ARG_IMAGE_PATH = "path";
	private static final String ARG_PREDICTIONS = "predictions";

	private String imagePath;
	private List<Classifier.Recognition> predictions;

	@BindView(R.id.image)
	ImageView image;
	@BindView(R.id.labels)
	RecyclerView labelsView;

	private LabelViewAdapter adapter;

	public PredictResultFragment()
	{
		// Required empty public constructor
	}

	public static PredictResultFragment newInstance(@NonNull String imagePath, List<Classifier.Recognition> predictions)
	{
		PredictResultFragment fragment = new PredictResultFragment();
		Bundle args = new Bundle();
		args.putString(ARG_IMAGE_PATH, imagePath);
		if (predictions!=null)
			args.putString(ARG_PREDICTIONS, new Gson().toJson(predictions,Classifier.Recognition.LIST_TYPE));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			imagePath = getArguments().getString(ARG_IMAGE_PATH);
			String json = getArguments().getString(ARG_PREDICTIONS);
			if (json!=null) {
				try {
					predictions = new Gson().fromJson(json, Classifier.Recognition.LIST_TYPE);
				}catch (JsonSyntaxException e){e.printStackTrace();}
			}
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

		adapter = new LabelViewAdapter();
		adapter.setData(predictions);
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
	}
	@OnClick(R.id.close)
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
}
