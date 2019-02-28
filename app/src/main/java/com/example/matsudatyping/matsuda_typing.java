package com.example.matsudatyping;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.RandomAccess;

public class matsuda_typing extends AppCompatActivity {

    int port = 10000;
    TextView QuizBoxJapanese;
    TextView QuizBoxRoman;

    Matsuda quiz[] = {
            new Matsuda("寿司","sushi"),
            new Matsuda("進捗どうですか","shintyokudoudesuka")};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matsuda_typing);
        QuizBoxJapanese = findViewById(R.id.text_type_japanese);
        QuizBoxJapanese = findViewById(R.id.text_type_roman);


    }


    @Override
    protected void onResume() {
        super.onResume();
        udpReceive();

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
        Random random = new  Random();
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
