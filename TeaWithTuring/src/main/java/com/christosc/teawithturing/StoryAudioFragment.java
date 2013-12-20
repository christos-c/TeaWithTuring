package com.christosc.teawithturing;

import android.app.Fragment;
import android.content.ContentValues;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.christosc.teawithturing.data.DataStorage;
import com.christosc.teawithturing.data.StoriesDatabase;
import com.christosc.teawithturing.data.StoriesProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StoryAudioFragment extends Fragment {
    View rootView;

    private static ImageView imagePlayPause, buttonDownload;
    private static MediaPlayer mediaPlayer;
    private static SeekBar seekBarProgress;
    private static ProgressBar loadingIndicator;
    private int audioLength;

    private Handler handler = new Handler();

    private String audioLocal, audioURL, storyID;

    public StoryAudioFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        audioURL = args.getString(Story.ARG_AUDIO_URL);
        audioLocal = args.getString(Story.ARG_AUDIO_LOCAL);
        storyID = args.getString(Story.ARG_STORY_ID);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_story_audio, container, false);
        assert rootView != null;

        imagePlayPause = (ImageView) rootView.findViewById(R.id.imagePlayPause);
        imagePlayPause.setOnClickListener(playPauseListener);

        buttonDownload = (ImageView) rootView.findViewById(R.id.buttonDownload);
        buttonDownload.setOnClickListener(downloadListener);

        seekBarProgress = (SeekBar)rootView.findViewById(R.id.seekBar);
        seekBarProgress.setMax(99);
        seekBarProgress.setOnTouchListener(touchListener);

        loadingIndicator = (ProgressBar) rootView.findViewById(R.id.audio_loading_indicator);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(updateListener);
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        });
        handler.postDelayed(runnable, 1000);

        new Player().execute(audioURL, audioLocal);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    class Player extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imagePlayPause.setVisibility(View.GONE);
            buttonDownload.setVisibility(View.GONE);
            seekBarProgress.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            Boolean prepared;
            try {
                if (urls[1] != null){
                    //Read from local file
                    Uri audioUri = Uri.parse(getActivity().getExternalFilesDir("") + "/" + urls[1]);
                    mediaPlayer.setDataSource(getActivity(), audioUri);
                }
                else {
                    mediaPlayer.setDataSource(urls[0]);
                }

                mediaPlayer.prepare();
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
            } catch (IOException e) {
                prepared = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            audioLength = mediaPlayer.getDuration();
            loadingIndicator.setVisibility(View.GONE);
            imagePlayPause.setVisibility(View.VISIBLE);
            seekBarProgress.setVisibility(View.VISIBLE);
            if (audioLocal == null)
                buttonDownload.setVisibility(View.VISIBLE);

            Log.d("Prepared", "//" + result);
        }
    }

    class Downloader extends AsyncTask<String, Void, Boolean> {
        private String audioLocal = "TeaWithTuringStory"+storyID+"-audio";

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                Log.d("AUDIO", "downloadListener beginning");
//                Toast.makeText(getActivity(), getString(R.string.download_begin_msg),
//                        Toast.LENGTH_SHORT).show();
                URL url= new URL(urls[0]);
                URLConnection con = url.openConnection();

                InputStream is = con.getInputStream();

                DataStorage.saveAudioToFile(is, audioLocal, getActivity());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d("AUDIO", "downloadListener complete");
//            Toast.makeText(getActivity(), getString(R.string.download_complete_msg),
//                    Toast.LENGTH_SHORT).show();
            buttonDownload.setVisibility(View.GONE);
            //Update the database entry
            ContentValues values = new ContentValues();
            values.put(StoriesDatabase.StoryEntry.COLUMN_LOCAL_AUDIO, audioLocal);
            Uri uri = Uri.withAppendedPath(StoriesProvider.CONTENT_URI, storyID);
            assert uri != null;
            getActivity().getContentResolver().update(uri, values, null, null);
        }
    }

    /*** LISTENERS ***/
    private OnClickListener playPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                imagePlayPause.setImageResource(R.drawable.ic_action_pause);
            }
            else {
                mediaPlayer.pause();
                imagePlayPause.setImageResource(R.drawable.ic_action_play);
            }
        }
    };

    private OnClickListener downloadListener = new OnClickListener() {
        @Override
        public void onClick(View v){
            buttonDownload.setEnabled(false);
            new Downloader().execute(audioURL);
        }
    };

    private OnBufferingUpdateListener updateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            seekBarProgress.setSecondaryProgress(percent);
        }
    };

    private OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(v.getId() == R.id.seekBar) {
                if(mediaPlayer.isPlaying()){
                    SeekBar sb = (SeekBar)v;
                    int playPosition = (audioLength / 100) * sb.getProgress();
                    mediaPlayer.seekTo(playPosition);
                }
            }
            return false;
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null){
                Double percentage =(((double)mediaPlayer.getCurrentPosition())/audioLength)*100;
                seekBarProgress.setProgress(percentage.intValue());
            }
            handler.postDelayed(this, 1000);
        }
    };
}