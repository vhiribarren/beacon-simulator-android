package net.alea.beaconsimulator.test;


import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.DrawerActions;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;

import net.alea.beaconsimulator.ActivityMain;
import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.test.espresso.MoreMatchers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BackButtonTest {

    @Rule
    public ActivityTestRule<ActivityMain> mActivityTestRule = new ActivityTestRule<>(ActivityMain.class);

    @Test
    public void goAboutActivityAndComeback() {
        // Open Drawer and go to about screen
        onView(withId(R.id.drawer_drawerlayout)).perform(DrawerActions.open());
        onView(withText(R.string.action_about)).perform(click());
        // Check we are on the about screen, and come back
        onView(withText(R.string.about_author)).check(ViewAssertions.matches(isDisplayed()));
        onView(MoreMatchers.navigateBackToolbarButton()).perform(click());
        // Check we are in the main screen
        onView(withId(R.id.main_viewpager)).check(ViewAssertions.matches(isDisplayed()));
    }

    @Test
    public void goSettingsAndComeback() {
        // Open Drawer and go to settings screen
        onView(withId(R.id.drawer_drawerlayout)).perform(DrawerActions.open());
        onView(withText(R.string.action_settings)).perform(click());
        // Check we are on the settings screen, and come back
        onView(withId(R.id.settings_fragment_content)).check(ViewAssertions.matches(isDisplayed()));
        onView(MoreMatchers.navigateBackToolbarButton()).perform(click());
        // Check we are in the main screen
        onView(withId(R.id.main_viewpager)).check(ViewAssertions.matches(isDisplayed()));
    }

}
