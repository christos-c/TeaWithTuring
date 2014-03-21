package com.christosc.teawithturing.storyScan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.christosc.teawithturing.AboutActivity;
import com.christosc.teawithturing.R;
import com.christosc.teawithturing.Story;
import com.christosc.teawithturing.StoryList;
import com.christosc.teawithturing.data.StoriesDatabase;
import com.christosc.teawithturing.data.StoriesProvider;
import com.qualcomm.QCAR.QCAR;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The main activity for scanning the image markers. */
public class StoryScanActivity extends Activity {
    protected static final String TAG = "DEBUG-QCAR";

    // Application status constants:
    private static final int APPSTATUS_UNINITED = -1;
    private static final int APPSTATUS_INIT_APP = 0;
    private static final int APPSTATUS_INIT_QCAR = 1;
    private static final int APPSTATUS_INIT_TRACKER = 2;
    private static final int APPSTATUS_INIT_APP_AR = 3;
    private static final int APPSTATUS_LOAD_TRACKER = 4;
    private static final int APPSTATUS_INITED = 5;
    private static final int APPSTATUS_CAMERA_STOPPED = 6;
    private static final int APPSTATUS_CAMERA_RUNNING = 7;

    // Name of the native dynamic libraries to load:
    private static final String NATIVE_LIB = "StoryScanActivity";
    private static final String NATIVE_LIB_QCAR = "QCAR";

    // Constants for Hiding/Showing Loading dialog
    static final int HIDE_LOADING_DIALOG = 0;
    static final int SHOW_LOADING_DIALOG = 1;

    private View mLoadingDialogContainer;

    // Our OpenGL view:
    private QCARSampleGLView mGlView;

    // Our renderer:
    private StoryScanActivityRenderer mRenderer;

    // Display size of the device:
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    // Constant representing invalid screen orientation to trigger a query:
    private static final int INVALID_SCREEN_ROTATION = -1;

    // Last detected screen rotation:
    private int mLastScreenRotation = INVALID_SCREEN_ROTATION;

    // The current application status:
    private int mAppStatus = APPSTATUS_UNINITED;

    // The async tasks to initialize the QCAR SDK:
    private InitQCARTask mInitQCARTask;
    private LoadTrackerTask mLoadTrackerTask;

    // An object used for synchronizing QCAR initialization, dataset loading and
    // the Android onDestroy() life cycle event. If the application is destroyed
    // while a data set is still being loaded, then we wait for the loading
    // operation to finish before shutting down QCAR:
    private final Object mShutdownLock = new Object();

    // QCAR initialization flags:
    private int mQCARFlags = 0;

    // The menu item for swapping data sets:
    boolean mIsStonesAndChipsDataSetActive = false;

    private RelativeLayout mUILayout;

    private Random randGenerator = new Random();

    private static List<Integer> storiesTrees, storiesDesk, storiesTea;

    /** Static initializer block to load native libraries on start-up. */
    static {
        loadLibrary(NATIVE_LIB_QCAR);
        loadLibrary(NATIVE_LIB);
    }

    /** Creates a handler to update the status of the Loading Dialog from an UI Thread */
    static class LoadingDialogHandler extends Handler {
        private final WeakReference<StoryScanActivity> mStoryScanActivity;

        LoadingDialogHandler(StoryScanActivity storyScanActivity) {
            mStoryScanActivity = new WeakReference<StoryScanActivity>(storyScanActivity);
        }

        public void handleMessage(Message msg) {
            StoryScanActivity storyScanActivity = mStoryScanActivity.get();
            if (storyScanActivity == null) {
                return;
            }
            if (msg.what == SHOW_LOADING_DIALOG) {
                storyScanActivity.mLoadingDialogContainer.setVisibility(View.VISIBLE);
            } else if (msg.what == HIDE_LOADING_DIALOG) {
                storyScanActivity.mLoadingDialogContainer.setVisibility(View.GONE);
            }
        }
    }

    private Handler loadingDialogHandler = new LoadingDialogHandler(this);

    /** An async task to initialize QCAR asynchronously. */
    private class InitQCARTask extends AsyncTask<Void, Integer, Boolean> {
        // Initialize with invalid value:
        private int mProgressValue = -1;

