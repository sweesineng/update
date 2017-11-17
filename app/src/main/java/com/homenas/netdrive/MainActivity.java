package com.homenas.netdrive;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.net.MalformedURLException;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import static com.homenas.netdrive.Constants.PERMISSIONS_REQUEST_CODE;
import static com.homenas.netdrive.Constants.curFiles;
import static com.homenas.netdrive.Constants.fabExpanded;
import static com.homenas.netdrive.Constants.permission;

public class MainActivity extends AppCompatActivity {

    public interface OnBackPressedListener {
        void doBack();
    }

    private final String TAG = getClass().getSimpleName();
    public MenuItem viewMode;
    public MenuItem search;
    public Boolean viewGrid = true;
    private FrameLayout mPopMenu;

    public RecyclerViewFragment mRecyclerViewFragment;
    private StorageManager mStorageManager;
    protected OnBackPressedListener onBackPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        new checkNetwork().execute();
        new NetworkHelper(getApplication()).execute();
        checkPermission();
        checkExtStorage();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPopMenu = (FrameLayout) findViewById(R.id.popMenu);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded) {
                    mRecyclerViewFragment.closeFabSubMenu();
                    fab.animate().rotation(0F).setInterpolator(new DecelerateInterpolator());
                }else{
                    mRecyclerViewFragment.openFabSubMenu();
                    fab.animate().rotation(45F).setInterpolator(new DecelerateInterpolator());
                }
            }
        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Set the first MenuItem title for Actionbar title
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(navigationView.getMenu().getItem(4).getTitle().toString());
        }
        showExtStorage();
        showNetwork();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            onBackPressedListener.doBack();
//            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkExtStorage();
        showExtStorage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        viewMode = menu.findItem(R.id.action_view);
        search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setBackgroundColor(ContextCompat.getColor(this,android.R.color.white));
        ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.GRAY);
        ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(Color.GRAY);
        MenuIcon(this,viewMode,R.drawable.ic_view_list_black_24dp,android.R.color.white);
        MenuIcon(this,search,R.drawable.ic_search_black_24dp,android.R.color.white);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        mRecyclerViewFragment = (RecyclerViewFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        switch(id) {
            case R.id.action_sortName:
                Constants.Sortby = "Name";
                mRecyclerViewFragment.updateData(curFiles);
                return true;
            case R.id.action_sortSize:
                Constants.Sortby = "Size";
                mRecyclerViewFragment.updateData(curFiles);
                return true;
            case R.id.action_view:
                if(viewGrid) {
                    MenuIcon(this, viewMode, R.drawable.ic_view_module_black_24dp, android.R.color.white);
                    mRecyclerViewFragment.setRecyclerViewLayoutManager(Constants.LayoutManagerType.GRID_LAYOUT_MANAGER);
                }else{
                    MenuIcon(this, viewMode, R.drawable.ic_view_list_black_24dp, android.R.color.white);
                    mRecyclerViewFragment.setRecyclerViewLayoutManager(Constants.LayoutManagerType.LINEAR_LAYOUT_MANAGER);
                }
                viewGrid = !viewGrid;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void MenuIcon(Context context, MenuItem item, int icon, @ColorRes int color) {
        item.setIcon(icon);
        Drawable menuDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(menuDrawable);
        DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(context,color));
    }

    private void initFrag() {
        mRecyclerViewFragment = new RecyclerViewFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, mRecyclerViewFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
        }else{
            Log.i(TAG, "Permission: Granted");
            initFrag();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Request Permission: Granted");
                    initFrag();
                } else {
                    Log.i(TAG, "Request Permission: Not Granted");
                }
        }
    }

    private void checkExtStorage() {
        mStorageManager = getApplication().getSystemService(StorageManager.class);
        List<StorageVolume> storageVolumes;
        if (mStorageManager != null) {
            storageVolumes = mStorageManager.getStorageVolumes();
            for (final StorageVolume volume : storageVolumes) {
                if (!volume.isPrimary() && volume.getState().equals(Environment.MEDIA_MOUNTED)) {
                    Constants.ExtSdVol = volume;
                } else {
                    Constants.ExtSdVol = null;
                }
            }
        }
    }

    private void showExtStorage() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        if(Constants.ExtSdVol == null) {
            menu.findItem(R.id.nav_sdcard).setVisible(false);
        }else{
            menu.findItem(R.id.nav_sdcard).setVisible(true);
        }
    }

    private void showNetwork() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        if(Constants.IPAddress == null) {
            menu.findItem(R.id.nav_network).setVisible(false);
        }else{
            menu.findItem(R.id.nav_network).setVisible(true);
        }
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    private class getSmb extends AsyncTask {
        @Override
        protected Object doInBackground(Object... arg0) {
            jcifs.Config.setProperty("jcifs.netbios.baddr", "192.168.174.255");
            try {
                SmbFile root = new SmbFile("smb://");
                if(root.exists()){
                    SmbFile[] list = root.listFiles();
                    for(SmbFile f:list) {
                        Log.i(TAG, "Folder: " + f.getName());
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SmbException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
