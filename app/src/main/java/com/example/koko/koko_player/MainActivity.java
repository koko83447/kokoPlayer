package com.example.koko.koko_player;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    Uri uri;
    TextView txvName,txvSb;
    boolean isVideo = false;

    Button btnPlay, btnStop;
    CheckBox ckbLoop;
    MediaPlayer mper;
    Toast tos;

    SeekBar sb;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txvName = (TextView)findViewById(R.id.txvName);
        //txvUri = (TextView)findViewById(R.id.txvUri);
        btnPlay = (Button)findViewById(R.id.btnPlay);
        btnStop = (Button)findViewById(R.id.btnStop);
        ckbLoop = (CheckBox)findViewById(R.id.ckbLoop);

        uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.welcome);

        txvName.setText("welcome.mp3");
        //txvUri.setText("檔案路徑:" + uri.toString());

        mper = new MediaPlayer();
        mper.setOnPreparedListener(this);
        mper.setOnErrorListener(this);
        mper.setOnCompletionListener(this);
        tos = Toast.makeText(this, "",Toast.LENGTH_SHORT);

        prepareMedia();

        handler = new Handler();
        sb = (SeekBar)findViewById(R.id.MusicSeekBar);
        txvSb = (TextView)findViewById(R.id.txvSb);
        //mper.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //sb.setMax(mper.getDuration());


        seebar();
        //playCycle();
    }


    void prepareMedia(){
        btnPlay.setText("Play");
        btnPlay.setEnabled(false);
        btnStop.setEnabled(false);

        try{
            mper.reset();
            mper.setDataSource(this, uri);
            mper.setLooping(ckbLoop.isChecked());
            mper.prepareAsync();

        }catch (Exception e){
            tos.setText("指定音樂檔錯誤!"+e.toString());
            tos.show();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mper.seekTo(0);
        btnPlay.setText("Play");
        btnStop.setEnabled(false);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        tos.setText("發生錯誤停止撥放");
        tos.show();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        btnPlay.setEnabled(true);
        sb.setMax(mper.getDuration());
        playCycle();
    }


    public void onPick(View v){
        Intent it = new Intent(Intent.ACTION_GET_CONTENT);

        if (v.getId() == R.id.btnPickAudio){
            it.setType("audio/*");
            startActivityForResult(it,100);
        }
        else {
            it.setType("video/*");
            startActivityForResult(it,101);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){
            isVideo = (requestCode == 101);

            uri = data.getData();
            txvName.setText(getFilename(uri));
            //txvUri.setText("檔案 URI:" + uri.toString());
            prepareMedia();
            }
        }

    String getFilename(Uri uri){
        String fileName = null;
        String[] colName = {MediaStore.MediaColumns.DISPLAY_NAME};

        Cursor cursor = getContentResolver().query(uri,colName, null, null, null);
        cursor.moveToFirst();
        fileName = cursor.getString(0);
        cursor.close();
        return fileName;
    }


        public void onMpPlay(View v){

            if (isVideo){
                Intent it = new Intent(this, Video.class);
                it.putExtra("uri", uri.toString());
                startActivity(it);
                return;
            }

            if (mper.isPlaying()){
                mper.pause();
                btnPlay.setText("Goon");

            }else {
                mper.start();
                btnPlay.setText("Pause");
                btnStop.setEnabled(true);
            }
        }

        public void onMpStop(View v){
            mper.pause();
            mper.seekTo(0);
            btnPlay.setText("Play");
            btnStop.setEnabled(false);
        }

        public void onMpLoop(View v){
            if (ckbLoop.isChecked())
                mper.setLooping(true);
            else mper.setLooping(false);
        }

        public void onMpBackward(View v){
            if (!btnPlay.isEnabled())return;
            int len = mper.getDuration();
            int pos = mper.getCurrentPosition();
            pos -= 10000;
            if (pos<0) pos = 0;
            mper.seekTo(pos);
            tos.setText("倒退10秒:" + pos/1000 +"/" + len/1000);
            tos.show();
        }

        public void onMpForward(View v){
            if (!btnPlay.isEnabled()) return;
            int len = mper.getDuration();
            int pos = mper.getCurrentPosition();
            pos += 10000;
            if (pos > len) pos = len;
            mper.seekTo(pos);
            tos.setText("前進10秒:" + pos/1000 +"/" + len/1000);
            tos.show();
        }

        @Override
        protected void onPause(){
            super.onPause();

            if (mper.isPlaying()){
                btnPlay.setText("Goon");
                mper.pause();
            }
        }
        @Override
        protected void onDestroy(){
            mper.release();
            super.onDestroy();
            handler.removeCallbacks(runnable);
        }
        @Override
        protected void onStop(){
            super.onStop();
            if (mper.isPlaying()){
                btnPlay.setText("Goon");
                mper.pause();
            }
        }

        /*public void onMplnfo(View v){
            if (!btnPlay.isEnabled())return;
            int len = mper.getDuration();
            int pos = mper.getCurrentPosition();
            tos.setText("目前撥放位置:" + pos/1000 +"/" + len/1000);
            tos.show();
        }*/

    public void seebar(){

        sb.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener(){
                    int progress_value;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                        if (input){
                            mper.seekTo(progress);
                        }
                        txvSb.setText("位置: " + progress/1000 + " / " +sb.getMax()/1000);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        txvSb.setText("位置: " + progress_value/1000 + " / " +sb.getMax()/1000);
                    }
                }
        );
    }
    public void playCycle(){
        sb.setProgress(mper.getCurrentPosition());
        /*if (mper.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                playCycle();
                }
            };
            handler.postDelayed(runnable, 100);
        }*/
        if (btnPlay.isEnabled()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 100);
        }
    }



}
