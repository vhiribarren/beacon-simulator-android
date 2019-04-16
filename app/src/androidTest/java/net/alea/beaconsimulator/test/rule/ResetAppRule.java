package net.alea.beaconsimulator.test.rule;

import android.content.Context;
import static android.support.test.InstrumentationRegistry.*;

import org.junit.rules.ExternalResource;

import java.io.File;


public class ResetAppRule extends ExternalResource {

    @Override
    protected void after() {
        super.after();
        cleanSharedPreferences();
    }

    private void cleanSharedPreferences() {
        File root = getInstrumentation().getTargetContext().getFilesDir().getParentFile();
        String[] sharedPreferencesFileNames = new File(root, "shared_prefs").list();
        for (String fileName : sharedPreferencesFileNames) {
            getInstrumentation()
                    .getTargetContext()
                    .getSharedPreferences(fileName.replace(".xml", ""), Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
        }
    }
}
