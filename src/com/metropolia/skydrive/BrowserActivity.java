package com.metropolia.skydrive;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.*;
import com.metropolia.skydrive.R;
import com.metropolia.skydrive.constants.Constants;
import com.metropolia.skydrive.constants.SortCriteria;
import com.metropolia.skydrive.objects.*;
import com.microsoft.live.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;


/**
 * User: Anoja
 * Date: 27.02.13
 * Time: 20:07
 */
public class BrowserActivity extends SherlockListActivity
{
    /* Live Client and download/upload class */
    private LiveConnectClient liveConnectClient;
    
    /* Directory navigation */
    
    private static final String HOME_FOLDER = "me/skydrive";
    private String currentFolderId;
    private Stack<String> previousFolderIds;
    private Stack<String> folderHierarchy;
    private TextView folderHierarchyView;
    private ActionBar actionBar;
    private Stack<ArrayList<SkyDriveObject>> navigationHistory;


    /* File manipulation */
    private boolean isCutNotPaste;
    private ArrayList<SkyDriveObject> filesToBePasted;
    private ArrayList<SkyDriveObject> currentlySelectedFiles;

    /*
     * Holder for the ActionMode, part of the contectual action bar
     * for selecting and manipulating items
     */
    private ActionMode actionMode;

    /* Browser state. If this is set to true only folders will be shown
     * and a button starting an upload of a given file (passed through
     * an intent) to the current folder is added to the layout.
     *
     * Used by the share receiver activity.
     */
    private boolean isUploadDialog = false;
    private boolean isDataOnWifiOnly;
    private ConnectivityManager connectivityManager;

    private LruCache thumbCache;

    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int memClass = ((ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
        final int cacheSize = 1024 * 1024 * memClass / 10;

        thumbCache = new LruCache(cacheSize);


        BrowserForSkyDriveApplication app = (BrowserForSkyDriveApplication) getApplication();

        liveConnectClient = app.getConnectClient();

        currentlySelectedFiles = new ArrayList<SkyDriveObject>();
        filesToBePasted = new ArrayList<SkyDriveObject>();
        previousFolderIds = new Stack<String>();
        currentFolderId = HOME_FOLDER;
        
        setContentView(R.layout.skydrive);

        //determineBrowserStateAndLayout(getIntent());
//        createLocalSkyDriveFolderIfNotExists();
//        setupListView(getListView());


        folderHierarchyView = (TextView) findViewById(R.id.folder_hierarchy);
        folderHierarchy = new Stack<String>();
        folderHierarchy.push(getString(R.string.rootFolderTitle));

        navigationHistory = new Stack<ArrayList<SkyDriveObject>>();

        //updateFolderHierarchy(null);
        app.setCurrentBrowser(this);

        //actionBar = getSupportActionBar();
//        if (savedInstanceState != null)
//        {
//            restoreSavedInstanceState(savedInstanceState);
//        }

        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        //      this, R.array.ContentViewStyles, android.R.layout.simple_spinner_item);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        //actionBar.setListNavigationCallbacks(adapter, new ActionBarNavigationListener());


        loadFolder(currentFolderId);
    }

        private void informUserOfConnectionProblemAndDismiss()
    {
        Toast.makeText(this, "errorLoggedOut", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
    private void loadFolder(String folderId)
    {
        if (folderId == null)
        {
            return;
        }
        if (connectionIsUnavailable())
        {
            return;
        }

        setSupportProgressBarIndeterminateVisibility(true);

       
       

        currentFolderId = folderId;

        if (currentlySelectedFiles != null)
        {
            currentlySelectedFiles.clear();
        }

       
        try
        {
            if (liveConnectClient != null)
            {
                liveConnectClient.getAsync(folderId + "/files?sort_by=" +
                        SortCriteria.NAME + "&sort_order=" + SortCriteria.ASCENDING, new LiveOperationListener()
                {
                    @Override
                    public void onComplete(LiveOperation operation)
                    {
                        setSupportProgressBarIndeterminateVisibility(false);

                        JSONObject result = operation.getResult();
                       
                    }


                    @Override
                    public void onError(LiveOperationException exception, LiveOperation operation)
                    {
                        setSupportProgressBarIndeterminateVisibility(false);
                        Log.e("ASE", exception.getMessage());
                    }
                });
            }
        } catch (IllegalStateException e)
        {
        	Toast.makeText(this, "Handle exception", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean connectionIsUnavailable()
    {
        getPreferences();
        boolean unavailable = (isDataOnWifiOnly &&
                (connectivityManager.getActiveNetworkInfo().getType()
                        != ConnectivityManager.TYPE_WIFI));
        if (unavailable)
        {
            Toast.makeText(this, "noInternetConnection", Toast.LENGTH_LONG).show();
        }
        return unavailable;
    }

    private void getPreferences()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        isDataOnWifiOnly = preferences.getBoolean("limit_all_to_wifi", false);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.browser_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:            	
            	Toast.makeText(this, "Navigate back", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.newFolder:
            	Toast.makeText(this, "New Folder", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.uploadFile:
            	Toast.makeText(this, "Upload file", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.reload:
            	Toast.makeText(this, "Reload folder", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.paste:
            	Toast.makeText(this, "Pasting....", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.sharedFiles:
            	Toast.makeText(this, "Load shared files", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.savedFiles:
            	Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
            	Toast.makeText(this, "Settings shown", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.audioControls:
            	Toast.makeText(this, "Audio controls", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.signOut:
                setSupportProgressBarIndeterminateVisibility(true);
                ((BrowserForSkyDriveApplication) getApplication()).getAuthClient().logout(new LiveAuthListener()
                {
                    @Override
                    public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState)
                    {
                        setSupportProgressBarIndeterminateVisibility(false);
                        Toast.makeText(getApplicationContext(), R.string.loggedOut, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        finish();
                        Log.e(Constants.LOGTAG, "Logged out. Status is " + status + ".");
                    }

                    @Override
                    public void onAuthError(LiveAuthException exception, Object userState)
                    {
                        setSupportProgressBarIndeterminateVisibility(false);
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        finish();
                        Log.e(Constants.LOGTAG, "Error: " + exception.getMessage());
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}



