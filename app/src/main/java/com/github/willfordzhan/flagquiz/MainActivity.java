package com.github.willfordzhan.flagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;


public class MainActivity extends AppCompatActivity {
    // keys for reading data from SharedPreferences
    // remember to keep the same as the preference.xml
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true; // used to force portrait mode
    private boolean preferenceChanged = true; // did preferences change ?
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        // Todo: to implement the preferenceChangeListener class.
        // determine screen size
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        // if the device is a tablet, set phoneDevice to false
        if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false;

        // if running on a phone-sized device, allow only portrait orientation
        if (phoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }
    @Override
    public void onStart(){
        super.onStart();

        if (preferenceChanged){
            // now that the default preferences have been set,
            // initialize MainActivityFragment and start the quiz
            MainActivityFragment quizFragment =
                    (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this)
            );
            quizFragment.updateRegions(
                    PreferenceManager.getDefaultSharedPreferences(this)
            );
            quizFragment.resetQuiz();
            // Todo: To implement these 3 methods
            preferenceChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // get the device's current orientation
        int orientation = getResources().getConfiguration().orientation;
        // display the app's menu only in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            // inflate the menu
            getMenuInflater().inflate(R.menu.menu_main,menu);
            return true;
        }
        else{
            return false;
        }
    }

    // displays the SettingsActivity when running on a phone
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent preferenceIntent = new Intent(this, Settings.class);
        startActivity(preferenceIntent);
        return super.onOptionsItemSelected(item);
    }

    // listeners for changes to the app's SharedPreferences
    private OnSharedPreferenceChangeListener preferenceChangeListener = new OnSharedPreferenceChangeListener() {
        // called when the user changes the app's preferences
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            preferenceChanged = true;
            MainActivityFragment quizFragment =
                    (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            if (key.equals(CHOICES)){ // # choices to display changed (items in preferences)
                quizFragment.updateGuessRows(sharedPreferences);
            }
            else if (key.equals(REGIONS)){ // regions to include changed
                Set<String> regions = sharedPreferences.getStringSet(REGIONS,null);

                if (regions != null && regions.size() > 0){
                    quizFragment.updateRegions(sharedPreferences);
                    quizFragment.resetQuiz();
                }
                else{
                    // at least one region to be set -- North America as default
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    regions.add(getString(R.string.default_region));
                    editor.putStringSet(REGIONS,regions);
                    editor.apply();

                    Toast.makeText(MainActivity.this, R.string.default_region_message,Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(MainActivity.this, R.string.restarting_quiz,Toast.LENGTH_SHORT).show();
        }
    };
}