        @Override
        protected Boolean doInBackground(Void... params) {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized (mShutdownLock) {
                QCAR.setInitParameters(StoryScanActivity.this, mQCARFlags);
                do {
                    // QCAR.init() blocks until an initialization step is
                    // complete, then it proceeds to the next step and reports
                    // progress in percents (0 ... 100%).
                    // If QCAR.init() returns -1, it indicates an error.
                    // Initialization is done when progress has reached 100%.
                    Log.v(TAG, "In loop, pre-init()");
                    mProgressValue = QCAR.init();
                    Log.d(TAG,"Progress value "+mProgressValue);
                    // Publish the progress value:
                    publishProgress(mProgressValue);
                } while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);

                return (mProgressValue > 0);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "QCAR progress: "+values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "On post execute");
            // Done initializing QCAR, proceed to next application
            // initialization status:
            if (result) {
                Log.d(TAG, "InitQCARTask::onPostExecute: QCAR initialization successful");
                updateApplicationStatus(APPSTATUS_INIT_TRACKER);
            } else {
                // Create dialog box for display error:
                AlertDialog dialogError = new AlertDialog.Builder(StoryScanActivity.this).create();

                dialogError.setButton
                        (DialogInterface.BUTTON_POSITIVE, "Close",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Exiting application:
                                        System.exit(1);
                                    }
                                }
                        );

                String logMessage;

