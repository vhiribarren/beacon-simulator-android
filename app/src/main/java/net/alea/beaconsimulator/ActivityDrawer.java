/****************************************************************************************
 * Copyright (c) 2016, 2017, 2019 Vincent Hiribarren                                    *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * Linking Beacon Simulator statically or dynamically with other modules is making      *
 * a combined work based on Beacon Simulator. Thus, the terms and conditions of         *
 * the GNU General Public License cover the whole combination.                          *
 *                                                                                      *
 * As a special exception, the copyright holders of Beacon Simulator give you           *
 * permission to combine Beacon Simulator program with free software programs           *
 * or libraries that are released under the GNU LGPL and with independent               *
 * modules that communicate with Beacon Simulator solely through the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces. You may           *
 * copy and distribute such a system following the terms of the GNU GPL for             *
 * Beacon Simulator and the licenses of the other code concerned, provided that         *
 * you include the source code of that other code when and as the GNU GPL               *
 * requires distribution of source code and provided that you do not modify the         *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces.                   *
 *                                                                                      *
 * The intent of this license exception and interface is to allow Bluetooth low energy  *
 * closed or proprietary advertise data packet structures and contents to be sensibly   *
 * kept closed, while ensuring the GPL is applied. This is done by using an interface   *
 * which only purpose is to generate android.bluetooth.le.AdvertiseData objects.        *
 *                                                                                      *
 * This exception is an additional permission under section 7 of the GNU General        *
 * Public License, version 3 (“GPLv3”).                                                 *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package net.alea.beaconsimulator;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.akexorcist.localizationactivity.LocalizationActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@SuppressLint("Registered")
public class ActivityDrawer extends LocalizationActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final Logger sLogger = LoggerFactory.getLogger(ActivityDrawer.class);

    public static final int ITEM_NONE = -1;
    public static final int ITEM_BROADCAST = R.id.nav_broadcast;
    public static final int ITEM_SCAN = R.id.nav_scan;

    @IntDef({ITEM_NONE, ITEM_BROADCAST, ITEM_SCAN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NavItem {}

    @Nullable
    private ActionBarDrawerToggle mDrawerToggle;
    @Nullable
    private DrawerLayout mDrawerLayout;
    @Nullable
    private NavigationView mNavigationView;


    @Override
    public void onResume() {
        super.onResume();
        // http://stackoverflow.com/questions/21974368/closing-the-navigation-drawer-after-opening-another-activity
        mDrawerLayout.closeDrawer(GravityCompat.START, false);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout == null) {
            return;
        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.isChecked()) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.nav_broadcast:
                ActivityMain.displayActivityFeature(this, ActivityMain.Feature.broadcast);
                break;
            case R.id.nav_scan:
                ActivityMain.displayActivityFeature(this, ActivityMain.Feature.scan);
                break;
            case R.id.nav_settings:
                ActivitySettings.launchActivity(this);
                break;
            case R.id.nav_about:
                ActivityAbout.launchActivity(this);
                break;
            default:
                return false;
        }
        return true;
    }


    protected void initNavigationDrawer(View mainView, Toolbar toolbar){
        mDrawerLayout = (DrawerLayout) mainView.findViewById(R.id.drawer_drawerlayout);
        mNavigationView = (NavigationView) findViewById(R.id.drawer_navigationview);
        mNavigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                supportInvalidateOptionsMenu();
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        selectNavigationItem(ITEM_BROADCAST);
    }

    protected void selectNavigationItem(@NavItem int itemId) {
        if (mNavigationView == null) {
            sLogger.warn("Navigation View is null");
            return;
        }
        Menu menu = mNavigationView.getMenu();
        if (itemId == ITEM_NONE) {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setChecked(false);
            }
        }
        else {
            MenuItem item = menu.findItem(itemId);
            if (item == null) {
                return;
            }
            item.setChecked(true);
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

}
