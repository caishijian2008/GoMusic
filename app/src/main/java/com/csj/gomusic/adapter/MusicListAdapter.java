package com.csj.gomusic.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.csj.gomusic.R;
import com.csj.gomusic.entity.MusicInfo;
import com.csj.gomusic.utils.MediaUtil;

import java.util.List;

/**
 * Created by caishijian on 16-4-20.
 */
public class MusicListAdapter extends BaseAdapter {

    private static final String TAG = "main";
    private Context context;
    private List<MusicInfo> musicInfos;
    private MusicInfo musicInfo;

    public MusicListAdapter(Context context, List<MusicInfo> musicInfos) {
        this.context = context;
        this.musicInfos = musicInfos;
    }

    @Override
    public int getCount() {
        return musicInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return musicInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = View.inflate(context, R.layout.item_song, null);
            holder = new ViewHolder();
            holder.tvName = (TextView) view.findViewById(R.id.tvName);
            holder.tvArtist = (TextView) view.findViewById(R.id.tvArtist);
            holder.tvMusicTime = (TextView) view.findViewById(R.id.tvMusicTime);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        //数据填充
        musicInfo = musicInfos.get(i);
        holder.tvName.setText(musicInfo.getDisplayName());
        holder.tvArtist.setText(musicInfo.getArtist());
        holder.tvMusicTime.setText(MediaUtil.formatTime(musicInfo.getDuration())); //显示时长
        return view;
    }

    static class ViewHolder {
        TextView tvName, tvArtist, tvMusicTime;
    }
}