                // NOTE: Check if initialization failed because the device is
                // not supported. At this point the user should be informed
                // with a message.
                if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED) {
                    logMessage = "Failed to initialize QCAR because this " +
                            "device is not supported.";
                } else {
                    logMessage = "Failed to initialize QCAR.";
                }
                // Log error:
                Log.d(TAG, "InitQCARTask::onPostExecute: " + logMessage + " Exiting.");

                // Show dialog box with error message:
                dialogError.setMessage(logMessage);
                dialogError.show();
            }
        }
    }

    /** An async task to load the tracker data asynchronously. */
    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... params) {
            // Prevent the onDestroy() method to overlap:
            synchronized (mShutdownLock) {
                // Load the tracker data set:
                return (loadTrackerData() > 0);
            }
        }

        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "LoadTrackerTask::onPostExecute: execution " +
                    (result ? "successful" : "failed"));

            if (result) {
                // The stones and chips data set is now active:
                mIsStonesAndChipsDataSetActive = true;

                // Done loading the tracker, update application status:
                updateApplicationStatus(APPSTATUS_INITED);
            } else {
                // Create dialog box for display error:
                AlertDialog dialogError = new AlertDialog.Builder(StoryScanActivity.this).create();

                dialogError.setButton
                        (DialogInterface.BUTTON_POSITIVE, "Close",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Exiting application:
                                        System.exit(1);
                                    }
                                }
                        );

                // Show dialog box with error message:
                dialogError.setMessage("Failed to load tracker data.");
                dialogError.show();
            }
        }
    }

    /** Stores screen dimensions */
    private void storeScreenDimensions() {
        // Query display dimensions:
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    /** Called when the activity first starts or the user navigates back to an activity. */
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "StoryScanActiviy::onCreate");
        super.onCreate(savedInstanceState);
        // Get a list of story IDs
        getStoryIDs();
        // Query the QCAR initialization flags:
        mQCARFlags = getInitializationFlags();
        // Update the application status to start initializing application:
        updateApplicationStatus(APPSTATUS_INIT_APP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.story_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        switch (item.getItemId()) {
            case R.id.action_list:
                onDestroy();
                Intent intent = new Intent(this, StoryList.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                onDestroy();
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getStoryIDs(){
        storiesDesk = new ArrayList<Integer>();
        storiesTrees = new ArrayList<Integer>();
        storiesTea = new ArrayList<Integer>();

        Log.d(TAG, "Getting story IDs");
        String projection[] = { StoriesDatabase.StoryEntry._ID,
                StoriesDatabase.StoryEntry.COLUMN_STORY_TYPE};
        Cursor cursor = getContentResolver().query(
                Uri.withAppendedPath(StoriesProvider.CONTENT_URI,""),
                projection, null, null, null);
        if (cursor!=null && cursor .moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(
                        StoriesDatabase.StoryEntry._ID)));
                String type = cursor.getString(cursor.getColumnIndex(
                        StoriesDatabase.StoryEntry.COLUMN_STORY_TYPE));
                assert type != null;
                if (type.equals(StoriesDatabase.StoryEntry.TYPE_TREES)) storiesTrees.add(id);
                else if (type.equals(StoriesDatabase.StoryEntry.TYPE_TEA)) storiesTea.add(id);
                else if (type.equals(StoriesDatabase.StoryEntry.TYPE_DESK)) storiesDesk.add(id);
                Log.d(TAG, "Found ID: "+id+" of type "+type);
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    /** Configure QCAR with the desired version of OpenGL ES. */
    private int getInitializationFlags() {
        int flags;
        // Query the native code:
        if (getOpenGlEsVersionNative() == 1) {
            flags = QCAR.GL_11;
        } else {
            flags = QCAR.GL_20;
        }
        return flags;
    }

    /**
     * Native method for querying the OpenGL ES version.
     * Returns 1 for OpenGl ES 1.1, returns 2 for OpenGl ES 2.0.
     */
    public native int getOpenGlEsVersionNative();

    /** Native tracker initialization and deinitialization. */
    public native int initTracker();

    public native void deinitTracker();

    /** Native functions to load and destroy tracking data. */
    public native int loadTrackerData();
    public native void destroyTrackerData();

    /** Native sample initialization. */
    public native void onQCARInitializedNative();

    /** Native method for setting / updating the projection matrix for AR content rendering */
    private native void setProjectionMatrix();

    /** Native methods for starting and stopping the camera. */
    private native void startCamera();
    private native void stopCamera();

    /** Called when the activity will start interacting with the user. */
    protected void onResume() {
        Log.d(TAG, "StoryScanActivity::onResume");
        super.onResume();
        // QCAR-specific resume operation
        QCAR.onResume();
        // We may start the camera only if the QCAR SDK has already been initialized
        if (mAppStatus == APPSTATUS_CAMERA_STOPPED) {
            updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
        }
    }

    private void updateActivityOrientation() {
        Configuration config = getResources().getConfiguration();

        boolean isPortrait = false;

        switch (config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                isPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                isPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }
        Log.d(TAG, "Activity is in " + (isPortrait ? "PORTRAIT" : "LANDSCAPE"));
        setActivityPortraitMode(isPortrait);
    }

    /** Callback for configuration changes the activity handles itself */
    public void onConfigurationChanged(Configuration config) {
        Log.d(TAG, "StoryScanActivity::onConfigurationChanged");
        super.onConfigurationChanged(config);
        updateActivityOrientation();
        storeScreenDimensions();
    }

    /** Called when the system is about to start resuming a previous activity. */
    protected void onPause() {
        Log.d(TAG, "StoryScanActivity::onPause");
        super.onPause();

        if (mAppStatus == APPSTATUS_CAMERA_RUNNING) {
            updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
        }
        // QCAR-specific pause operation
        QCAR.onPause();
    }

    /** Native function to deinitialize the application. */
    private native void deinitApplicationNative();

    /** The final call you receive before your activity is destroyed. */
    protected void onDestroy() {
        Log.d(TAG, "StoryScanActivity::onDestroy");
        super.onDestroy();

        // Cancel potentially running tasks
        if (mInitQCARTask != null &&
                mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED) {
            mInitQCARTask.cancel(true);
            mInitQCARTask = null;
        }

        if (mLoadTrackerTask != null &&
                mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED) {
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }

        // Ensure that all asynchronous operations to initialize QCAR
        // and loading the tracker datasets do not overlap:
        synchronized (mShutdownLock) {
            // Do application deinitialization in native code:
            deinitApplicationNative();
            // Destroy the tracking data set:
            destroyTrackerData();
            // Deinit the tracker:
            deinitTracker();
            // Deinitialize QCAR SDK:
            QCAR.deinit();
        }
        System.gc();
    }

    /**
     * NOTE: this method is synchronized because of a potential concurrent
     * access by ImageTargets::onResume() and InitQCARTask::onPostExecute().
     */
    private synchronized void updateApplicationStatus(int appStatus) {
        // Exit if there is no change in status:
        if (mAppStatus == appStatus)
            return;

        // Store new status value:
        mAppStatus = appStatus;

        // Execute application state-specific actions:
        switch (mAppStatus) {
            case APPSTATUS_INIT_APP:
                // Initialize application elements that do not rely on QCAR
                // initialization:
                initApplication();
                Log.d(TAG, "App initialized");
                // Proceed to next application initialization status:
                updateApplicationStatus(APPSTATUS_INIT_QCAR);
                break;

            case APPSTATUS_INIT_QCAR:
                // Initialize QCAR SDK asynchronously to avoid blocking the
                // main (UI) thread.
                //
                // NOTE: This task instance must be created and invoked on the
                // UI thread and it can be executed only once!
                try {
                    mInitQCARTask = new InitQCARTask();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        mInitQCARTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        mInitQCARTask.execute();
                } catch (Exception e) {
                    Log.d(TAG, "Initializing QCAR SDK failed");
                }
                break;

            case APPSTATUS_INIT_TRACKER:
                // Initialize the ImageTracker:
                if (initTracker() > 0) {
                    // Proceed to next application initialization status:
                    updateApplicationStatus(APPSTATUS_INIT_APP_AR);
                }
                break;

            case APPSTATUS_INIT_APP_AR:
                // Initialize Augmented Reality-specific application elements
                // that may rely on the fact that the QCAR SDK has been
                // already initialized:
                initApplicationAR();
                // Proceed to next application initialization status:
                updateApplicationStatus(APPSTATUS_LOAD_TRACKER);
                break;

            case APPSTATUS_LOAD_TRACKER:
                // Load the tracking data set:
                //
                // NOTE: This task instance must be created and invoked on the
                // UI thread and it can be executed only once!
                try {
                    mLoadTrackerTask = new LoadTrackerTask();
                    mLoadTrackerTask.execute();
                } catch (Exception e) {
                    Log.d(TAG, "Loading tracking data set failed");
                }
                break;

            case APPSTATUS_INITED:
                // Hint to the virtual machine that it would be a good time to
                // run the garbage collector:
                //
                // NOTE: This is only a hint. There is no guarantee that the
                // garbage collector will actually be run.
                System.gc();
                // Native post initialization:
                onQCARInitializedNative();

                // Activate the renderer:
                mRenderer.mIsActive = true;

                // Now add the GL surface view. It is important
                // that the OpenGL ES surface view gets added
                // BEFORE the camera is started and video
                // background is configured.
                addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

                // Sets the UILayout to be drawn in front of the camera
                mUILayout.bringToFront();
                // Start the camera:
                updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);

                break;

            case APPSTATUS_CAMERA_STOPPED:
                // Call the native function to stop the camera:
                stopCamera();
                break;

            case APPSTATUS_CAMERA_RUNNING:
                // Call the native function to start the camera:
                startCamera();
                // Hides the Loading Dialog
                loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);
                // Sets the layout background to transparent
                mUILayout.setBackgroundColor(Color.TRANSPARENT);
                break;

            default:
                throw new RuntimeException("Invalid application state");
        }
    }


    /** Tells native code whether we are in portait or landscape mode */
    private native void setActivityPortraitMode(boolean isPortrait);


    /** Initialize application GUI elements that are not related to AR. */
    private void initApplication() {
        // Set the screen orientation:
        // NOTE: Use SCREEN_ORIENTATION_LANDSCAPE or SCREEN_ORIENTATION_PORTRAIT
        //       to lock the screen orientation for this activity.
        int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;

        // Apply screen orientation
        setRequestedOrientation(screenOrientation);

        updateActivityOrientation();

        // Query display dimensions:
        storeScreenDimensions();

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    /** Native function to initialize the application. */
    private native void initApplicationNative(int width, int height);

    /** Initializes AR application components. */
    private void initApplicationAR() {
        // Do application initialization in native code (e.g. registering callbacks, etc.):
        initApplicationNative(mScreenWidth, mScreenHeight);

        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = QCAR.requiresAlpha();

        mGlView = new QCARSampleGLView(this);
        mGlView.init(mQCARFlags, translucent, depthSize, stencilSize);

        mRenderer = new StoryScanActivityRenderer();
        mRenderer.mActivity = this;
        mGlView.setRenderer(mRenderer);

        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay, null, false);

        if (mUILayout != null){
            mUILayout.setVisibility(View.VISIBLE);
            mUILayout.setBackgroundColor(Color.BLACK);

            // Gets a reference to the loading dialog
            mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);

            // Shows the loading indicator at start
            loadingDialogHandler.sendEmptyMessage(SHOW_LOADING_DIALOG);

            // Adds the inflated layout to the view
            addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
        }
    }

    /** A helper for loading native libraries stored in "libs/armeabi*". */
    public static boolean loadLibrary(String nLibName) {
        try {
            System.loadLibrary(nLibName);
            Log.d(TAG, "Native library lib" + nLibName + ".so loaded");
            return true;
        } catch (UnsatisfiedLinkError ulee) {
            Log.d(TAG, "The library lib" + nLibName + ".so could not be loaded");
        } catch (SecurityException se) {
            Log.d(TAG, "The library lib" + nLibName + ".so was not allowed to be loaded");
        }
        return false;
    }

    /** Updates projection matrix and viewport after a screen rotation change was detected. */
    public void updateRenderView() {
        int currentScreenRotation = getWindowManager().getDefaultDisplay().getRotation();
        if (currentScreenRotation != mLastScreenRotation) {
            // Set projection matrix if there is already a valid one:
            if (QCAR.isInitialized() && (mAppStatus == APPSTATUS_CAMERA_RUNNING)) {
                Log.d(TAG, "StoryScanActivity::updateRenderView");

                // Query display dimensions:
                storeScreenDimensions();

                // Update viewport via renderer:
                mRenderer.updateRendering(mScreenWidth, mScreenHeight);

                // Update projection matrix:
                setProjectionMatrix();

                // Cache last rotation used for setting projection matrix:
                mLastScreenRotation = currentScreenRotation;
            }
        }
    }


    public void targetFound(int targetID){
        Log.d(TAG, "inTargetFound");
        String targetType = translateTarget(targetID);
        int storyID = -1;
        int listIndex;
        if (targetType != null){
            if (targetType.equals(StoriesDatabase.StoryEntry.TYPE_TEA)){
                listIndex = randGenerator.nextInt(storiesTea.size());
                storyID = storiesTea.get(listIndex);
                Log.d(TAG, "Chose story "+storyID+" from TEA");
            }
            else if (targetType.equals(StoriesDatabase.StoryEntry.TYPE_DESK)){
                listIndex = randGenerator.nextInt(storiesDesk.size());
                storyID = storiesDesk.get(listIndex);
                Log.d(TAG, "Chose story "+storyID+" from DESK");
            }
            else if (targetType.equals(StoriesDatabase.StoryEntry.TYPE_TREES)){
                listIndex = randGenerator.nextInt(storiesTrees.size());
                storyID = storiesTrees.get(listIndex);
                Log.d(TAG, "Chose story "+storyID+" from TREES");
            }
        }

        //Now pause the app
        updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
        // QCAR-specific pause operation
        QCAR.onPause();

        if (storyID != -1){
            Bundle args = getData(String.valueOf(storyID));

            Log.d(TAG, "inResultFound");
            Intent detailIntent = new Intent(this, Story.class);
            detailIntent.putExtras(args);
            startActivity(detailIntent);
        }
    }

    private String translateTarget(int targetID){
        String type = null;
        switch (targetID){
            case 0:
                type = StoriesDatabase.StoryEntry.TYPE_TEA;
                break;
            case 1:
                type = StoriesDatabase.StoryEntry.TYPE_TREES;
                break;
            case 2:
                type = StoriesDatabase.StoryEntry.TYPE_DESK;
                break;
        }
        return type;
    }

    private Bundle getData(String storyId){
        Bundle args = new Bundle();
        String projection[] = { StoriesDatabase.StoryEntry._ID,
                StoriesDatabase.StoryEntry.COLUMN_AUTHOR_SURNAME,
                StoriesDatabase.StoryEntry.COLUMN_TITLE,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_TEXT,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_TEXT,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_AUDIO,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_AUDIO,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_VIDEO,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_VIDEO,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_ESSAY,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_ESSAY,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_BIO,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_BIO
        };
        Uri uri = Uri.withAppendedPath(StoriesProvider.CONTENT_URI, storyId);
        assert uri != null;
        Cursor storyCursor = getContentResolver().query(uri, projection,
                null, null, null);
        assert storyCursor != null;
        if (storyCursor.moveToFirst()) {
            String storyID = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry._ID));
            args.putString(Story.ARG_STORY_ID, storyID);
            String storyAuthor = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_AUTHOR_SURNAME));
            args.putString(Story.ARG_STORY_AUTHOR, storyAuthor);

            String storyTitle = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_TITLE));
            args.putString(Story.ARG_STORY_TITLE, storyTitle);

            String value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_TEXT));
            if (value != null && !value.equals("")) args.putString(Story.ARG_TEXT_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_TEXT));
            if (value != null && !value.equals("")) args.putString(Story.ARG_TEXT_LOCAL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_AUDIO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_AUDIO_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_AUDIO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_AUDIO_LOCAL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_VIDEO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_VIDEO_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_VIDEO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_VIDEO_LOCAL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_ESSAY));
            if (value != null && !value.equals("")) args.putString(Story.ARG_ESSAY_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_ESSAY));
            if (value != null && !value.equals("")) args.putString(Story.ARG_ESSAY_LOCAL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_BIO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_BIO_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_BIO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_BIO_LOCAL, value);

        }
        storyCursor.close();
        return args;
    }
}