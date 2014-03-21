package com.christosc.teawithturing;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.christosc.teawithturing.data.DataStorage;
import com.christosc.teawithturing.data.StoriesDatabase;
import com.christosc.teawithturing.data.StoriesProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StoryAudioFragment extends Fragment {
    View rootView;

    int mNotificationId = 1243;

    private static ImageView imagePlayPause, buttonDownload;
    protected static MediaPlayer mediaPlayer;
    private static SeekBar seekBarProgress;

    private Handler handler = new Handler();

    private Player player;

    private Bundle savedState = null;

    private static final String tag = "INFO-AUDIO";
    private ProgressDialog progressDialog;

    public StoryAudioFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(updateListener);
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
                imagePlayPause.setImageResource(R.drawable.ic_action_play);
            }
        });
        handler.postDelayed(runnable, 1000);
        player = new Player();
        Log.d(tag, "Fragment created");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_story_audio, container, false);
        assert rootView != null;

        // Readjust the height of the text if there is audio
        // This is only used when recreating the view (by TabListener)
        ScrollView textView = (ScrollView) getActivity().findViewById(R.id.story_scroller);
        if (textView != null) {
            Log.d(tag, "ScrollView exists");
            int height;
            if (Story.TEXT_HEIGHT_LAND != -1 || Story.TEXT_HEIGHT != -1) {
                if (getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_LANDSCAPE) height = Story.TEXT_HEIGHT_LAND;
                else height = Story.TEXT_HEIGHT;
                Log.d(tag, "Changing height of text fragment to " + height);
                textView.getLayoutParams().height = height;
            }
        }

        imagePlayPause = (ImageView) rootView.findViewById(R.id.imagePlayPause);
        imagePlayPause.setOnClickListener(playPauseListener);

        buttonDownload = (ImageView) rootView.findViewById(R.id.buttonDownload);
        buttonDownload.setOnClickListener(downloadListener);
        if (Story.mAudioLocal != null)
            buttonDownload.setVisibility(View.GONE);

        seekBarProgress = (SeekBar)rootView.findViewById(R.id.seekBar);
        seekBarProgress.setMax(99);
        seekBarProgress.setOnTouchListener(touchListener);

        if (savedInstanceState == null)
            savedInstanceState = savedState;
        if (savedInstanceState != null) {
            // If the media player is playing while moving back to this tab,
            // switch to the pause button (instead of the default play)
            if (mediaPlayer.isPlaying())
                imagePlayPause.setImageResource(R.drawable.ic_action_pause);
        }
        else {
            player.execute(Story.mAudioURL, Story.mAudioLocal);
        }

        Log.d(tag, "View created");
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mediaPlayer.isPlaying())
//            mediaPlayer.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = new Bundle();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void createNotification() {
        Notification.Builder mBuilder =
                new Notification.Builder(getActivity())
                        .setSmallIcon(R.drawable.ic_action_download_notification)
                        .setContentTitle("Take Tea With Turing")
                        .setContentText("Downloading audio from story: " + Story.mStoryTitle);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getActivity(), StoryList.class);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(StoryList.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
        }

        NotificationManager mNotificationManager = (NotificationManager) getActivity().
                getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    class Player extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading audio...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
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
            progressDialog.dismiss();
            if (Story.mAudioLocal == null)
                buttonDownload.setVisibility(View.VISIBLE);
            else
                seekBarProgress.setSecondaryProgress(100);
            Log.d("Prepared", "//" + result);
        }
    }

    class Downloader extends AsyncTask<String, Void, Boolean> {
        private String audioLocal = "TeaWithTuringStory"+Story.mStoryID+"-audio";

        @Override
        protected void onPreExecute() {
            // Dim the download button
            createNotification();
            buttonDownload.getDrawable().setAlpha(128);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                Log.d("AUDIO", "downloadListener beginning");
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
            Toast.makeText(getActivity(), getString(R.string.download_complete_msg),
                    Toast.LENGTH_SHORT).show();
            NotificationManager mNotificationManager = (NotificationManager) getActivity().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(mNotificationId);
            buttonDownload.setVisibility(View.GONE);
            seekBarProgress.setSecondaryProgress(100);
            //Update the database entry
            Story.mAudioLocal = audioLocal;
            ContentValues values = new ContentValues();
            values.put(StoriesDatabase.StoryEntry.COLUMN_LOCAL_AUDIO, audioLocal);
            Uri uri = Uri.withAppendedPath(StoriesProvider.CONTENT_URI, Story.mStoryID);
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
            new Downloader().execute(Story.mAudioURL);
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
                    int playPosition = (mediaPlayer.getDuration() / 100) * sb.getProgress();
                    mediaPlayer.seekTo(playPosition);
                }
            }
            return false;
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                double currentPos = (double) mediaPlayer.getCurrentPosition();
                Double percentage =(currentPos/mediaPlayer.getDuration())*100;
                seekBarProgress.setProgress(percentage.intValue());
            }
            handler.postDelayed(this, 1000);
        }
    };
}