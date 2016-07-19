package com.csj.gomusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.csj.gomusic.entity.MusicInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caishijian on 16-4-29.
 */
public class MediaUtil {

    private static final String TAG = "main";

    /**
     * 从数据库中查询音乐信息，并保存到List集合
     * @param context
     * @return 音乐信息的集合
     */
    public static List<MusicInfo> getMusicInfos(Context context) throws Exception {
        Cursor cursor = null;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        Log.i(TAG, "cursor.getCount(): --"+cursor.getCount());
        if (cursor.getCount() < 0) {
            throw new Exception("没有发现歌曲哦！");
        }

        List<MusicInfo> musicInfos = new ArrayList<MusicInfo>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            MusicInfo musicInfo = new MusicInfo();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)); //文件id
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)); //文件标题
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)); //艺术家
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)); //专辑
            String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)); //文件显示名称
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)); //文件路径
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)); //专辑id
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); //文件时长
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)); //文件大小
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); //是否为音乐
            if(isMusic != 0) { //把音乐添加到集合中
                musicInfo.setId(id);
                musicInfo.setTitle(title);
                musicInfo.setArtist(artist);
                musicInfo.setAlbum(album);
                musicInfo.setDisplayName(displayName);
                musicInfo.setUrl(url);
                musicInfo.setAlbumId(albumId);
                musicInfo.setDuration(duration);
                musicInfo.setSize(size);
                musicInfos.add(musicInfo);
            }
        }

        return musicInfos;
    }

    /**
     * 格式化时间，把毫秒转换为“分:秒”格式
     * @param time
     * @return
     */
    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }
}
