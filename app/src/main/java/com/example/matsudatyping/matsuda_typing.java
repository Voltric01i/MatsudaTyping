package com.example.matsudatyping;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.sax.StartElementListener;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.RandomAccess;

public class matsuda_typing extends AppCompatActivity {

    final int QUIZ_DEADTIME = 3;
    final int QUIZ_TOTALTIME = 60;
    final static int port = 10000;

    int quizTime = QUIZ_TOTALTIME;
    int missCount = 3;
    int clearCount = 0;

    TextView QuizBoxJapanese;
    TextView QuizBoxRoman;
    TextView RemainingTime;

    Handler quizTimeHandler;
    Runnable qtr;
    Handler GameTimeHandler;
    Runnable gtr;

    String keyPressed = null;

    Matsuda quiz[] = {
            new Matsuda("あれは寿司だ","arehasushida"),
            new Matsuda("進捗どうですか","shintyokudoudesuka"),
            new Matsuda("こたつ","kotatsu"),
            new Matsuda("お掃除ロボット","osouzirobotto" ),
            new Matsuda("魂を燃やす","tamasiiwomoyasu" )};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matsuda_typing);
        QuizBoxJapanese = findViewById(R.id.text_type_japanese);
        QuizBoxRoman = findViewById(R.id.text_type_roman);
        RemainingTime = findViewById(R.id.remainingTimeText);

    }


    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
        udpReceive();

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

    public static class startDialog extends DialogFragment {

        private AlertDialog dialog ;
        private AlertDialog.Builder alert;
        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            alert = new AlertDialog.Builder(getActivity());
            // カスタムレイアウトの生成
            final View alertView = getActivity().getLayoutInflater().inflate(R.layout.dialog_start, null);
            new Thread() {
                @Override
                public void run(){
                    TextView textDescription = alertView.findViewById(R.id.description_text2);
                    textDescription.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.cancel();
                                    ((matsuda_typing) getActivity()).startGame();
                                }
                            }
                    );

                    try {
                        //waiting = trueの間、ブロードキャストを受け取る
                        while(!Thread.currentThread().isInterrupted()){
                            //受信用ソケット
                            DatagramSocket receiveUdpSocket = new DatagramSocket(port);
                            byte[] buf = new byte[5];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);

                            receiveUdpSocket.receive(packet);
                            //受信バイト数取得
                            int length = packet.getLength();
                            String keyPressed = new String(buf, 0, length);
                            if((keyPressed.equals(" ")) || (keyPressed.equals("\n"))){
                                dialog.cancel();
                                ((matsuda_typing) getActivity()).startGame();
                            }

                            receiveUdpSocket.close();
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            // ViewをAlertDialog.Builderに追加
            alert.setView(alertView);

            // Dialogを生成
            dialog = alert.create();
            dialog.show();

            return dialog;
        }
    }

    public static class EndDialog extends DialogFragment {

        private AlertDialog dialog ;
        private AlertDialog.Builder alert;
        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            alert = new AlertDialog.Builder(getActivity());
            // カスタムレイアウトの生成
            final View alertView = getActivity().getLayoutInflater().inflate(R.layout.dialog_end, null);
            new Thread() {
                @Override
                public void run(){
                    TextView textDescription = alertView.findViewById(R.id.description_text2);
                    textDescription.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    matsuda_typing ma_typing= (matsuda_typing) getActivity();
                                    ma_typing.reload();
                                }
                            }
                    );

                    try {
                        //waiting = trueの間、ブロードキャストを受け取る
                        while(!Thread.currentThread().isInterrupted()){
                            //受信用ソケット
                            DatagramSocket receiveUdpSocket = new DatagramSocket(port);
                            byte[] buf = new byte[5];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);

                            receiveUdpSocket.receive(packet);
                            //受信バイト数取得
                            int length = packet.getLength();
                            String keyPressed = new String(buf, 0, length);
                            if((keyPressed.equals(" ")) || (keyPressed.equals("\n"))){
                                dialog.cancel();
                                ((matsuda_typing) getActivity()).startGame();
                            }

                            receiveUdpSocket.close();
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            // ViewをAlertDialog.Builderに追加
            alert.setView(alertView);

            // Dialogを生成
            dialog = alert.create();
            dialog.show();

            return dialog;
        }
    }


    public void changeSushiImage(){

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
                        readNewQuiz();
                        missCount--;
                        //MISS
                    }else  if (QuizBoxRoman.length() == 0){
                        quizTimeCount = QUIZ_DEADTIME;
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
        if(typeText.equals(QuizBoxRoman.getText().toString().substring(0,1)) ){
            QuizBoxRoman.setText(quiz[1].roman.substring(1,QuizBoxRoman.length()));
        }
        if (QuizBoxRoman.length() == 0){
            readNewQuiz();
        }
    }

    public void readNewQuiz(){
        Random random = new Random();
        int quizNum = random.nextInt(quiz.length);
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
                        byte[] buf = new byte[5];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);

                        receiveUdpSocket.receive(packet);
                        //受信バイト数取得
                        int length = packet.getLength();
                        keyPressed = new String(buf, 0, length);
                        onTypeText(keyPressed);

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
