package com.christosc.teawithturing;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class StoryVideoFragment extends Fragment {
    private static StoryVideoFragment videoFragment;
    private static MediaController mediaController;
    private static View rootView;
    private static VideoView videoView;
    private static ProgressBar loadingIndicator;
    private static String videoURL, videoLocal;

    public static Fragment newInstance() {
        if (videoFragment == null){
            videoFragment = new StoryVideoFragment();
            Bundle args = new Bundle();
            args.putString(Story.ARG_VIDEO_URL, Story.mVideoURL);
            args.putString(Story.ARG_VIDEO_LOCAL, Story.mVideoLocal);
            videoFragment.setArguments(args);
        }
        return videoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        videoURL = args.getString(Story.ARG_VIDEO_URL);
        videoLocal = args.getString(Story.ARG_VIDEO_LOCAL);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_story_video, container, false);
        assert rootView != null;
        mediaController = new MediaController(getActivity());
        videoView =(VideoView) rootView.findViewById(R.id.videoView);
        loadingIndicator = (ProgressBar) rootView.findViewById(R.id.video_loading_indicator);
        new Player().execute(videoURL, videoLocal);
        return rootView;
    }

    public static void showVideo() {
        mediaController.show();
            /*videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                View decorView = getActivity().getWindow().getDecorView();
                // Hide the status bar.
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
                // Remember that you should never show the action bar if the
                // status bar is hidden, so hide that too if necessary.
                ActionBar actionBar = getActivity().getActionBar();
                actionBar.hide();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                videoView.start();
            }
        });*/
    }

    class Player extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingIndicator.setVisibility(View.VISIBLE);
            Log.d("VIDEO", "Pre-execute player");
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            Boolean prepared;
            try {
                if (urls[1] != null){
                    // TODO Read from local file
                }
                else {
                    Log.d("VIDEO", "Loading from "+urls[0]);
                    videoView.setVideoPath(urls[0]);
                }
                prepared = true;
            } catch (IllegalArgumentException e) {
                Log.d("IllegarArgument", e.getMessage());
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
            mediaController.setAnchorView(videoView);
            mediaController.setMediaPlayer(videoView);
            videoView.setMediaController(mediaController);
            videoView.requestFocus();
            videoView.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);
        }
    }
}
