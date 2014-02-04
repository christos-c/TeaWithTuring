package com.christosc.teawithturing;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;

public class StoryVideoFragment extends Fragment {
    // Shared with VideoView to keep the state
    public static MediaPlayer mMediaPlayer;
    public static Uri mUri;
    public static MediaController mMediaController;
    public static ProgressDialog progressDialog;
    public static int mSeekWhenPrepared;

    private CustomVideoView videoView;
    private static Player player;

    private Bundle savedState = null;

    private static String tag = "INFO-VIDEO";
    private View rootView;

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    /*public static interface VideoTaskCallbacks {
        void onConfigurationChanged(Configuration newConfig);
    }
    private VideoTaskCallbacks callbacks;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (VideoTaskCallbacks) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }*/
    /*public static Fragment newInstance(String mVideoURL, String mVideoLocal) {
        if (videoFragment == null){
            videoFragment = new StoryVideoFragment();
            Bundle args = new Bundle();
            args.putString(Story.ARG_VIDEO_URL, mVideoURL);
            args.putString(Story.ARG_VIDEO_LOCAL, mVideoLocal);
            videoFragment.setArguments(args);
        }
        return videoFragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaController = new MediaController(getActivity());
        player = new Player();
        savedState = null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_story_video, container, false);
        assert rootView != null;

        if (StoryAudioFragment.mediaPlayer != null && StoryAudioFragment.mediaPlayer.isPlaying())
            StoryAudioFragment.mediaPlayer.pause();

        if (getActivity().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE)
            setFullScreenMode();

        if (savedInstanceState == null)
            savedInstanceState = savedState;

        if (savedInstanceState != null) {
            int videoPos = savedInstanceState.getInt("videoPos");
            Log.d(tag, "Saved state - video position: " + videoPos);
//            videoView.setMediaController(mMediaController);
            if (videoPos > 2000) videoPos -= 2000;
            videoView.setSeekPos(videoPos);
            progressDialog.show();
        }
        else {
            Log.d(tag, "New instance");
            videoView = (CustomVideoView) rootView.findViewById(R.id.videoView);
            player.execute(Story.mVideoURL, Story.mVideoLocal);
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMediaController.hide();
        savedState = new Bundle();
        savedState.putInt("videoPos", videoView.getCurrentPosition());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            Log.d(tag, "LANDSCAPE MODE");
            setFullScreenMode();
        }
        else {
            Log.d(tag, "PORTRAIT MODE");
            WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
            getActivity().getWindow().clearFlags(attrs.flags);
            getActivity().getActionBar().show();
        }
    }

    private void setFullScreenMode() {
        WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setAttributes(attrs);
        getActivity().getActionBar().hide();
//        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    class Player extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(tag, "Pre-execute player");
            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setTitle("Story Video");
            progressDialog.setMessage("Loading video...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            videoView.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            Boolean prepared;
            try {
                if (urls[1] != null){
                    // TODO Read from local file
                }
                else {
                    Log.d(tag, "Loading from "+urls[0]);
                    videoView.setVideoPath(urls[0]);
                }
                prepared = true;
            } catch (IllegalArgumentException e) {
                Log.d("IllegalArgument", e.getMessage());
                prepared = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                prepared = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                prepared = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d(tag, "Post-execute player");
            Log.d(tag, "Video can " +((videoView.canPause())? "" : "not ") + "pause");
//            progressDialog.dismiss();
            mMediaController.setAnchorView(videoView);
            mMediaController.setMediaPlayer(videoView);
            videoView.setMediaController(mMediaController);
            videoView.setVisibility(View.VISIBLE);
            videoView.setKeepScreenOn(true);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    progressDialog.dismiss();
                    videoView.start();
                    mMediaController.show();
                }
            });
        }
    }
}
