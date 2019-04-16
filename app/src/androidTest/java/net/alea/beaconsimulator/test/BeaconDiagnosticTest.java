package net.alea.beaconsimulator.test;


import static android.support.test.espresso.Espresso.*;

import static android.support.test.InstrumentationRegistry.*;
import android.support.test.espresso.Espresso;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.action.ViewActions.*;

import static android.support.test.espresso.assertion.ViewAssertions.*;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.alea.beaconsimulator.ActivityMain;
import net.alea.beaconsimulator.App;
import net.alea.beaconsimulator.bluetooth.BeaconSimulatorService;
import net.alea.beaconsimulator.test.espresso.BluetoothIdlingResource;
import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.test.rule.ResetAppRule;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static net.alea.beaconsimulator.test.espresso.MoreMatchers.*;
import static net.alea.beaconsimulator.test.espresso.EspressoUtils.*;
import static net.alea.beaconsimulator.test.espresso.MoreViewActions.*;

import static org.hamcrest.Matchers.*;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BeaconDiagnosticTest {


    final static BluetoothIdlingResource sBtIdlingResource = new BluetoothIdlingResource(getInstrumentation().getTargetContext());

    @ClassRule
    public static ResetAppRule sResetAppRule = new ResetAppRule();
    @Rule
    public ActivityTestRule<ActivityMain> mActivityTestRule = new ActivityTestRule<>(ActivityMain.class);


    @BeforeClass
    public static void beforeClass() {
        Espresso.registerIdlingResources(sBtIdlingResource);
        App.getInstance().getConfig().setBroadcastResilience(false);
    }

    @AfterClass
    public static void afterClass() {
        Espresso.unregisterIdlingResources(sBtIdlingResource);
    }

    @Before
    public void setUp() {
        BeaconSimulatorService.stopAll(getInstrumentation().getTargetContext(), true);
    }


    /**
     * Given some broadcasting beacons,
     * When a beacon diagnostic is performed
     * And the diagnostic is finished
     * Then all previous broadcasting beacon continue to broadcast.
     */
    @Test
    public void restoreBeaconsAfterDiagnostic() {
        // Enable Bluetooth
        sBtIdlingResource.switchBluetoothState(BluetoothIdlingResource.State.bluetooth_on);

        // Launch diagnostic
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.action_autodiagnostic))
                .perform(click());

        onView(withId(R.id.diagnostic_button_start))
                .perform(click());

        onView(withId(R.id.diagnostic_textview_maxbroadcast));
        final String maxBroadcast = getText(withId(R.id.diagnostic_textview_maxbroadcast));

        Espresso.pressBack();

        // Create and activate a beacon
        onView(withId(R.id.main_fab_shared))
                .perform(click());

        onView(withId(R.id.newbeacon_textview_ibeacon))
                .perform(click());

        onView(withId(R.id.action_save))
                .perform(click());

        Espresso.pressBack();

        // We ensure the first beacon is restored to ON
        onView(withId(R.id.simulator_recyclerview_beaconlist))
                .check(matches(atPosition(0, hasDescendant(allOf(withId(R.id.savedbeacon_switch_broadcast), isNotChecked())))));

        onView(withId(R.id.simulator_recyclerview_beaconlist))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.savedbeacon_switch_broadcast)));

        // Launch again diagnostic
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.action_autodiagnostic))
                .perform(click());

        onView(withId(R.id.diagnostic_button_start))
                .perform(click());

        onView(withId(R.id.diagnostic_textview_maxbroadcast));
        final String newMaxBroadcast = getText(withId(R.id.diagnostic_textview_maxbroadcast));

        assertThat(maxBroadcast, equalTo(newMaxBroadcast));

        Espresso.pressBack();

        // We ensure the first beacon is restored to ON
        onView(withId(R.id.simulator_recyclerview_beaconlist))
                .check(matches(atPosition(0, hasDescendant(allOf(withId(R.id.savedbeacon_switch_broadcast), isChecked())))));
    }


    /**
     * Potential bug: if a beacon is ON and the bluetooth interface is brutally shutdown,
     * the beacon continue to be broadcasted, and the total number of broadcastable
     * beacon is reduced.
     */
    @Test
    public void bluetoothShutdownLeak() {
        // Enable Bluetooth
        sBtIdlingResource.switchBluetoothState(BluetoothIdlingResource.State.bluetooth_on);

        // Launch diagnostic
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(ViewMatchers.withText(R.string.action_autodiagnostic))
                .perform(click());

        onView(withId(R.id.diagnostic_button_start))
                .perform(click());

        onView(withId(R.id.diagnostic_textview_maxbroadcast));
        final String maxBroadcast = getText(withId(R.id.diagnostic_textview_maxbroadcast));

        Espresso.pressBack();

        // Create and activate a beacon
        onView(withId(R.id.main_fab_shared))
                .perform(click());

        onView(withId(R.id.newbeacon_textview_ibeacon))
                .perform(click());

        onView(withId(R.id.action_save))
                .perform(click());

        Espresso.pressBack();

        // We ensure the first beacon is OFF
        onView(withId(R.id.simulator_recyclerview_beaconlist))
                .check(matches(atPosition(0, hasDescendant(allOf(withId(R.id.savedbeacon_switch_broadcast), isNotChecked())))));

        onView(withId(R.id.simulator_recyclerview_beaconlist))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.savedbeacon_switch_broadcast)));

        // Ensure if we shutdown bluetooth, there is no resource leak with Android BT system
        sBtIdlingResource.switchBluetoothState(BluetoothIdlingResource.State.bluetooth_off);


        // Launch again diagnostic
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.action_autodiagnostic))
                .perform(click());

        // Ensure if we shutdown bluetooth, there is no resource leak with Android BT system
        sBtIdlingResource.switchBluetoothState(BluetoothIdlingResource.State.bluetooth_on);

        onView(withId(R.id.diagnostic_button_start))
                .perform(click());

        onView(withId(R.id.diagnostic_textview_maxbroadcast));
        final String newMaxBroadcast = getText(withId(R.id.diagnostic_textview_maxbroadcast));

        assertThat(maxBroadcast, equalTo(newMaxBroadcast));
    }

}
