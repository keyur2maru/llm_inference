package com.google.mediapipe.examples.llminference;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import io.esper.whisper.IEsperWhisperService;

public class WhisperServiceConnection {
    private static final String TAG = "WhisperServiceConnection";
    private IEsperWhisperService whisperService;
    private final Context context;
    private boolean isBound = false;

    public interface TranscriptionCallback {
        void onTranscriptionReceived(String transcription);
        void onTranscriptionError(String error);
    }

    public WhisperServiceConnection(Context applicationContext) {
        this.context = applicationContext;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            whisperService = IEsperWhisperService.Stub.asInterface(service);
            Log.d(TAG, "Whisper service connected");
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            whisperService = null;
            Log.d(TAG, "Whisper service disconnected");
            isBound = false;
        }
    };

    public void bindService() {
        if (!isBound) {
            Intent intent = new Intent("android.permission.BIND_ESPER_WHISPER_SERVICE");
            intent.setPackage("io.esper.whisper");
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection);
            isBound = false;
        }
    }

    public void transcribeAudio(String filePath, TranscriptionCallback callback) {
        if (!isBound) {
            Log.e(TAG, "Service not bound, attempting to bind now");
            bindService();
            callback.onTranscriptionError("Service not bound");
            return;
        }
        // Execute transcription in a background thread to prevent UI blocking
        new Thread(() -> {
            try {
                String result = whisperService.transcribeAudio(filePath);
                Log.d(TAG, "Transcription result: " + result);
                callback.onTranscriptionReceived(result);
            } catch (Exception e) {
                Log.e(TAG, "Error transcribing audio", e);
                callback.onTranscriptionError("Error transcribing audio");
            }
        }).start();
    }
}

