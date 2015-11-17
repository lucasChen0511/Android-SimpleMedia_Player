package tw.com.flag.ch11_player;

import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.VideoView;

public class Video extends AppCompatActivity implements MediaPlayer.OnCompletionListener{

    VideoView vdv;
    int pos = 0; //用來記錄前次的播放位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隱藏系統的狀態列
        getSupportActionBar().hide(); // 隱藏Activity的標題列
        setContentView(R.layout.activity_video); // 以上2項設定必須在本方法之前呼叫
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持螢幕一直開著不會自動休眠

        Intent it = getIntent(); // 取得傳入的Intent物件
        Uri uri = Uri.parse(it.getStringExtra("uri"));// 取出要撥放影片的Uri
        if (savedInstanceState != null) // 如果是因為旋轉而重新啟動的Activity
            pos = savedInstanceState.getInt("pos", 0); // 取出旋轉前所儲存的播放位置

        vdv = (VideoView) findViewById(R.id.videoView);
        MediaController mediaCtrl = new MediaController(this); // 建立播放控制物件
        vdv.setMediaController(mediaCtrl);
        vdv.setVideoURI(uri);
        vdv.setOnCompletionListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        vdv.seekTo(pos);
        vdv.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pos = vdv.getCurrentPosition();
        vdv.stopPlayback();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos",pos); // 將onPause()中所取得的播放位置儲存到bundle
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }
}
