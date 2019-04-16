package net.alea.beaconsimulator.test.espresso;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;


public class EspressoUtils {

    public static String getText(final Matcher<View> matcher) {
        final String[] stringHolder = { null };
        Espresso.onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }
            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView tv = (TextView)view; //Safe, because of check in getConstraints()
                stringHolder[0] = tv.getText().toString();
            }
        });
        return stringHolder[0];
    }

}
