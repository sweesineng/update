package com.homenas.netdrive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.homenas.netdrive.Constants.KEY_LAYOUT_MANAGER;
import static com.homenas.netdrive.Constants.LayoutManagerType;
import static com.homenas.netdrive.Constants.LocalRoot;
import static com.homenas.netdrive.Constants.PERMISSIONS_REQUEST_CODE;
import static com.homenas.netdrive.Constants.SPAN_COUNT;
import static com.homenas.netdrive.Constants.curFiles;
import static com.homenas.netdrive.Constants.fabExpanded;
import static com.homenas.netdrive.R.id.recyclerView;


/**
 * Created by engss on 24/10/2017.
 */

public class RecyclerViewFragment extends Fragment
        implements CustomAdapter.CustomAdapterListener, MainActivity.OnBackPressedListener, BreadcrumbsAdapter.BreadcrumbsAdapterListener {

    private final String TAG = getClass().getSimpleName();
    private CustomAdapter mAdapter;
    private BreadcrumbsAdapter bAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView brecyclerView;
    private LayoutManagerType mCurrentLayoutManagerType;
    private RecyclerView.LayoutManager mLayoutManager;
    private FrameLayout mPopMenu;

    private List<FilesData> mDataset = new ArrayList<>();
    private List<DocumentFile> mCrumbs = new ArrayList<>();
    public Boolean viewGrid = true;
    private DocumentFile curRoot;
    private int curCrumb = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CustomAdapter(getActivity(), mDataset, this);
        bAdapter = new BreadcrumbsAdapter(mCrumbs, this);
        ((MainActivity) getActivity()).setOnBackPressedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recyclerview_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(recyclerView);
        mRecyclerView.hasFixedSize();
        DividerItemDecoration hDividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL);
        DividerItemDecoration lDividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(hDividerItemDecoration);
        mRecyclerView.addItemDecoration(lDividerItemDecoration);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        brecyclerView = (RecyclerView) ((AppCompatActivity)getActivity()).findViewById(R.id.breadcrumbs);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        brecyclerView.setLayoutManager(mLayoutManager);
        brecyclerView.setItemAnimator(new DefaultItemAnimator());
        bAdapter = new BreadcrumbsAdapter(mCrumbs, this);
        brecyclerView.setAdapter(bAdapter);
        initItemList();

        mPopMenu = (FrameLayout) getActivity().findViewById(R.id.popMenu);
        final FloatingActionButton fabfolder = (FloatingActionButton) ((AppCompatActivity)getActivity()).findViewById(R.id.fabFolder);
        fabfolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Create New Folder", Toast.LENGTH_SHORT).show();
                closeFabSubMenu();
            }
        });

        final FloatingActionButton fabfile = (FloatingActionButton) ((AppCompatActivity)getActivity()).findViewById(R.id.fabFile);
        fabfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Create New Files", Toast.LENGTH_SHORT).show();
                closeFabSubMenu();
            }
        });
    }

    public void initItemList() {
        mCrumbs.clear();
        curRoot = LocalRoot;
        updateData(curRoot);
    }

    public void setRecyclerViewLayoutManager(Constants.LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }
        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                mAdapter.setView(viewGrid);
                mRecyclerView.setAdapter(mAdapter);
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                mAdapter.setView(!viewGrid);
                mRecyclerView.setAdapter(mAdapter);
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                mAdapter.setView(!viewGrid);
                mRecyclerView.setAdapter(mAdapter);
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onItemClick(int position){
//        Toast.makeText(getActivity(), "click at " + mDataset.get(position), Toast.LENGTH_SHORT).show();
        updateData(mDataset.get(position).getDoc());
    }

    @Override
    public void onCrumbClick(int position){
        mCrumbs.subList(position + 1,mCrumbs.size()).clear();
        updateData(mCrumbs.get(mCrumbs.size() -1));
    }

    @Override
    public void doBack() {
        if(curFiles.getUri().toString().equals(curRoot.getUri().toString())) {
            Toast.makeText(getActivity(), "At root ", Toast.LENGTH_SHORT).show();
        }else{
            mCrumbs.remove(curFiles);
            updateData(curFiles.getParentFile());
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateData(DocumentFile files) {
        if(files.isDirectory()) {
            mDataset.clear();
            curFiles = files;
            if(mCrumbs.size() != 0) {
                if(!mCrumbs.get(mCrumbs.size()-1).getName().equals(curFiles.getName())) {
                    mCrumbs.add(curFiles);
                    bAdapter.notifyItemInserted(mCrumbs.size());
                    curCrumb = mCrumbs.size();
                }else{
                    bAdapter.notifyItemRangeRemoved(mCrumbs.size(),curCrumb-mCrumbs.size());
                }
                brecyclerView.smoothScrollToPosition(mCrumbs.size()-1);
            }else{
                mCrumbs.add(curFiles);
                bAdapter.notifyDataSetChanged();
                curCrumb = mCrumbs.size();
            }
            for(DocumentFile file : files.listFiles()) {
                FilesData data = new FilesData(file, Constants.Sortby, Constants.Accending);
                mDataset.add(data);
            }
            final ConstraintLayout noItem = (ConstraintLayout) getActivity().findViewById(R.id.noItem);
            if(mDataset.size() == 0) {
                noItem.setVisibility(View.VISIBLE);
            }else{
                noItem.setVisibility(View.INVISIBLE);
            }
            Collections.sort(mDataset,FilesData.Comparators.Sort);
            mAdapter.notifyDataSetChanged();
        }
    }



    public void getExtStorage() {
        StorageManager mStorageManager = getActivity().getSystemService(StorageManager.class);
        if (mStorageManager != null) {
            List<StorageVolume> storageVolumes = mStorageManager.getStorageVolumes();
            for (final StorageVolume volume : storageVolumes) {
                if(!volume.isPrimary()){
                    Intent intent = volume.createAccessIntent(null);
                    startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSIONS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(data.getData() != null) {
                getActivity().getContentResolver().takePersistableUriPermission(data.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Constants.ExtStorage = DocumentFile.fromTreeUri(getActivity(),data.getData());
                curRoot = Constants.ExtStorage;
                mCrumbs.clear();
                updateData(Constants.ExtStorage);
            }
        }
    }

    public void closeFabSubMenu() {
        mPopMenu.setVisibility(View.INVISIBLE);
        Constants.fabExpanded = false;
    }

    public void openFabSubMenu() {
        mPopMenu.setVisibility(View.VISIBLE);
        fabExpanded = true;
    }
}
