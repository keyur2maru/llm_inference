package com.google.mediapipe.examples.llminference;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;

public class AudioRepository implements IRecorderListener {
    private final Recorder recorder;
    public String recordingFilePath = null;
    public Context context;

    public AudioRepository(Context context) {
        this.recorder = new Recorder(context);
        this.recorder.setListener(this);
        this.context = context;
        recordingFilePath = context.getFilesDir().getAbsolutePath() + "/recording.wav";
        this.context.grantUriPermission("io.esper.whisper", FileProvider.getUriForFile(context, "com.google.mediapipe.examples.llminference.fileprovider", new File(recordingFilePath)), Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public void startRecording(String filePath) {
        recordingFilePath = filePath;
        recorder.setFilePath(filePath);
        recorder.start();
    }

    public Uri getRecordingUri() {
        return FileProvider.getUriForFile(context, "com.google.mediapipe.examples.llminference.fileprovider", new File(recordingFilePath));
    }

    public void stopRecording() {
        recorder.stop();
    }

    public void playRecording() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(recordingFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(String message) {
       Log.d("AudioRepository", message);
    }

    @Override
    public void onDataReceived(float[] samples) {
        Log.d("AudioRepository", "Received " + samples.length + " samples");
    }
}