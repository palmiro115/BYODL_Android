package com.byodl.activities.home.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byodl.R;

import org.tensorflow.contrib.android.Classifier;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LabelViewAdapter extends RecyclerView.Adapter<AbstractViewHolder>
{
	private List<Classifier.Recognition> data = new ArrayList<>();
	NumberFormat nf;

	public LabelViewAdapter(){
		nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(1);
	}
	@Override
	public AbstractViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new LabelViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_label,parent,false));
	}

	@Override
	public void onBindViewHolder(AbstractViewHolder holder, int position)
	{
		holder.bind(position);
	}

	@Override
	public int getItemCount()
	{
		return data.size();
	}
	public void setData(List<Classifier.Recognition> data){
		this.data.clear();
		if (data!=null)
			this.data.addAll(data);
		notifyDataSetChanged();
	}
	class LabelViewHolder extends AbstractViewHolder{
		@BindView(R.id.label)
		TextView label;
		public LabelViewHolder(View itemView)
		{
			super(itemView);
			ButterKnife.bind(this,itemView);
		}

		@Override
		public void bind(int position)
		{
			Classifier.Recognition r = data.get(position);
			label.setText(String.format(Locale.getDefault(),"%s - %s%%",r.getTitle(),nf.format(r.getConfidence()*100)));
		}
	}
}
