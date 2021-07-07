package com.ecosense.app.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

@SuppressWarnings("deprecation")
public class Text_to_Speech implements TextToSpeech.OnInitListener {
    /**
     * Called when the activity is first created.
     */
//    ** Called when the activity is first created. */
    Context context;
    private TextToSpeech tts;

    public Text_to_Speech(Context context) {
        this.context = context;
        tts = new TextToSpeech(this.context, this);
    }


    public void destroy_tts_Speech() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

    }


    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
//                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }


    @SuppressLint("ObsoleteSdkInt")
    @SuppressWarnings("deprecation")
    public void speakOut(String text) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
//        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
