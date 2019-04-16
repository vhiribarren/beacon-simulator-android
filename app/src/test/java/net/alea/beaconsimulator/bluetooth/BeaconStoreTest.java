package net.alea.beaconsimulator.bluetooth;


import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.BeaconType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 23)
public class BeaconStoreTest {


    @Test
    public void storeEmptyAtStartup() {
        BeaconStore store = new BeaconStore(RuntimeEnvironment.application);
        assertEquals("Should have empty size at startup", 0, store.size());
        assertNull("Getting data at startup should return null", store.getBeaconAt(0));
        assertNull("Negative index should return null", store.getBeaconAt(-1));
        assertNull("Getting data at startup should return null", store.getBeaconAt((int)Math.floor(Math.random()*1000)));
    }


    @Test
    public void storeAndGetBeaconImmediately() {
        BeaconStore store = new BeaconStore(RuntimeEnvironment.application);
        BeaconModel model = new BeaconModel(BeaconType.raw);
        store.saveBeacon(model);
        assertEquals(1, store.size());
        assertEquals(model.getId(), store.getBeaconAt(0).getId());
        assertEquals(model.getId(), store.getBeacon(model.getId()).getId());
        BeaconModel storedBeacon =  store.getBeaconAt(0);
        assertSame("Should be same since model is cached", model, storedBeacon);
    }

    @Test
    public void storeAndGetBeaconAfterPersistence() {
        BeaconStore store = new BeaconStore(RuntimeEnvironment.application);
        BeaconModel model = new BeaconModel(BeaconType.raw);
        store.saveBeacon(model);
        assertEquals(1, store.size());
        // Simulating new app instance by creating a new store
        BeaconStore store2 = new BeaconStore(RuntimeEnvironment.application);
        assertEquals(1, store2.size());
        BeaconModel storedBeacon =  store2.getBeaconAt(0);
        assertEquals(model.getId(), storedBeacon.getId());
        assertNotSame("Should not be same since new one is deserialized", model, storedBeacon);
    }

    @Test
    public void deleteSavedBeacon() {
        BeaconStore store = new BeaconStore(RuntimeEnvironment.application);
        BeaconModel model = new BeaconModel(BeaconType.raw);
        store.saveBeacon(model);
        assertEquals(1, store.size());
        // Simulating new app instance by creating a new store
        BeaconStore store2 = new BeaconStore(RuntimeEnvironment.application);
        store2.deleteBeacon(model);
        assertEquals(0, store2.size());
        // Checking it is completely deleted from persistence by creating another store
        BeaconStore store3 = new BeaconStore(RuntimeEnvironment.application);
        assertEquals(0, store3.size());
    }


}
