package net.hitch_hiking.otostopproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;

/**
 * Created by root on 7/1/15.
 */
public class Intro extends AppIntro {

    public static final String PREFERENCES_FILE_NAME = "MyAppPreferences";

    @Override
    public void init(Bundle bundle) {
        int preferencesResult;
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES_FILE_NAME, MODE_WORLD_READABLE);

        preferencesResult = pref.getInt("key", 0); // getting integer

        if (preferencesResult > 0) {
            loadMainActivity();
            Toast.makeText(getApplicationContext(),"[+]GetSharedPre return is: " + preferencesResult , Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(),"[+]GetSharedPre return is: " + preferencesResult, Toast.LENGTH_SHORT).show();
        }

        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(new FirstSlide(), getApplicationContext());
        addSlide(new SecondSlide(), getApplicationContext());
        addSlide(new ThirdSlide(), getApplicationContext());
        addSlide(new FourthSlide(), getApplicationContext());

        // OPTIONAL METHODS
        // Override bar/separator color
        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip button
        showSkipButton(false);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        setVibrate(true);
        setVibrateIntensity(30);
    }

    private void loadMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSkipPressed() {
        Toast.makeText(getApplicationContext(),getString(R.string.skip_string), Toast.LENGTH_SHORT).show();
        loadMainActivity();
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    public void getStarted(View view){
        loadMainActivity();
    }
}
