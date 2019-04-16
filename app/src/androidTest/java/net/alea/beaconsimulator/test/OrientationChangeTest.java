package net.alea.beaconsimulator.test;

import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import net.alea.beaconsimulator.ActivityMain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class OrientationChangeTest {

    @Rule
    public ActivityTestRule<ActivityMain> mActivityTestRule = new ActivityTestRule<>(ActivityMain.class);

    @Test
    public void activityMainTest() throws RemoteException, InterruptedException {

        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        uiDevice.setOrientationLeft();
        Thread.sleep(1000);
        uiDevice.setOrientationNatural();

    }

}
