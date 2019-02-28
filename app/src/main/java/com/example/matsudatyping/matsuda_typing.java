package com.example.matsudatyping;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

    int port = 10000;
    TextView QuizBoxJapanese;
    TextView QuizBoxRoman;
    TextView RemainingTime;
    int quizTime = QUIZ_TOTALTIME;
    int missCount = 3;
    int clearCount = 0;

    Handler quizTimeHandler;
    Runnable qtr;
    Handler GameTimeHandler;
    Runnable gtr;



    Matsuda quiz[] = {
            new Matsuda("寿司","sushi"),
            new Matsuda("進捗どうですか","shintyokudoudesuka"),
            new Matsuda("こたつ","kotatsu"),
            new Matsuda("お掃除ロボット","osouzirobotto" )};

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
        udpReceive();
        startGame();

    }

    public void startGame(){

        GameTimeHandler = new Handler();
        gtr = new Runnable(){
            @Override
            public void run() {
                RemainingTime.setText(String.format("%02d", quizTime));
                if(quizTime == 0){
                    endPopUp();
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
                        String keyCode = new String(buf, 0, length);
                        onTypeText(keyCode);

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
