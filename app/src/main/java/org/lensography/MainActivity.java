package org.lensography;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    ImageView imgLensography;
    TextView txtLensography;

    int index;
    CharSequence charSequence;
    long delay = 200;
    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            txtLensography.setText(charSequence.subSequence(0,index++));
            if(index <= charSequence.length()){
                handler.postDelayed(runnable,delay);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLensography = findViewById(R.id.txtLensography);
        imgLensography = findViewById(R.id.imgLensography);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        
        Animation rotate= AnimationUtils.loadAnimation(MainActivity.this,R.anim.rotate);
        imgLensography.startAnimation(rotate);

        animatText("Lensography");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                startActivity(intent);
                finish();
            }
        },4000);
    }

    public void animatText(CharSequence cs){
        charSequence = cs;
        index = 0;
        txtLensography.setText("");
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,delay);
    }
}