package com.example.matsudatyping;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.sax.StartElementListener;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.RandomAccess;

public class matsuda_typing extends AppCompatActivity {

    final int QUIZ_DEADTIME = 20;
    final int QUIZ_TOTALTIME = 180;
    final int SET_MONNY = 3000;
    final static int port = 34512;


    int quizTime = QUIZ_TOTALTIME;
    int missCount = 10;
    int clearCount = 0;
    int totalKeyInput = 0;

    int quizNum= 0;
    int itemRand = 0;

    TextView QuizBoxJapanese;
    TextView QuizBoxRoman;
    TextView RemainingTime;
    ImageView SushiImage;
    private Handler handler = new Handler();
    private Handler handlerDismiss = new Handler();

    Handler quizTimeHandler;
    Runnable qtr;
    Handler GameTimeHandler;
    Runnable gtr;

    private static Handler mHandler = new Handler();
    private ProgressBar mProgress;

    String keyPressed = "";

    Matsuda quiz[] = {
            new Matsuda("あれは寿司だ","arehasushida"),
            new Matsuda("進捗どうですか","shintyokudoudesuka"),
            new Matsuda("こたつ","kotatsu"),
            new Matsuda("お掃除ロボット","osouzirobotto" ),
            new Matsuda("魂を燃やす","tamasiiwomoyasu" ),
            new Matsuda("これは松打だ","korehamatudada" ),
            new Matsuda("ヤフー最高","yahu-saikou" ),
            new Matsuda("タイピング練習","taipingurennsyuu" ),
            new Matsuda("お寿司よりピザ","osushiyoripiza" ),
            new Matsuda("ハッピータイピング","happi-taipingu" ),};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matsuda_typing);
        QuizBoxJapanese = findViewById(R.id.text_type_japanese);
        QuizBoxRoman = findViewById(R.id.text_type_roman);
        RemainingTime = findViewById(R.id.remainingTimeText);
        SushiImage = findViewById(R.id.sushiImageView);

    }


    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
        udpReceive();
        readNewQuiz();
        showStartDialog();

    }

    public void showStartDialog(){
        DialogFragment dialogFragment = new startDialog();
        dialogFragment.show(getFragmentManager(),"next");

    }

    public void showEndDialog(){
        DialogFragment dialogFragment = new EndDialog();
        dialogFragment.show(getFragmentManager(),"next");
    }



    public String getKeyPressed() {
        return keyPressed;
    }

    public String getTotalScore(){

        String out = null;
        if(totalKeyInput >= SET_MONNY){
            out = String.valueOf((clearCount * 100) -SET_MONNY) + "円" + " 得しました";
            return out;
        }else {
            out = String.valueOf(-1 * ((clearCount * 100) -SET_MONNY)) + "円" + " 損しました";
            return out;
        }
    }

    public String getRightKeyInput(){
        String out = String.valueOf(clearCount);
        return out;
    }
    public String getAvgKeyInput(){
        String out = String.valueOf((float) totalKeyInput/QUIZ_TOTALTIME);
        return out;
    }

    public static class startDialog extends DialogFragment {

        private AlertDialog dialog ;
        private AlertDialog.Builder alert;


        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);
            Dialog dialog = getDialog();

            //AttributeからLayoutParamsを求める
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();


            //display metricsでdpのもと(?)を作る
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            //LayoutParamsにdpを計算して適用(今回は横幅300dp)(※metrics.scaledDensityの返り値はfloat)
            float dialogWidth = 700 * metrics.scaledDensity;
            layoutParams.width = (int)dialogWidth;

            //LayoutParamsをセットする
            dialog.getWindow().setAttributes(layoutParams);


        }
        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            alert = new AlertDialog.Builder(getActivity());
            // カスタムレイアウトの生成
            final View alertView = getActivity().getLayoutInflater().inflate(R.layout.dialog_start, null);

            // Dialogを生成
            // ViewをAlertDialog.Builderに追加
            alert.setView(alertView);

            dialog = alert.create();
            dialog.show();

            TextView textView = alertView.findViewById(R.id.description_text2);
            textView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                            ((matsuda_typing) getActivity()).startGame();
                        }
                    }
            );


            Thread checkReceive = new Thread(new Runnable() {
                @Override
                public void run() {
                    //Threadが動いてる限り回す
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            // UDPパケット待ち受け
                            final DatagramSocket recvUdpSocket = new DatagramSocket(port);
                            recvUdpSocket.setReuseAddress(true);

                            final byte[] buffer = new byte[2048];
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                            // 受信するまでブロック
                            recvUdpSocket.receive(packet);

                            // 受信したデータをトーストで出力
                             mHandler.post(new Runnable() {
                                public void run() {
                                    try {
                                        String result = new String(buffer, "UTF-8");
                                        dialog.cancel();
                                        ((matsuda_typing) getActivity()).startGame();

                                        recvUdpSocket.close();

                                        //監視しているスレッドを止める
                                    } catch(IOException e) {
                                    } finally {
                                        //ダイアログを消す
                                        dialog.dismiss();
                                    }
                                }
                            });
                        } catch (IOException e) {

                        }
                    }
                }
            });

            return dialog;
        }
    }

    public static class EndDialog extends DialogFragment {

        private AlertDialog dialog ;
        private AlertDialog.Builder alert;
        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);
            Dialog dialog = getDialog();
            TextView textResult = dialog.findViewById(R.id.description_text3);
            TextView textResult1 = dialog.findViewById(R.id.result_right_keyinput);
            TextView textResult2 = dialog.findViewById(R.id.result_avg_keyinput);

            textResult.setText(((matsuda_typing) getActivity()).getTotalScore());
            textResult1.setText(((matsuda_typing) getActivity()).getRightKeyInput());
            textResult2.setText(((matsuda_typing) getActivity()).getAvgKeyInput());

            //AttributeからLayoutParamsを求める
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();

            //display metricsでdpのもと(?)を作る
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            //LayoutParamsにdpを計算して適用(今回は横幅300dp)(※metrics.scaledDensityの返り値はfloat)
            float dialogWidth = 700 * metrics.scaledDensity;
            layoutParams.width = (int)dialogWidth;

            //LayoutParamsをセットする
            dialog.getWindow().setAttributes(layoutParams);

        }
        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            alert = new AlertDialog.Builder(getActivity());

            // カスタムレイアウトの生成
            final View alertView = getActivity().getLayoutInflater().inflate(R.layout.dialog_end, null);


            // ViewをAlertDialog.Builderに追加
            alert.setView(alertView);
            if(((matsuda_typing) getActivity()).getKeyPressed().equals("@")){
                dialog.cancel();
                ((matsuda_typing) getActivity()).startGame();
                ((matsuda_typing) getActivity()).reload();
            }

            // Dialogを生成
            dialog = alert.create();
            dialog.show();

            return dialog;
        }
    }


    public void changeSushiImage(ImageView imageView){

        switch (itemRand){
            case 0:
                imageView.setImageResource(R.drawable.akami);
                break;
            case 1:
                imageView.setImageResource(R.drawable.asi);
                break;
            case 2:
                imageView.setImageResource(R.drawable.chutoro);
                break;
            case 3:
                imageView.setImageResource(R.drawable.ebi);
                break;
            case 4:
                imageView.setImageResource(R.drawable.ikura);
                break;
            case 5:
                imageView.setImageResource(R.drawable.kazunoko);
                break;
            case 6:
                imageView.setImageResource(R.drawable.ootoro);
                break;
            case 7:
                imageView.setImageResource(R.drawable.tako);
                break;
            case 8:
                imageView.setImageResource(R.drawable.tamago);
                break;
            case 9:
                imageView.setImageResource(R.drawable.tekkamaki);
                break;

        }

        if(itemRand >=  9){
            itemRand = 0;
        }else {
            itemRand++;
        }

    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }



    public void startGame(){

        GameTimeHandler = new Handler();
        gtr = new Runnable(){
            @Override
            public void run() {
                RemainingTime.setText(String.format("%02d", quizTime));
                if(quizTime == 0){
                    endPopUp();
                    showEndDialog();
                    return;
                    //END
                }else {
                    quizTime--;
                }
                GameTimeHandler.postDelayed(this, 1000);

            }
        };
        GameTimeHandler.post(gtr);

        quizTimeHandler = new Handler();
        qtr = new Runnable(){
            int quizTimeCount = QUIZ_DEADTIME;
            @Override
            public void run() {
                    if((missCount == 0)){
                        endPopUp();
                        showEndDialog();
                        return;
                        //END
                    }

                    if(quizTimeCount == 0){
                        quizTimeCount = QUIZ_DEADTIME;
                        changeSushiImage(SushiImage);
                        readNewQuiz();
                        missCount--;
                        //MISS
                    }else  if (QuizBoxRoman.length() == 0){
                        quizTimeCount = QUIZ_DEADTIME;
                        changeSushiImage(SushiImage);
                        readNewQuiz();
                        clearCount++;
                        //CLEAR
                    }
                    quizTimeCount--;
                    quizTimeHandler.postDelayed(this, 1000);
            }
        };
        quizTimeHandler.post(qtr);
    }

    private void hideNavigationBar() {
        View sysView = getWindow().getDecorView();
        sysView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public void endPopUp(){
        GameTimeHandler.removeCallbacks(gtr);
        quizTimeHandler.removeCallbacks(qtr);
        Toast toast = Toast.makeText(matsuda_typing.this, "終了", Toast.LENGTH_SHORT);
        toast.show();
        missCount = QUIZ_DEADTIME;
        clearCount = 0;
    }


    public void onTypeText(String typeText ){
        totalKeyInput++;
        if(typeText.equals(QuizBoxRoman.getText().toString().substring(0,1)) ){
            QuizBoxRoman.setText(QuizBoxRoman.getText().toString().substring(1,QuizBoxRoman.length()));
        }
        if (QuizBoxRoman.length() == 0){
            readNewQuiz();
        }
    }

    public void readNewQuiz(){

        if(quizNum >=  quiz.length-1){
            quizNum = 0;
        }else {
            quizNum++;
        }

        QuizBoxJapanese.setText(quiz[quizNum].japanese);
        QuizBoxRoman.setText(quiz[quizNum].roman);


    }


    void udpReceive() {
        new Thread() {
            @Override
            public void run(){
                try {
                    //waiting = trueの間、ブロードキャストを受け取る
                    while(!Thread.currentThread().isInterrupted()){
                        //受信用ソケット
                        DatagramSocket receiveUdpSocket = new DatagramSocket(port);
                        byte[] buf = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);

                        receiveUdpSocket.receive(packet);
                        //受信バイト数取得
                        int length = packet.getLength();
                        Log.d("NormalDialog","received" );
                        keyPressed = new String(buf, 0, length);
                        Log.d("NormalDialog",keyPressed );

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                onTypeText(keyPressed);
                            }
                        });

                        receiveUdpSocket.close();
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

}
