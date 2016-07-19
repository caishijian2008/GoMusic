package com.csj.gomusic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.csj.gomusic.R;
import com.csj.gomusic.adapter.MusicListAdapter;
import com.csj.gomusic.entity.MusicInfo;
import com.csj.gomusic.service.MyService;
import com.csj.gomusic.utils.MediaUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "main";

    public static final int PLAY_MSG = 1;        //播放
    public static final int PAUSE_MSG = 2;        //暂停
    public static final int STOP_MSG = 3;        //停止
    public static final int CONTINUE_MSG = 4;    //继续
    public static final int PREVIOUS_MSG = 5;    //上一首
    public static final int NEXT_MSG = 6;        //下一首
    public static final int PROGRESS_CHANGE = 7;//进度改变
    public static final int PLAYING_MSG = 8;    //正在播放
    public static final String MUSIC_SERVICE = "com.csj.media.MUSIC_SERVICE";

    private Button btnPlay, btnPrevious, btnNext, btnRepeatMusic, btnShuffleMusic;
    private ImageView ivMusicAlbum;
    private TextView tvMusicName, tvMusicTime;
    private ListView mlvMusicList; //音乐列表
    private int listPosition = 0; // 标识列表位置
    private List<MusicInfo> musicInfos;
    private MusicListAdapter musicListAdapter; //音乐列表适配器
    private MainReceiver mainReceiver;

    private SeekBar musicProgress; //音乐拖动条
    private TextView currentProgress; //显示当前进度
    private TextView finalProgress; //显示歌曲时间

    private boolean isFirstTime = true;
    private boolean isPlaying; // 正在播放
    private boolean isPause; // 暂停

    private int currentTime; // 当前时间
    private int duration; // 时长

    private int repeatState; // 循环标识
    private final int isCurrentRepeat = 1; // 单曲循环
    private final int isAllRepeat = 2; // 全部循环
    private final int isNoneRepeat = 3; // 无重复播放
    private boolean isNoneShuffle = true; // 顺序播放
    private boolean isShuffle = false; // 随机播放

    // 一系列动作
    public static final String CTL_ACTION = "com.csj.action.CTL_ACTION"; // 控制动作
    public static final String UPDATE_ACTION = "com.csj.action.UPDATE_ACTION"; // 更新动作
    public static final String MUSIC_CURRENT = "com.csj.action.MUSIC_CURRENT"; // 当前音乐改变动作
    public static final String MUSIC_DURATION = "com.csj.action.MUSIC_DURATION"; // 音乐时长改变动作
    public static final String REPEAT_ACTION = "com.csj.action.REPEAT_ACTION"; // 音乐重复改变动作
    public static final String SHUFFLE_ACTION = "com.csj.action.SHUFFLE_ACTION"; // 音乐随机播放动作

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(); //初始化控件
        setViewOnClickListener(); //设置控件的监听器
        //初始化ListView，显示歌曲列表
        try {
            musicInfos = MediaUtil.getMusicInfos(MainActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "没有找到歌曲！", Toast.LENGTH_SHORT).show();
                }
            });
        }
        musicListAdapter = new MusicListAdapter(MainActivity.this, musicInfos);
        mlvMusicList.setAdapter(musicListAdapter);
        // 单击列表项播放歌曲
        mlvMusicList.setOnItemClickListener(new MusicListItemClickListener());

        repeatState = isNoneRepeat;// 初始话播放状态为顺序播放状态

        mainReceiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        filter.addAction(MUSIC_CURRENT);
        filter.addAction(MUSIC_DURATION);
        filter.addAction(REPEAT_ACTION);
        filter.addAction(SHUFFLE_ACTION);
        // 注册BroadcastReceiver
        registerReceiver(mainReceiver, filter);

    }

    private void initView() {
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnRepeatMusic = (Button) findViewById(R.id.btnRepeatMusic);
        btnShuffleMusic = (Button) findViewById(R.id.btnShuffleMusic);
        ivMusicAlbum = (ImageView) findViewById(R.id.ivMusicAlbum);
        tvMusicName = (TextView) findViewById(R.id.tvMusicName);
        tvMusicTime = (TextView) findViewById(R.id.tvMusicTime);
        mlvMusicList = (ListView) findViewById(R.id.lvMusicList);

        musicProgress = (SeekBar) findViewById(R.id.sbMusicProgress);
        currentProgress = (TextView) findViewById(R.id.tvTimeStart);
        finalProgress = (TextView) findViewById(R.id.tvTimeEnd);
    }

    private void setViewOnClickListener() {
        ViewOnClickListener onClickListener = new ViewOnClickListener();
        btnPlay.setOnClickListener(onClickListener);
        btnPrevious.setOnClickListener(onClickListener);
        btnNext.setOnClickListener(onClickListener);
        btnRepeatMusic.setOnClickListener(onClickListener);
        btnShuffleMusic.setOnClickListener(onClickListener);
        musicProgress.setOnSeekBarChangeListener(new SeekBarChangeListener());
    }

    private class ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(CTL_ACTION);
            switch (view.getId()) {
                case R.id.btnPlay:
                    if (isFirstTime) {
                        play();
                        isFirstTime = false;
                        isPlaying = true;
                        isPause = false;
                    } else {
                        if (isPlaying) {
                            btnPlay.setBackgroundResource(R.mipmap.pause);
                            intent.setAction("com.csj.media.MUSIC_SERVICE");
                            intent.putExtra("msg", PAUSE_MSG);
                            startService(intent);
                            isPlaying = false;
                            isPause = true;
                        } else if (isPause) {
                            btnPlay.setBackgroundResource(R.mipmap.action_play_normal);
                            intent.setAction("com.csj.media.MUSIC_SERVICE");
                            intent.putExtra("msg", CONTINUE_MSG);
                            startService(intent);
                            isPause = false;
                            isPlaying = true;
                        }
                    }
                    break;
                case R.id.btnPrevious:
                    isFirstTime = false;
                    isPlaying = true;
                    isPause = false;
                    previous();
                    break;
                case R.id.btnNext:
                    isFirstTime = false;
                    isPlaying = true;
                    isPause = false;
                    next();
                    break;
                case R.id.btnRepeatMusic:
                    if (repeatState == isNoneRepeat) {
                        repeat_one();
                        btnShuffleMusic.setClickable(false);
                        repeatState = isCurrentRepeat;
                    } else if (repeatState == isCurrentRepeat) {
                        repeat_all();
                        btnShuffleMusic.setClickable(false);
                        repeatState = isAllRepeat;
                    } else if (repeatState == isAllRepeat) {
                        repeat_none();
                        btnShuffleMusic.setClickable(true);
                        repeatState = isNoneRepeat;
                    }
                    switch (repeatState) {
                        case isCurrentRepeat: //单曲循环
                            btnRepeatMusic.setBackgroundResource(R.mipmap.repeat_current);
                            Toast.makeText(MainActivity.this, R.string.repeat_current, Toast.LENGTH_SHORT).show();
                            break;
                        case isAllRepeat: //全部循环
                            btnRepeatMusic.setBackgroundResource(R.mipmap.repeat_all);
                            Toast.makeText(MainActivity.this, R.string.repeat_all, Toast.LENGTH_SHORT).show();
                            break;
                        case isNoneRepeat: //顺序播放
                            btnRepeatMusic.setBackgroundResource(R.mipmap.repeat_none);
                            Toast.makeText(MainActivity.this, R.string.repeat_none, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case R.id.btnShuffleMusic:
                    if (isNoneShuffle){
                        btnShuffleMusic.setBackgroundResource(R.mipmap.shuffle);
                        Toast.makeText(MainActivity.this, R.string.shuffle, Toast.LENGTH_SHORT).show();
                        isNoneShuffle = false;
                        isShuffle = true;
                        shuffleMusic();
                        btnRepeatMusic.setClickable(false);
                    } else if (isShuffle) {
                        btnShuffleMusic.setBackgroundResource(R.mipmap.shuffle_none);
                        Toast.makeText(MainActivity.this, R.string.shuffle_none, Toast.LENGTH_SHORT).show();
                        isShuffle = false;
                        isNoneShuffle = true;
                        btnRepeatMusic.setClickable(true);
                    }
                    break;
            }
        }
    }

    // 顺序播放
    private void repeat_none() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 3);
        sendBroadcast(intent); //发送广播，将被MyService接收
    }

    //全部循环
    private void repeat_all() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 2);
        sendBroadcast(intent); //发送广播，将被MyService接收
    }

    //单曲循环
    private void repeat_one() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 1);
        sendBroadcast(intent); //发送广播，将被MyService接收
    }

    // 随机播放
    private void shuffleMusic() {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 4);
        sendBroadcast(intent); //发送广播，将被MyService接收
    }

    private void next() {
        listPosition = listPosition + 1;
        if (listPosition <= musicInfos.size() - 1) {
            MusicInfo musicInfo = musicInfos.get(listPosition);
            tvMusicName.setText(musicInfo.getDisplayName());
            tvMusicTime.setText(MediaUtil.formatTime(musicInfo.getDuration()));
            finalProgress.setText(MediaUtil.formatTime(musicInfo.getDuration()));
            Intent intent = new Intent();
            intent.setAction(MUSIC_SERVICE);
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("url", musicInfo.getUrl());
            intent.putExtra("msg", NEXT_MSG);
            startService(intent);
        } else {
            listPosition = listPosition - 1;
            Toast.makeText(MainActivity.this, R.string.no_next_song, Toast.LENGTH_SHORT).show();
        }
    }

    private void previous() {
        listPosition = listPosition - 1;
        if (listPosition >= 0) {
            MusicInfo musicInfo = musicInfos.get(listPosition);
            tvMusicName.setText(musicInfo.getDisplayName());
            tvMusicTime.setText(MediaUtil.formatTime(musicInfo.getDuration()));
            finalProgress.setText(MediaUtil.formatTime(musicInfo.getDuration()));
            Intent intent = new Intent();
            intent.setAction(MUSIC_SERVICE);
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("url", musicInfo.getUrl());
            intent.putExtra("msg", PREVIOUS_MSG);
            startService(intent);
        } else {
            listPosition = 0;
            Toast.makeText(MainActivity.this, R.string.no_previous_song, Toast.LENGTH_SHORT).show();
        }
    }

    private void play() {
        btnPlay.setBackgroundResource(R.mipmap.action_play_normal);
        MusicInfo musicInfo = musicInfos.get(listPosition);
        tvMusicName.setText(musicInfo.getDisplayName());
        tvMusicTime.setText(MediaUtil.formatTime(musicInfo.getDuration()));
        finalProgress.setText(MediaUtil.formatTime(musicInfo.getDuration()));
        Intent intent = new Intent();
        intent.setAction(MUSIC_SERVICE);
        intent.putExtra("listPosition", 0);
        intent.putExtra("url", musicInfo.getUrl());
        intent.putExtra("msg", PLAY_MSG);
        startService(intent);
    }


    @Override
    protected void onDestroy() {
        exit();
        unregisterReceiver(mainReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.exit_menu) {
            exitPrompt();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exitPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("确定退出？")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exit();
                    }
                }).create().show();
    }

    private void exit() {
        Intent intent = new Intent(MainActivity.this, MyService.class);
        stopService(intent);
        finish();
    }

    // 接收从MyService发送过来的广播
    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UPDATE_ACTION)) {
                // 更新列表位置
                listPosition = intent.getIntExtra("current", -1);
                if (listPosition >= 0) {
                    tvMusicName.setText(musicInfos.get(listPosition).getDisplayName());
                    tvMusicTime.setText(MediaUtil.formatTime(musicInfos.get(listPosition).getDuration()));
                    finalProgress.setText(MediaUtil.formatTime(musicInfos.get(listPosition).getDuration()));
                }
            } else if (action.equals(MUSIC_DURATION)) {
                duration = intent.getIntExtra("duration", -1); //时长
                musicProgress.setMax(duration);
                finalProgress.setText(MediaUtil.formatTime(duration));
            } else if (action.equals(MUSIC_CURRENT)) {
                currentTime = intent.getIntExtra("currentTime", -1);
                currentProgress.setText(MediaUtil.formatTime(currentTime));
                musicProgress.setProgress(currentTime);
            }
        }
    }

    // 单击列表项播放歌曲
    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            listPosition = i;
            if (musicInfos != null) {
                MusicInfo musicInfo = musicInfos.get(listPosition);
                Log.i(TAG, "MusicInfo: "+musicInfo.toString());
                tvMusicName.setText(musicInfos.get(listPosition).getDisplayName());
                tvMusicTime.setText(MediaUtil.formatTime(musicInfos.get(listPosition).getDuration()));
                finalProgress.setText(MediaUtil.formatTime(musicInfos.get(listPosition).getDuration()));
                Intent intent = new Intent();
                intent.setAction(MUSIC_SERVICE);
                intent.putExtra("listPosition", listPosition);
                intent.putExtra("url", musicInfo.getUrl());
                intent.putExtra("msg", PLAY_MSG);
                startService(intent);
            }
        }
    }

    /**
     * 拖动滑动条控制播放进度
     */
    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            switch (seekBar.getId()) {
                case R.id.sbMusicProgress:
                    if (b) {
                        audioTrackChange(i); // 用户控制播放进度
                        Log.i(TAG, "onProgressChanged: proress--"+i);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private void audioTrackChange(int progress) {
        Intent intent = new Intent();
        intent.setAction(MUSIC_SERVICE);
        intent.putExtra("url", musicInfos.get(listPosition).getUrl());
        intent.putExtra("listPosition", listPosition);
        intent.putExtra("msg", PROGRESS_CHANGE);
        intent.putExtra("progress", progress);
        startService(intent);
    }
}

