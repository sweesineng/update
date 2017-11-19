package com.homenas.netdrive;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by engss on 24/10/2017.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    public interface CustomAdapterListener { // create an interface
        void onItemClick(int position); // create callback function
    }

    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private CustomAdapterListener mListener;
    private List<SmbData> mDataSet;
    private boolean mViewGrid;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                    mListener.onItemClick(getAdapterPosition());
                }
            });
            textView = v.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public CustomAdapter(Context context, List<SmbData> dataSet, CustomAdapterListener listener) {
        mContext = context;
        mDataSet = dataSet;
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(mViewGrid ? R.layout.card_item : R.layout.row_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.i(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getTextView().setText(mDataSet.get(position).getObjName());
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setView(boolean viewGrid) {
        mViewGrid = viewGrid;
    }
}
