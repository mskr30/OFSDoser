/*
 * Copyright (c) 2013. Alexander Martinz.
 */

package net.openfiresecurity.ofsdoser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.openfiresecurity.ofsdoser.activities.PrefActivity;
import net.openfiresecurity.ofsdoser.fragments.DosFragment;

public class MainActivity extends FragmentActivity {

    private SharedPreferences mPrefs;
    private Toast mToast;
    private FragmentManager fragmentManager;

    private boolean mDebug = false;
    private static long back_pressed;

    /**
     * Override backbutton presses to exit
     */
    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                if (mToast != null)
                    mToast.cancel();
                finish();
            } else {
                mToast = Toast.makeText(getBaseContext(),
                        getString(R.string.action_press_again),
                        Toast.LENGTH_SHORT);
                mToast.show();
            }
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, PrefActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        mDebug = mPrefs.getBoolean("pref_extensive_logging", false);
        logDebug("Extensive Logging: " + (mDebug ? "enabled" : "disabled"));

        fragmentManager = getSupportFragmentManager();
        replaceFragment(new DosFragment(), false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            Runtime.getRuntime().exit(0);
        }
    }

    private void replaceFragment(Fragment fragment, boolean addToBackstack) {
        if (addToBackstack) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
    }

    private void logDebug(String msg) {
        if (mDebug) {
            Log.e("OFSDOSER", msg);
        }
    }

}