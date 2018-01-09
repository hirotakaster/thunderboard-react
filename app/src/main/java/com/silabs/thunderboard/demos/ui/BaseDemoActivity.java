package com.silabs.thunderboard.demos.ui;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.silabs.thunderboard.BuildConfig;
import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.ui.ThunderBoardActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public abstract class BaseDemoActivity extends ThunderBoardActivity implements BaseDemoViewListener {

    @Inject
    PreferenceManager prefsManager;

    /**
     * The toolbar at the top of the activity
     */
    protected Toolbar toolbar;

    /**
     * Container for motion, environment, and i/o layouts
     */
    protected FrameLayout mainSection;

    protected String deviceAddress;

    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_base);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mainSection = (FrameLayout) findViewById(R.id.main_section);

        setupToolbar();
        deviceAddress = getIntent().getStringExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_demo, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            return true;
        } else if (id == R.id.action_open_in_browser) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        getDemoPresenter().clearViewListener();
        super.onDestroy();
    }

    // ThunderBoardActivity

    @Override
    public void onBluetoothDisabled() {
        finish();
    }

    // BaseDemoViewListener

    @Override
    public void onWifi(boolean isConnected) {
        if (!isConnected) {
            Toast.makeText(this, "No Wi-Fi", Toast.LENGTH_SHORT);
        }
    }

    public void onDisconnected() {
        getDemoPresenter().clearViewListener();
    }

    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getToolbarColor());
        toolbar.setTitle(getToolbarString());

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        params.height += getStatusBarHeight();
        toolbar.setLayoutParams(params);

        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract int getToolbarColor();

    protected abstract String getToolbarString();

    protected abstract BaseDemoPresenter getDemoPresenter();

    protected abstract void initControls();

    /**
     * setMenuItemsStreaming
     *
     * Makes the share and open in browser icons appear or
     * disappear, based on whether the streaming is true of false.
     *
     * @param isStreaming
     *
     */
    private void setMenuItemsEnabled(boolean isStreaming) {
        menu.findItem(R.id.action_share).setVisible(isStreaming);
        menu.findItem(R.id.action_open_in_browser).setVisible(isStreaming);
    }

    /**
     * isBadString
     *
     * Checks to see if a string is valid
     *
     * @param string
     * @return true if the input string is not valid
     */
    private boolean isBadString(String string) {
        return TextUtils.isEmpty(string) || TextUtils.equals(string, "null");
    }
}
