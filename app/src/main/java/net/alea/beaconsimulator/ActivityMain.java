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

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityMain extends ActivityDrawer implements  FragmentScanner.OnScannerActionDelegate {

    private static final Logger sLogger = LoggerFactory.getLogger(ActivityMain.class);

    enum Feature {broadcast, scan}

    private static final String EXTRA_FEATURE = "EXTRA_FEATURE";

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private FloatingActionButton mSharedFab;

    private ObjectAnimator mFabAnimator;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED: {
                    final int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    switch (btState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            final FragmentScanner fragmentScanner = mViewPagerAdapter.getFragmentScanner();
                            if (fragmentScanner != null) {
                                fragmentScanner.stopBeaconScan();
                            }
                            break;
                    }
                    break;
                }
            }
        }
    };


    public static void displayActivityFeature(Context context, Feature feature) {
        final Intent activityIntent = new Intent(context, ActivityMain.class);
        activityIntent.putExtra(EXTRA_FEATURE, feature);
        context.startActivity(activityIntent);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        Feature feature = (Feature)extras.getSerializable(EXTRA_FEATURE);
        if (feature == null) {
            return;
        }
        switch (feature) {
            case broadcast:
                mViewPager.setCurrentItem(ViewPagerAdapter.PAGE_INDEX_SIMULATOR);
                break;
            case scan:
                mViewPager.setCurrentItem(ViewPagerAdapter.PAGE_INDEX_SCANNER);
                break;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sLogger.debug("onCreate()");

        setContentView(R.layout.activity_main);

        mSharedFab = (FloatingActionButton) findViewById(R.id.main_fab_shared);
        mSharedFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mViewPager.getCurrentItem()) {
                    case ViewPagerAdapter.PAGE_INDEX_SCANNER:
                        final FragmentScanner fragmentScanner = mViewPagerAdapter.getFragmentScanner();
                        if (fragmentScanner != null) {
                            fragmentScanner.actionScanToggle();
                        }
                        break;
                    case ViewPagerAdapter.PAGE_INDEX_SIMULATOR:
                        final FragmentSimulator fragmentSimulator = mViewPagerAdapter.getFragmentSimulator();
                        if (fragmentSimulator != null) {
                            fragmentSimulator.actionCreateBeacon();
                        }
                        break;
                    default:
                        throw new IndexOutOfBoundsException(String.format("Cannot have more than %s items", ViewPagerAdapter.PAGE_COUNT));
                }
            }
        });
        mSharedFab.setImageResource(R.drawable.ic_menu_add); // To init FAB with icon

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mViewPager, this);

        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                final FragmentSimulator fragmentSimulator = mViewPagerAdapter.getFragmentSimulator();
                if (fragmentSimulator == null) {
                    return;
                }
                if (position != ViewPagerAdapter.PAGE_INDEX_SIMULATOR && fragmentSimulator.isEditMode()) {
                    fragmentSimulator.finishEditMode();
                }
                switch (position) {
                    case ViewPagerAdapter.PAGE_INDEX_SIMULATOR:
                        selectNavigationItem(ITEM_BROADCAST);
                        break;
                    case ViewPagerAdapter.PAGE_INDEX_SCANNER:
                        selectNavigationItem(ITEM_SCAN);
                        break;
                    case -1:
                    default:
                        selectNavigationItem(ITEM_NONE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mSharedFab.hide();
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        switch (mViewPager.getCurrentItem()) {
                            case ViewPagerAdapter.PAGE_INDEX_SIMULATOR:
                                mSharedFab.setImageResource(R.drawable.ic_menu_add);
                                break;
                            case ViewPagerAdapter.PAGE_INDEX_SCANNER:
                            default:
                                mSharedFab.setImageResource(R.drawable.ic_menu_search);
                                break;
                        }
                        mSharedFab.show();
                        break;
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(ViewPagerAdapter.PAGE_INDEX_SIMULATOR).setIcon(R.drawable.ic_menu_broadcast_on);
        tabLayout.getTabAt(ViewPagerAdapter.PAGE_INDEX_SCANNER).setIcon(R.drawable.ic_menu_search);

        initNavigationDrawer(findViewById(android.R.id.content), toolbar);
        onNewIntent(getIntent()); // Beware: intent can be null after an app update from Android Studio
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // In this method, so we are sure the internal state of mViewPager is loaded after config change
        switch (mViewPager.getCurrentItem()) {
            case ViewPagerAdapter.PAGE_INDEX_SCANNER:
                mSharedFab.setImageResource(R.drawable.ic_menu_search);
                // Trick to ensure setUserVisibilityHint is not called with false after orientation change
                final FragmentScanner fragmentScanner = mViewPagerAdapter.getFragmentScanner();
                if (fragmentScanner != null) {
                    mViewPager.getAdapter().setPrimaryItem(null, ViewPagerAdapter.PAGE_INDEX_SCANNER, fragmentScanner);
                }
                break;
            case ViewPagerAdapter.PAGE_INDEX_SIMULATOR:
            default:
                mSharedFab.setImageResource(R.drawable.ic_menu_add);
                // Trick to ensure setUserVisibilityHint is not called with false after orientation change
                final FragmentSimulator fragmentSimulator = mViewPagerAdapter.getFragmentSimulator();
                if (fragmentSimulator != null) {
                    mViewPager.getAdapter().setPrimaryItem(null, ViewPagerAdapter.PAGE_INDEX_SIMULATOR, fragmentSimulator);
                }
                // TODO CHECK IF OK AND CORRECT ANIMATION OF BUTTON
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        sLogger.debug("onStart()");
        registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public void onResume() {
        super.onResume();
        sLogger.debug("onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        sLogger.debug("onStop()");
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        final FragmentSimulator fragmentSimulator = mViewPagerAdapter.getFragmentSimulator();
        if (fragmentSimulator != null
                && mViewPager.getCurrentItem() == ViewPagerAdapter.PAGE_INDEX_SIMULATOR
                && fragmentSimulator.isEditMode()) {
            fragmentSimulator.finishEditMode();
        }
        else {
            super.onBackPressed();
        }
    }

    public void onScanStatusUpdate(boolean isScanning) {
        if (mViewPager.getCurrentItem() != ViewPagerAdapter.PAGE_INDEX_SCANNER) {
            return;
        }
        if (isScanning) {
            if (mSharedFab != null) mSharedFab.setImageResource(R.drawable.ic_menu_pause);
            int whiteColor = 0xFFFFFFFF;
            int accentColor = ContextCompat.getColor(this, R.color.colorAccent);
            mFabAnimator = ObjectAnimator.ofArgb(mSharedFab.getDrawable(), "tint", accentColor, whiteColor);
            mFabAnimator.setDuration(500);
            mFabAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            mFabAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            mFabAnimator.start();
        }
        else {
            if (mSharedFab != null) mSharedFab.setImageResource(R.drawable.ic_menu_search);
            if (mFabAnimator != null) {
                mFabAnimator.setRepeatCount(0);
                mFabAnimator = null;
            }
        }
    }


    // http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
    // Trick of retrieving fragment by name to be sure it is directly retrieved from
    // the fragment manager. Usefull to be used with the trick in onRestoreInstance and
    // avoid the call to setUserVisibleHint. It we override intantiateItem, the trick to
    // avoid the call to setUserVisibleHint does not work anymore.
    private static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final static int PAGE_COUNT = 2;
        private final static int PAGE_INDEX_SIMULATOR = 0;
        private final static int PAGE_INDEX_SCANNER = 1;

        private final int _containerId;
        private final Context _context;
        private final FragmentManager _fragmentManager;

        public ViewPagerAdapter(FragmentManager manager, ViewPager container, Context context) {
            super(manager);
            _containerId = container.getId();
            _context = context;
            _fragmentManager = manager;
        }

        @Nullable
        public Fragment getRegisteredFragment(int position) {
            return _fragmentManager.findFragmentByTag(getFragmentTag(_containerId, position));
        }

        @Nullable
        public FragmentSimulator getFragmentSimulator() {
            return (FragmentSimulator) getRegisteredFragment(PAGE_INDEX_SIMULATOR);
        }

        @Nullable
        public FragmentScanner getFragmentScanner() {
            return (FragmentScanner) getRegisteredFragment(PAGE_INDEX_SCANNER);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGE_INDEX_SIMULATOR:
                    return new FragmentSimulator();
                case PAGE_INDEX_SCANNER:
                    return new FragmentScanner();
                default:
                    throw new IndexOutOfBoundsException(String.format("Cannot have more than %s items", PAGE_COUNT));
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case PAGE_INDEX_SIMULATOR:
                    return _context.getString(R.string.main_tab_simulator);
                case PAGE_INDEX_SCANNER:
                    return _context.getString(R.string.main_tab_scanner);
                default:
                    throw new IndexOutOfBoundsException(String.format("Cannot have more than %s items", PAGE_COUNT));
            }
        }

        private String getFragmentTag(int viewPagerId, int fragmentPosition) {
            return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
        }

    }


}
