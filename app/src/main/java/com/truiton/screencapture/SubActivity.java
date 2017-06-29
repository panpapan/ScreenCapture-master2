package com.truiton.screencapture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SubActivity extends Activity {

    private final int REQUEST_PERMISSION = 1000;

    private TextView textView;
    private EditText editText;
    private String fileName = "hello.txt";
    private Button buttonSave, buttonRead;
    private String text = "no string";

    private String outputFilePath;

    private int from;
    private int to;
    private MediaPlayer mp;

    private int length;

    private Button button;
    private Runnable r;

    private String filePath;

    private Timer timer;
    private CountUpTimerTask timerTask = null;
    private Handler handler = new Handler();
    private long count = 0;

    private TextView timerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        timerText = (TextView)findViewById(R.id.timer);
        timerText.setText("00:00.0");

        Intent intent = getIntent();
        filePath = intent.getStringExtra("FilePass");

        final EditText editText = (EditText) findViewById(R.id.editText);
        final EditText editText2 = (EditText) findViewById(R.id.editText2);


        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Android 6, API 23以上でパーミッシンの確認
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission();
                } else {
                    setUpReadWriteExternalStorage();
                }
            }
        });


        findViewById(R.id.trimview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*String outputFilePath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getPath()+"/output_crop.mp4";*/

                mp = new MediaPlayer();


                FileInputStream fs = null;
                FileDescriptor fd = null;

                try {
                    fs = new FileInputStream(outputFilePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fd = fs.getFD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mp.setDataSource(fd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                length = mp.getDuration();
                mp.release();

                /*final TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(String.valueOf(length));*/



                VideoView videoView = (VideoView) findViewById(R.id.video);
                videoView.setVideoPath(outputFilePath);
                videoView.start();

                if(null != timer){
                    timer.cancel();
                    timer = null;
                    //timerText.setText("00:00.0");
                }

                // Timer インスタンスを生成
                timer = new Timer();

                // TimerTask インスタンスを生成
                timerTask = new CountUpTimerTask();

                // スケジュールを設定 100msec
                // public void schedule (TimerTask task, long delay, long period)
                timer.schedule(timerTask, 0, 100);

                // カウンター
                count = 0;
                timerText.setText("00:00.0");
            }
        });

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String stringfrom = editText.getText().toString();
                from = Integer.parseInt(stringfrom);

                String stringto = editText2.getText().toString();
                to = Integer.parseInt(stringto);

                crop();
            }
        });

        findViewById(R.id.return1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private boolean crop() {
        try {
            // オリジナル動画を読み込み
            //String filePath = Environment.getExternalStorageDirectory() + "/sample1.mp4";
            /*String filePath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).getPath()+"/happy/test1002739393.mp4";*/
            Movie originalMovie = MovieCreator.build(filePath);

            // 分割
            Track track = originalMovie.getTracks().get(0);
            Movie movie = new Movie();
            movie.addTrack(new AppendTrack(new CroppedTrack(track, from*25, to*25)));

            // 出力
            Container out = new DefaultMp4Builder().build(movie);

            File mfile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "trimming");
            if (!mfile.mkdirs()) {
                //Log.e(LOG_TAG, "Directory not created");
            }
            File file = File.createTempFile("test",".mp4",mfile);

            outputFilePath = file.toString();

            /*outputFilePath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).getPath()+"/output_crop.mp4";*/
            FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
            out.writeContainer(fos.getChannel());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setUpReadWriteExternalStorage() {

        if (isExternalStorageReadable()) {
            mp = new MediaPlayer();


            FileInputStream fs = null;
            FileDescriptor fd = null;

            try {
                fs = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fd = fs.getFD();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mp.setDataSource(fd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            length = mp.getDuration();
            mp.release();

            /*final TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(String.valueOf(length));*/


            VideoView videoView = (VideoView) findViewById(R.id.video);
            videoView.setVideoPath(filePath);
            videoView.start();


            if(null != timer){
                timer.cancel();
                timer = null;
                //timerText.setText("00:00.0");
            }

            // Timer インスタンスを生成
            timer = new Timer();

            // TimerTask インスタンスを生成
            timerTask = new CountUpTimerTask();

            // スケジュールを設定 100msec
            // public void schedule (TimerTask task, long delay, long period)
            timer.schedule(timerTask, 0, 100);

            // カウンター
            count = 0;
            timerText.setText("00:00.0");
        }

    }

    class CountUpTimerTask extends TimerTask {
        @Override
        public void run() {
            // handlerを使って処理をキューイングする
            handler.post(new Runnable() {
                public void run() {
                    count++;
                    long mm = count*100 / 1000 / 60;
                    long ss = count*100 / 1000 % 60;
                    long ms = (count*100 - ss * 1000 - mm * 1000 * 60)/100;
                    // 桁数を合わせるために02d(2桁)を設定
                    timerText.setText(String.format("%1$02d:%2$02d.%3$01d", mm, ss, ms));
                    if(count > length/100) {
                        timer.cancel();
                    }
                }
            });
        }
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    // Checks if external storage is available to at least read
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    // permissionの確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            setUpReadWriteExternalStorage();
        }
        // 拒否していた場合
        else{
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(SubActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this, "アプリ実行に許可が必要です", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpReadWriteExternalStorage();
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "何もできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    /*class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            handler.post( new Runnable() {
                public void run() {
                    count++;
                    textView.setText(String.valueOf(count));
                }
            });
        }
    }*/
}
