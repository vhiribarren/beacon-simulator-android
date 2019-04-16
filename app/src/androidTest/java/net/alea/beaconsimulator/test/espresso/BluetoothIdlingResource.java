package net.alea.beaconsimulator.test.espresso;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.support.test.espresso.IdlingResource;

public class BluetoothIdlingResource implements IdlingResource {

    public enum State { bluetooth_on, bluetooth_off, always_idle }

    private ResourceCallback resourceCallback;
    private State stateTarget = State.always_idle;
    private final BluetoothAdapter btAdapter;

    public BluetoothIdlingResource(Context context) {
        final BluetoothManager btManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.btAdapter = btManager.getAdapter();
    }

    @Override
    public String getName() {
        return "Bluetooth State";
    }

    @Override
    public boolean isIdleNow() {
        switch (stateTarget) {
            case bluetooth_on: {
                if (btAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    this.resourceCallback.onTransitionToIdle();
                    return true;
                }
                else {
                    return false;
                }
            }
            case bluetooth_off: {
                if (btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    this.resourceCallback.onTransitionToIdle();
                    return true;
                }
                else {
                    return false;
                }
            }
            case always_idle:
            default: {
                this.resourceCallback.onTransitionToIdle();
                return true;
            }

        }
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }

    public void switchBluetoothState(State bluetoothState) {
        this.stateTarget = bluetoothState;
        switch (bluetoothState) {
            case bluetooth_off:
                btAdapter.disable();
                break;
            case bluetooth_on:
                btAdapter.enable();
                break;
            default:
        }
    }

}
