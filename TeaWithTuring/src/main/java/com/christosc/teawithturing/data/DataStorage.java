package com.christosc.teawithturing.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class DataStorage {

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static void saveTextToFile(String storyText, String fileName, Context context) {
        if (!isExternalStorageWritable()) return;
        File file = new File(context.getExternalFilesDir(""), fileName);
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file)));
            out.write(storyText);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveAudioToFile(InputStream is, String fileName, Context context) {
        if (!isExternalStorageWritable()) return;
        File file = new File(context.getExternalFilesDir(""),fileName);
        try {
            BufferedInputStream bis = new BufferedInputStream(is, 1024 * 50);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 50];
            int current;
            while ((current = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, current);
            }

            fos.flush();
            fos.close();
            bis.close();
            Log.d("AUDIO", "file saved to: " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readTextFromFile(String fileName, Context context) {
        String storyText = "";
        if (isExternalStorageReadable()){
            File file = new File(context.getExternalFilesDir(""),fileName);
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file)));
                String line;
                while ((line=in.readLine())!=null){
                    storyText += line+"\n";
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return storyText;
    }
}