package com.csj.gomusic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.csj.gomusic.entity.MusicInfo;
import com.csj.gomusic.utils.MediaUtil;

import java.io.IOException;
import java.util.List;

public class MyService extends Service {

    public static final String TAG = "main";

    public static final int PLAY_MSG = 1;		//播放
    public static final int PAUSE_MSG = 2;		//暂停
    public static final int STOP_MSG = 3;		//停止
    public static final int CONTINUE_MSG = 4;	//继续
    public static final int PREVIOUS_MSG = 5;	//上一首
    public static final int NEXT_MSG = 6;		//下一首
    public static final int PROGRESS_CHANGE = 7;//进度改变
    public static final int PLAYING_MSG = 8;	//正在播放

    private int currentTime;		//当前播放进度
    private int duration;			//播放长度

    private MediaPlayer mediaPlayer = null; // 媒体播放器对象
    private String path; 			// 音乐文件路径
    private int msg;				//播放信息
    private boolean isPause; 		// 暂停状态
    private int current; 		// 记录当前正在播放的音乐
    private List<MusicInfo> musicInfos; // 存放MusicInfo对象的集合

    private int status = 3; //播放状态，默认为顺序播放
    private MyBroadcastReceiver myReceiver; //自定义的广播接收器，接收播放状态

    //服务要发送的一些Action
    public static final String UPDATE_ACTION = "com.csj.action.UPDATE_ACTION";	//更新动作
    public static final String CTL_ACTION = "com.csj.action.CTL_ACTION";		//控制动作
    public static final String MUSIC_CURRENT = "com.csj.action.MUSIC_CURRENT";	//当前音乐播放时间更新动作
    public static final String MUSIC_DURATION = "com.csj.action.MUSIC_DURATION";//新音乐长度更新动作

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (mediaPlayer != null) {
                    currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
                    Intent intent = new Intent();
                    intent.setAction(MUSIC_CURRENT);
                    intent.putExtra("currentTime", currentTime);
                    sendBroadcast(intent); // 给MainActivity发送广播
                    handler.sendEmptyMessageDelayed(1, 1000); // 每一秒进度自动拖动拖条
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        try {
            musicInfos = MediaUtil.getMusicInfos(MyService.this);
        } catch (Exception e) {
            e.printStackTrace();
            Looper.prepare();
            Toast.makeText(MyService.this, "not found songs!", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }

        //设置音乐播放完成时的监听器
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (status == 1) { //单曲循环
                    mediaPlayer.start();
                } else if (status == 2) { //全部循环
                    current++;
                    if (current > musicInfos.size() - 1) { //变为第一首重新播放
                        current = 0;
                    }
                    Intent intent = new Intent(UPDATE_ACTION);
                    intent.putExtra("current", current);
                    // 发送广播，被MainActivity的BroadcastReceiver接收到
                    sendBroadcast(intent);
                    path = musicInfos.get(current).getUrl();
                    play(0);
                } else if (status == 3) { //顺序播放
                    current++;
                    if (current <= musicInfos.size() - 1) {
                        Intent intent = new Intent(UPDATE_ACTION);
                        intent.putExtra("current", current);
                        // 发送广播，被MainActivity的BroadcastReceiver接收到
                        sendBroadcast(intent);
                        path = musicInfos.get(current).getUrl();
                        play(0);
                    } else {
                        mediaPlayer.seekTo(0);
                        current = 0;
                        Intent intent = new Intent(UPDATE_ACTION);
                        intent.putExtra("current", current);
                        // 发送广播，被MainActivity的BroadcastReceiver接收到
                        sendBroadcast(intent);
                    }
                } else if (status == 4) { //随机播放
                    current = getRandomIndex(musicInfos.size() - 1);
                    Intent intent = new Intent(UPDATE_ACTION);
                    intent.putExtra("current", current);
                    // 发送广播，被MainActivity的BroadcastReceiver接收到
                    sendBroadcast(intent);
                    path = musicInfos.get(current).getUrl();
                    play(0);
                }
            }
        });

        myReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CTL_ACTION);
        registerReceiver(myReceiver, filter);


    }

    private int getRandomIndex(int i) {
        return (int) (Math.random() * i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        path = intent.getStringExtra("url");		//歌曲路径
        Log.i(TAG, "onStartCommand: "+path);
        current = intent.getIntExtra("listPosition", -1);	//当前播放的歌曲在musicInfos的位置
        msg = intent.getIntExtra("msg", 0);			//播放信息
        if (msg == PLAY_MSG) {	//直接播放音乐
            play(0);
        } else if (msg == PAUSE_MSG) {	//暂停
            pause();
        } else if (msg == STOP_MSG) {		//停止
            stop();
        } else if (msg == CONTINUE_MSG) {	//继续播放
            resume();
        } else if (msg == PREVIOUS_MSG) {	//上一首
            previous();
        } else if (msg == NEXT_MSG) {		//下一首
            next();
        } else if (msg == PROGRESS_CHANGE) {	//进度更新
            currentTime = intent.getIntExtra("progress", -1);
            play(currentTime);
        }

//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(MyService.this, MainActivity.class), 0);
//        builder.setContentIntent(pendingIntent)
//                .setAutoCancel(false)
//                .setTicker(musicInfos.get(current).getDisplayName())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle(musicInfos.get(current).getDisplayName())
//                .setContentText(MediaUtil.formatTime(musicInfos.get(current).getDuration()));
//        startForeground(1, builder.build()); //设置为前台服务

        return super.onStartCommand(intent, flags, startId);
    }

    private void next() {
        Intent intent = new Intent(UPDATE_ACTION);
        intent.putExtra("current", current);
        // 发送广播，将被MainActivity中的BroadcastReceiver接收到
        sendBroadcast(intent);
        play(0);
    }

    private void previous() {
        Intent intent = new Intent(UPDATE_ACTION);
        intent.putExtra("current", current);
        // 发送广播，将被MainActivity中的BroadcastReceiver接收到
        sendBroadcast(intent);
        play(0);
    }

    private void resume() {
        if (isPause) {
            mediaPlayer.start();
            isPause = false;
        }
    }

    private void stop() {

    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            isPause = true;
        }

    }

    private void play(int currentTime) {

        try {
            mediaPlayer.reset(); // 把各项参数恢复到初始状态
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare(); // 进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));// 注册一个监听器
            handler.sendEmptyMessage(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int currentTime;

        public PreparedListener(int currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start(); // 开始播放
            if (currentTime > 0) { // 如果音乐不是从头播放
                mediaPlayer.seekTo(currentTime);
            }

            //发送播放进度广播
            Intent intent = new Intent();
            intent.setAction(MUSIC_DURATION);
            duration = mediaPlayer.getDuration();
            intent.putExtra("duration", duration);	//通过Intent来传递歌曲的总长度
            sendBroadcast(intent);
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control", -1);
            switch (control) {
                case 1:
                    status = 1; // 播放状态为1表示：单曲循环
                    break;
                case 2:
                    status = 2; // 播放状态为2表示：全部循环
                    break;
                case 3:
                    status = 3; // 播放状态为3表示：顺序循环
                    break;
                case 4:
                    status = 4; // 播放状态为4表示：随机循环
                    break;

            }
        }
    }

}
