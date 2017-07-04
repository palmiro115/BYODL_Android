package com.byodl.activities.home.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.byodl.R;
import com.byodl.model.Prediction;
import com.byodl.model.Predictions;
import com.byodl.widgets.CheckableFrameLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HistoryAdapter extends RecyclerView.Adapter<AbstractViewHolder> {

    private final OnSelectionChangedListener selectionListener;

    public interface OnSelectionChangedListener{
        void onSelectionChanged(Predictions predictions,boolean isSelected);
    }

    private List<Predictions> data = new ArrayList<>();
    private Set<Predictions> selected = new HashSet<>();

    public HistoryAdapter(@NonNull OnSelectionChangedListener listener){
        selectionListener = listener;
    }

    @Override
    public AbstractViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item,parent,false));
    }

    @Override
    public void onBindViewHolder(AbstractViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    public void setItems(List<Predictions> predictions){
        data.clear();
        selected.clear();
        if (predictions!=null)
            data.addAll(predictions);
        notifyDataSetChanged();
    }
    public int getSelectionsCount(){
        return selected.size();
    }
    class HistoryViewHolder extends AbstractViewHolder{
        @BindView(R.id.rootView)
        CheckableFrameLayout root;
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.labels)
        TextView labels;
        public HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bind(int position) {
            Predictions p = data.get(position);
            Picasso.with(itemView.getContext())
                    .load(new File(p.getFileName()))
                    .fit()
                    .centerCrop()
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            //do nothing
                        }

                        @Override
                        public void onError() {
                            image.setImageResource(R.drawable.ic_photo_white_48dp);
                        }
                    });
            String out = "";
            for (Prediction pr:p.getPredictions()){
                if (out.length()>0)
                    out+=", ";
                out+=pr.getLabel();
            }
            labels.setText(out);
        }
        @OnClick(R.id.rootView)
        void onRootClick(){
            int position = getAdapterPosition();
            if (position==RecyclerView.NO_POSITION)
                return;
            Predictions p = data.get(position);
            boolean isSelected = selected.contains(p);
            if (isSelected){
                selected.remove(p);
                root.setChecked(false);
            }
            else{
                selected.add(p);
                root.setChecked(true);
            }
            isSelected = !isSelected;
            if (selectionListener!=null){
                selectionListener.onSelectionChanged(p,isSelected);
            }
        }
    }

    public Set<Predictions> getSelected() {
        Set<Predictions> p = new HashSet<>();
        p.addAll(selected);
        return p;
    }


    public void removeItem(Predictions p) {
        int idx = data.indexOf(p);
        if (idx>=0){
            data.remove(idx);
            notifyItemRemoved(idx);
        }
        selected.remove(p);
        selectionListener.onSelectionChanged(p,false);
    }

}
