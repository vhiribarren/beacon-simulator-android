package com.akexorcist.localizationactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BlankDummyActivity extends Activity {

    public static void launchActivity(Activity context) {
        Intent aboutIntent = new Intent(context, BlankDummyActivity.class);
        context.startActivity(aboutIntent);
        context.overridePendingTransition(
                R.anim.animation_localization_activity_transition_in,
                R.anim.animation_localization_activity_transition_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_dummy);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(
                R.anim.animation_localization_activity_transition_in,
                R.anim.animation_localization_activity_transition_out);
    }

}
