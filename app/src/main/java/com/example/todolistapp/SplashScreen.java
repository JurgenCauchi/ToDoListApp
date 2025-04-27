package com.example.todolistapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge layout to handle system bars (like status bar and navigation)
        EdgeToEdge.enable(this);

        // Set the content view to the splash screen layout
        setContentView(R.layout.activity_splash_screen);

        // Handle insets to adjust padding around system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Get the system bars' insets (top, left, right, bottom)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply padding to the view so that content is not overlapped by system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create a new handler to delay the transition to the main activity
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // Create an intent to navigate from SplashScreen to MainActivity
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                // Start MainActivity
                startActivity(intent);
                // Finish the current activity (SplashScreen) so it won't be in the back stack
                finish();
            }
        }, 3000); // Delay of 3000 milliseconds (3 seconds) before transitioning
    }
}
