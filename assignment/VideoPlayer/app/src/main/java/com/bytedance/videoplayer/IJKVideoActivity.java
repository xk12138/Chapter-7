package com.bytedance.videoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class IJKVideoActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private VideoPlayerIJK ijkPlayer;
    private TextView timer;
    private SeekBar seekBar;
    private boolean isPlaying;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ijkplayer);
        setTitle("ijkPlayer");

        linearLayout = findViewById(R.id.linearLayout);
        ijkPlayer = findViewById(R.id.ijkPlayer);
        timer = findViewById(R.id.timer);

        //加载native库
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            this.finish();
        }

        ijkPlayer.setListener(new VideoPlayerListener());
        ijkPlayer.setVideoResource(R.raw.bytedance);
        isPlaying = false;
        Thread thread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    if(isPlaying) {
                        refreshTime();
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        isPlaying = true;

        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ijkPlayer.start();
                isPlaying = true;
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });

        findViewById(R.id.buttonPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ijkPlayer.pause();
                isPlaying = false;
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SCALED, WindowManager.LayoutParams.FLAG_SCALED);
            }
        });

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //防止使用setProgress时导致重复播放那百分之一
                if(fromUser) {
                    ijkPlayer.seekTo(ijkPlayer.getDuration() * progress / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void refreshTime() {
        timer.setText(parseTime(ijkPlayer.getCurrentPosition()) + "/" + parseTime(ijkPlayer.getDuration()));
        if(seekBar != null && ijkPlayer.getDuration() != 0) {
            seekBar.setProgress((int) (ijkPlayer.getCurrentPosition() * 100 / ijkPlayer.getDuration()));
        }
    }

    private String parseTime(long time) {
        int position = (int)(time / 1000);
        int second = position % 60;
        String sSecond = "";
        if(second < 10) {
            sSecond = "0" + String.valueOf(second);
        }
        else {
            sSecond = String.valueOf(second);
        }
        int minute = position / 60;
        String sMinute = "";
        if(minute < 10) {
            sMinute = "0" + String.valueOf(minute);
        }
        else {
            sMinute = String.valueOf(minute);
        }
        return sMinute + ":" + sSecond;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ijkPlayer.isPlaying()) {
            ijkPlayer.stop();
        }

        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //使用在旋转为横屏后容易黑屏，且没有报错信息，未能找出错误原因。
    }
}


