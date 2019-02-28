package com.example.matsudatyping;

public class Matsuda {
    String japanese;
    String roman;

    Matsuda(String japanese,String roman){
        this.japanese = japanese;
        this.roman = roman;
    }

    String getRoman() {
        return this.roman;
    }
    String getJapanese(){
        return  this.japanese;
    }

}
