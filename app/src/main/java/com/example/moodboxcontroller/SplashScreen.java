package com.example.moodboxcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        View gradientOverlay = findViewById(R.id.splash_screen_overlay);
        gradientOverlay.setAlpha(1f); // Make the overlay visible.

        // Animating the gradientOverlay which makes the effect of fading in and out all the views of
        // the splash screen.
        gradientOverlay.animate().alpha(0.0f).setDuration(1500).withEndAction(() ->
                // After fading in the elements wait for 2 seconds and then fade out.
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        // Fading out the elements.
                        gradientOverlay.animate().alpha(1f).setDuration(1500).withEndAction(() -> {
                            startActivity(new Intent(SplashScreen.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                            overridePendingTransition(0,0);
                            finish();
                }), 2000));
    }
}