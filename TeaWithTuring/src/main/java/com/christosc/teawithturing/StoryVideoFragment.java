package com.christosc.teawithturing;

import android.app.Fragment;
import android.media.MediaPlayer;
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
    private static MediaController mediaController;
    private static VideoView videoView;
    private static Player player;

    private Bundle savedState = null;

    private static String tag = "INFO-VIDEO";
    private View rootView;

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
        mediaController = new MediaController(getActivity());
        player = new Player();
        this.setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_story_video, container, false);
        assert rootView != null;

        if (savedInstanceState != null || savedState != null) {
            Log.d(tag, "Saved state");
            if (savedInstanceState == null)
                savedInstanceState = savedState;
            videoView.seekTo(savedInstanceState.getInt("videoPos"));
            Log.d(tag, "videoView "+((videoView == null) ? "" : "not ") + "null");
            Log.d(tag, "mediaController "+((mediaController == null) ? "" : "not ") + "null");
//            mediaController.setAnchorView(videoView);
//            mediaController.setMediaPlayer(videoView);
//            videoView.setMediaController(mediaController);
//            videoView.setVisibility(View.VISIBLE);
            mediaController.show();
        }
        else {
            Log.d(tag, "New instance");
            videoView = (VideoView) rootView.findViewById(R.id.videoView);
            player.execute(Story.mVideoURL, Story.mVideoLocal);
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mediaController.hide();
        savedState = new Bundle();
        savedState.putInt("videoPos", videoView.getCurrentPosition());
    }

    class Player extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(tag, "Pre-execute player");
            rootView.findViewById(R.id.video_progressBar).setVisibility(View.VISIBLE);
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
            Log.d(tag, "Post-execute player");
            mediaController.setAnchorView(videoView);
            mediaController.setMediaPlayer(videoView);
            videoView.setMediaController(mediaController);

            videoView.setVisibility(View.VISIBLE);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    rootView.findViewById(R.id.video_progressBar).setVisibility(View.GONE);
                    videoView.start();
                    mediaController.show();
                }
            });
        }
    }
}
