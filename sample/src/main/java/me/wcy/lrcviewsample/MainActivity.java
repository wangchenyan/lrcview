package me.wcy.lrcviewsample;

import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;

import me.wcy.lrcview.LrcView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    private LrcView lrcBig;
    private LrcView lrcSmall;
    private Button btnPlayPause;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lrcBig = (LrcView) findViewById(R.id.lrc_big);
        lrcSmall = (LrcView) findViewById(R.id.lrc_small);
        btnPlayPause = (Button) findViewById(R.id.btn_play_pause);

        btnPlayPause.setOnClickListener(this);
        mediaPlayer.setOnCompletionListener(this);

        AssetManager am = getAssets();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(am.openFd("cbg.mp3").getFileDescriptor());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lrcBig.loadLrc(getLrcText("rtl.lrc"));
        lrcSmall.loadLrc(getLrcText("cbg.lrc"));
    }

    private String getLrcText(String fileName) {
        String lrcText = null;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            lrcText = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lrcText;
    }

    @Override
    public void onClick(View view) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            handler.post(runnable);
        } else {
            mediaPlayer.pause();
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        lrcBig.onDrag(0);
        lrcSmall.onDrag(0);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying()) {
                long time = mediaPlayer.getCurrentPosition();
                lrcBig.updateTime(time);
                lrcSmall.updateTime(time);
            }

            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }
}
