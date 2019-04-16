package net.alea.beaconsimulator.test.espresso;

import android.support.annotation.NonNull;
import android.support.test.espresso.matcher.BoundedMatcher;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.*;


public class MoreMatchers {

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @SuppressWarnings("SimplifiableIfStatement")
            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    public static Matcher<View> navigateBackToolbarButton() {
        return allOf(
                withParent(withClassName(is(Toolbar.class.getName()))),
                withClassName(anyOf(
                        is(ImageButton.class.getName()),
                        is(AppCompatImageButton.class.getName())
                )));
    }
}
