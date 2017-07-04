package com.byodl.activities.home.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.byodl.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LabelSelectAdapter extends RecyclerView.Adapter<AbstractViewHolder>
{
	private final OnLabelSelectedListener selectedListener;
	private List<String> data = new ArrayList<>();

	private String selected;

	public interface OnLabelSelectedListener{
		void onLabelSelected(String label);
	}
	public LabelSelectAdapter(@NonNull OnLabelSelectedListener listener){
		selectedListener = listener;
	}
	@Override
	public AbstractViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new LabelViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_selectable_label,parent,false));
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
	public void setData(List<String> data){
		this.data.clear();
		selected = null;
		if (data!=null)
			this.data.addAll(data);
		notifyDataSetChanged();
	}
	class LabelViewHolder extends AbstractViewHolder{
		@BindView(R.id.label)
		CheckedTextView label;
		public LabelViewHolder(View itemView)
		{
			super(itemView);
			ButterKnife.bind(this,itemView);
		}

		@Override
		public void bind(int position)
		{
			String s = data.get(position);
			label.setText(s);
			if (selected!=null&&selected.equals(s))
				label.setChecked(true);
			else
				label.setChecked(false);
		}
		@OnClick(R.id.label)
		void onLabelClick(){
			int position = getAdapterPosition();
			if (position==RecyclerView.NO_POSITION)
				return;
			String s = data.get(position);
			if (selected!=null&&selected.equals(s))
				return;
			if (selected!=null){
				int idx = data.indexOf(selected);
				selected = null;
				if (idx>=0){
					notifyItemChanged(idx);
				}
			}
			selected = s;
			notifyItemChanged(position);
			selectedListener.onLabelSelected(s);
		}
	}
	public String getSelected(){
		return selected;
	}
}
