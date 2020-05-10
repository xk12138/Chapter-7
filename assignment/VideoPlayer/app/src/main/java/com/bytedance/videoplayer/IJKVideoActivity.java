package com.bytedance.videoplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class IJKVideoActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private VideoPlayerIJK ijkPlayer;
    private TextView timer;
    private Button play, pause;
    private SeekBar seekBar;
    private boolean isPlaying;
    private int screenHeight, screenWidth;

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

        play = findViewById(R.id.buttonPlay);
        pause = findViewById(R.id.buttonPause);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ijkPlayer.start();
                isPlaying = true;
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ijkPlayer.pause();
                isPlaying = false;
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
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //进入横屏，开始全屏???全屏失败，过于卡顿且发生闪退，原因是linearLayout强制转化失败？？？
            Log.d("IjkVideoActivity", "ORIENTATION_LANDSCAPE");
            getRect();
            play.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.INVISIBLE);
            seekBar.setVisibility(View.INVISIBLE);
            timer.setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ijkPlayer.getLayoutParams();
            lp.height = screenHeight;
            lp.width = screenWidth;
            ijkPlayer.setLayoutParams(lp);
        }
        else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("IjkVideoActivity", "ORIENTATION_PORTRAIT");
            getRect();
            play.setVisibility(View.VISIBLE);
            pause.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            timer.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ijkPlayer.getLayoutParams();
            lp.height = screenWidth * 9 / 16;
            lp.width = screenWidth;
            ijkPlayer.setLayoutParams(lp);
        }
    }

    private void getRect() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
    }
}


