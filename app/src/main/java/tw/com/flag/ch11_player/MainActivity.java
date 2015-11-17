package tw.com.flag.ch11_player;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener{

    Uri uri; //儲存影音檔案的uri
    TextView txvName,txvUri;
    boolean isVideo = false; //紀錄是否為影片檔(否則音樂檔)
    Button btnPlay,btnStop;
    CheckBox ckbLoop;
    MediaPlayer mper;
    Toast tos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       //設定螢幕不隨手機選轉,以及畫面直向顯示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR); //螢幕不隨手機旋轉
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //螢幕直向顯示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 螢幕不進入休眠

        txvName = (TextView) findViewById(R.id.txvName);
        txvUri = (TextView) findViewById(R.id.txvUri);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnStop = (Button) findViewById(R.id.btnStop);
        ckbLoop = (CheckBox) findViewById(R.id.ckbLoop);

        uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.welcome);

        txvName.setText("welcome.mp3");
        txvUri.setText("程式內的樂曲:" + uri.toString());

        mper = new MediaPlayer();
        mper.setOnPreparedListener(this);
        mper.setOnErrorListener(this);
        mper.setOnCompletionListener(this);
        tos = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        prepareMusic();
    }

    void prepareMusic() {
        btnPlay.setText("播放");
        btnPlay.setEnabled(false);//使播放紐不能按(要等準備好才能按)
        btnStop.setEnabled(false);//使停止鈕不能按

        try {
            mper.reset(); // 如果之前有播放過歌,必須reset後才能換歌
            mper.setDataSource(this, uri); //指定音樂來源
            mper.setLooping(ckbLoop.isChecked());
            mper.prepareAsync(); //要求MediaPlayer準備播放指定的音樂
        } catch (Exception e) { // 攔截錯誤並顯示訊息
            tos.setText("指定音樂檔錯誤!" + e.toString());
            tos.show();
        }
    }

    protected void onPick(View v) {
        Intent it = new Intent(Intent.ACTION_GET_CONTENT);
        if (v.getId() == R.id.btnPickAudio) {
            it.setType("audio/*");
            startActivityForResult(it, 100);
        } else {
            it.setType("video/*");
            startActivityForResult(it,101);
        }
    }

    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode,resCode,data);

        if (resCode == Activity.RESULT_OK) {
            isVideo = (reqCode == 101);

            uri = convertUri(data.getData());//取得選取檔案的Uri並做Uri格式轉換

            txvName.setText((isVideo ? "影片:" : "歌曲") + uri.getLastPathSegment());//顯示檔名(Uri最後一段文字)
            txvUri.setText("檔案位置:" + uri.getPath());
            if(!isVideo)prepareMusic();
        }
    }

    Uri convertUri(Uri uri) { //將"content://"類型的Uri轉換為"file://"的Uri
        if (uri.toString().substring(0, 7).equals("content")) {
            String[] colName = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(uri, colName, null, null, null);
            cursor.moveToFirst();
            uri = Uri.parse("file://" + cursor.getString(0));
            cursor.close();
        }
        return uri;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {//當音樂播放完畢時
        mper.seekTo(0);//將播放位置歸0
        btnPlay.setText("播放");
        btnStop.setEnabled(false);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        tos.setText("發生錯誤,停止播放");
        tos.show();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        btnPlay.setEnabled(true); //當準備好時,讓播放紐有作用
    }

    public void onMpPlay(View v) {
        if (isVideo) {
            Intent it = new Intent(this, Video.class); // 建立開啟Video Activity的Intent
            it.putExtra("uri", uri.toString());//將影片的Uri以"uri"為名加入Intent中
            startActivity(it);
            return;
        }

        if (mper.isPlaying()) { //如果正在播放就暫停
            mper.pause();
            btnPlay.setText("繼續");
        } else { //如果沒有在播放就開始播
            mper.start();
            btnPlay.setText("暫停");
            btnStop.setEnabled(true);
        }
    }

    public void onMpStop(View v) {
        mper.pause();
        mper.seekTo(0);
        btnPlay.setText("播放");
        btnStop.setEnabled(false);
    }

    public void onMpLoop(View v) {
        if (ckbLoop.isChecked()) {
            mper.setLooping(true);
        } else {
            mper.setLooping(false);
        }
    }

    public void onMpInfo() {
        if (!btnPlay.isEnabled()) return;

        int len = mper.getDuration();
        int pos = mper.getCurrentPosition();
        tos.setText("目前播放位置:" + pos/1000 + "/" + len/1000);
        tos.show();
    }

    public void onMpBackward(View v) {
        if(!btnPlay.isEnabled()) return;

        int len = mper.getDuration();
        int pos = mper.getCurrentPosition();
        pos -= 10000;
        if(pos < 0) pos = 0;
        mper.seekTo(pos);
        tos.setText("倒退10秒:" + pos / 1000 + "/" + len / 1000);
        tos.show();
    }

    public void onMpForward(View v) {
        if(!btnPlay.isEnabled()) return; // 如果還沒準備好(播放紐不能按),則不處理

        int len = mper.getDuration(); //讀取音樂長度
        int pos = mper.getCurrentPosition(); //讀取目前播放位置
        pos += 10000; //前進10秒(10000ms )
        if(pos > len) pos = len; //不可大於總秒數
        mper.seekTo(pos); // 移動播放位置
        tos.setText("前進10秒:" + pos/1000 + "/" + len/1000);
        tos.show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mper.isPlaying()) {
            btnPlay.setText("繼續");
            mper.pause();
        }
    }

    @Override
    protected void onDestroy() {
        mper.release();
        super.onDestroy();
    }

}
