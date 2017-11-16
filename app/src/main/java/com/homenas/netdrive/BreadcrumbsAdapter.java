package com.homenas.netdrive;

import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by engss on 24/10/2017.
 */

public class BreadcrumbsAdapter extends RecyclerView.Adapter<BreadcrumbsAdapter.ViewHolder> {

    public interface BreadcrumbsAdapterListener { // create an interface
        void onCrumbClick(int position); // create callback function
    }

    private final String TAG = getClass().getSimpleName();
    private BreadcrumbsAdapterListener mListener;
    private List<DocumentFile> mDataSet;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Log.i(TAG, "Element " + getAdapterPosition() + " clicked.");
                    mListener.onCrumbClick(getAdapterPosition());
                }
            });
            textView = v.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public BreadcrumbsAdapter(List<DocumentFile> dataSet, BreadcrumbsAdapterListener listener) {
        mDataSet = dataSet;
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.breadcrumbs_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
//        Log.i(TAG, "Name " + position + " set.");
        viewHolder.getTextView().setText(mDataSet.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
