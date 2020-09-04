package me.wcy.lrcviewsample;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import me.wcy.lrcview.LrcView;

public class MainActivity extends AppCompatActivity {
    private LrcView lrcView;
    private SeekBar seekBar;
    private Button btnPlayPause;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lrcView = findViewById(R.id.lrc_view);
        seekBar = findViewById(R.id.progress_bar);
        btnPlayPause = findViewById(R.id.btn_play_pause);

        try {
            mediaPlayer.reset();
            AssetFileDescriptor fileDescriptor = getAssets().openFd("send_it.m4a");
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mediaPlayer.getDuration());
                seekBar.setProgress(0);
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                lrcView.updateTime(0);
                seekBar.setProgress(0);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 加载歌词文本
        String mainLrcText = getLrcText("send_it_en.lrc");
        String secondLrcText = getLrcText("send_it_cn.lrc");
        lrcView.loadLrc(mainLrcText, secondLrcText);

        // 加载歌词文件
        // File mainLrcFile = new File("/sdcard/Download/send_it_cn.lrc");
        // File secondLrcFile = new File("/sdcard/Download/send_it_en.lrc");
        // lrcView.loadLrc(mainLrcFile, secondLrcFile);

        // 加载在线歌词
        // String url = "http://pz6twp8s0.bkt.clouddn.com/%E6%AD%8C%E8%AF%8D.txt";
        // lrcView.loadLrcByUrl(url, "gb2312");

        lrcView.setDraggable(true, (view, time) -> {
            mediaPlayer.seekTo((int) time);
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                handler.post(runnable);
            }
            return true;
        });

        lrcView.setOnTapListener((view, x, y) -> {
            Toast.makeText(this, "点击歌词", Toast.LENGTH_SHORT).show();
        });

        btnPlayPause.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                handler.post(runnable);
            } else {
                mediaPlayer.pause();
                handler.removeCallbacks(runnable);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                lrcView.updateTime(seekBar.getProgress());
            }
        });
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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying()) {
                long time = mediaPlayer.getCurrentPosition();
                lrcView.updateTime(time);
                seekBar.setProgress((int) time);
            }

            handler.postDelayed(this, 300);
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
