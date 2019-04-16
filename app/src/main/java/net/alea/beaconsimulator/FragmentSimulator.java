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

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import net.alea.beaconsimulator.bluetooth.BeaconSimulatorService;
import net.alea.beaconsimulator.bluetooth.BeaconStore;
import net.alea.beaconsimulator.bluetooth.event.BeaconStoreSizeEvent;
import net.alea.beaconsimulator.bluetooth.event.BroadcastChangedEvent;
import net.alea.beaconsimulator.bluetooth.model.AltBeacon;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.BeaconType;
import net.alea.beaconsimulator.bluetooth.model.EddystoneEID;
import net.alea.beaconsimulator.bluetooth.model.EddystoneTLM;
import net.alea.beaconsimulator.bluetooth.model.EddystoneUID;
import net.alea.beaconsimulator.bluetooth.model.EddystoneURL;
import net.alea.beaconsimulator.bluetooth.model.IBeacon;
import net.alea.beaconsimulator.component.DialogAskBluetooth;
import net.alea.beaconsimulator.component.DividerItemDecoration;
import net.alea.beaconsimulator.component.DialogNewBeaconTemplate;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.TreeSet;

/**
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.hcbho6z42
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-6a6f0c422efd#.ibwrebaff
 * http://enoent.fr/blog/2015/01/18/recyclerview-basics/
 * http://stackoverflow.com/questions/26483778/display-actionmode-over-toolbar
 *
 */
public class FragmentSimulator extends Fragment {

    private static final Logger sLogger = LoggerFactory.getLogger(FragmentSimulator.class);

    private static final int SWICH_REFRESH_DELAY = 500; // in ms

    private SavedBeaconAdapter mBeaconAdapter;
    private BeaconStore mBeaconStore;
    private View mDescriptionView;
    private final Handler mHandler = new Handler();
    private RecyclerView mRecyclerView;
    @Nullable private ActionMode mActionMode;
    @Nullable private BeaconSimulatorService.ServiceControl mServiceControl;
    private boolean mServiceBound = false;

    private final MyItemTouchCallback mItemTouchCallback = new MyItemTouchCallback();
    private final ActionMode.Callback mActionModeCallback = new MyActionModeCallback();


    public FragmentSimulator() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sLogger.debug("onCreate()");
        setHasOptionsMenu(true);
        mBeaconStore = ((App)getActivity().getApplication()).getBeaconStore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        sLogger.debug("onCreateView()");

        View view = inflater.inflate(R.layout.fragment_simulator, container, false);

        mDescriptionView = view.findViewById(R.id.simulator_linearlayout_description);
        Button autoDiagnosticButton = (Button) view.findViewById(R.id.simulator_button_autodiagnostic);
        autoDiagnosticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityBeaconDiagnostic.launchActivity(getContext());
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.simulator_recyclerview_beaconlist);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        mBeaconAdapter = new SavedBeaconAdapter();
        mRecyclerView.setAdapter(mBeaconAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mItemTouchCallback.enableDrag(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        sLogger.debug("onStart()");
        sLogger.debug("Binding to service");
        BeaconSimulatorService.bindService(getContext(), mServiceConnection);
        mServiceBound = true;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        sLogger.debug("onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        sLogger.debug("onPause()");
    }

    @Override
    public void onStop() {
        sLogger.debug("onStop()");
        if (mServiceBound) {
            sLogger.debug("Unbinding from service");
            BeaconSimulatorService.unbindService(getContext(), mServiceConnection);
            mServiceBound = false;
            mServiceControl = null;
        }
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_simulator_default, menu);
        if (mServiceControl != null) {
            menu.findItem(R.id.nav_broadcast_stop).setVisible(
                    mServiceControl.getBroadcastList().size() != 0
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_auto_diagnostic: {
                ActivityBeaconDiagnostic.launchActivity(getContext());
                return true;
            }
            case R.id.nav_broadcast_stop: {
                BeaconSimulatorService.stopAll(getContext(), true);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableEditMode() {
        if (mActionMode != null) {
            return;
        }
        mActionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(mActionModeCallback);
    }

    public boolean isEditMode() {
        return mActionMode != null;
    }

    public void finishEditMode() {
        if (mActionMode == null) {
            return;
        }
        mActionMode.finish();
    }

    public void actionCreateBeacon() {
        BottomSheetDialogFragment newBeaconTemplateDialog = new DialogNewBeaconTemplate();
        newBeaconTemplateDialog.show(getActivity().getSupportFragmentManager(), newBeaconTemplateDialog.getTag());
    }

    private void updateStopBroadcastMenu() {
        getActivity().supportInvalidateOptionsMenu();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onBroadcastChangedEvent(BroadcastChangedEvent event) {
        final int pos = mBeaconStore.getBeaconIndex(event.getBeaconId());
        // Delayed to avoid switching back a broadcast switch while it is being switched on
        mHandler.postDelayed(new TimerTask() {
            @Override
            public void run() {
                mBeaconAdapter.notifyItemChanged(pos);
            }
        }, SWICH_REFRESH_DELAY);
        final int nbActive =  event.getActiveBroadcast();
        if (event.isBroadcasting()) {
            Snackbar.make(getView(), getResources().getQuantityString(R.plurals.simulator_broadcast_start, nbActive, nbActive), Snackbar.LENGTH_SHORT).show();
        }
        else {
            Snackbar.make(getView(), getResources().getQuantityString(R.plurals.simulator_broadcast_stop, nbActive, nbActive), Snackbar.LENGTH_SHORT).show();
        }
        updateStopBroadcastMenu();
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onStoreSizeEvent(BeaconStoreSizeEvent event) {
        if (event.getStoreSize() == 0) {
            mDescriptionView.setVisibility(View.VISIBLE);
        }
        else {
            mDescriptionView.setVisibility(View.GONE);
        }
    }

    private class MyActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.fragment_simulator_edit_mode, menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete: {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.beacondelete_title)
                            .setMessage(R.string.beacondelete_message)
                            .setPositiveButton(R.string.all_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBeaconAdapter.deleteSelected();
                                    mActionMode.finish();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.all_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    break;
                }
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (! mItemTouchCallback.isDragging()) {
                mBeaconAdapter.clearSelection();
            }
            mItemTouchCallback.enableDrag(true);
            mActionMode = null;
        }
    }



    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sLogger.debug("Now binded to BeaconSimulatorService");
            mServiceControl = (BeaconSimulatorService.ServiceControl)service;
            mBeaconAdapter.notifyDataSetChanged();
            updateStopBroadcastMenu();
        }
        // Theoretically, this method cannot be called when a binding is done in the same process
        @Override
        public void onServiceDisconnected(ComponentName name) {
            sLogger.debug("Connection with BeaconSimulatorService lost");
            mServiceBound = false;
            mServiceControl = null;
        }
    };



    private class MyItemTouchCallback extends ItemTouchHelper.Callback {
        private boolean _enabled = true;
        private boolean _itemPositionChanged = false;
        private boolean _isDragging;
        public boolean isDragging() { return _isDragging; }
        public void enableDrag(boolean enable) {_enabled = enable; }
        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }
        @Override
        public boolean isLongPressDragEnabled() {
            return _enabled;
        }
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            _itemPositionChanged = true;
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (isEditMode()) {
                finishEditMode();
            }
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    mBeaconAdapter.changeItemIndex(i, i+1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    mBeaconAdapter.changeItemIndex(i, i-1);
                }
            }
            return true;
        }
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_DRAG: {
                    _isDragging = true;
                    break;
                }
                case ItemTouchHelper.ACTION_STATE_IDLE: {
                    _isDragging = false;
                    if (_itemPositionChanged) {
                        mBeaconAdapter.clearSelection();
                    }
                    _itemPositionChanged = false;
                    break;
                }
            }
        }
    }



    public class SavedBeaconAdapter extends RecyclerView.Adapter<SavedBeaconAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener{
            public final View view;
            public final TextView beaconName;
            public final TextView beaconType;
            public final TextView beaconSubType;
            public final ImageView beaconImage;
            public final Switch beaconToggle;
            public final View selectedOverlay;
            public final TextView resilientState;
            public ViewHolder(View v) {
                super(v);
                v.setTag(this);
                view = v;
                beaconName =(TextView) v.findViewById(R.id.savedbeacon_textview_name);
                beaconType =(TextView) v.findViewById(R.id.savedbeacon_textview_type);
                beaconSubType = (TextView) v.findViewById(R.id.savedbeacon_textview_subtype);
                beaconImage =(ImageView) v.findViewById(R.id.savedbeacon_imageview_beacon);
                beaconToggle = (Switch) v.findViewById(R.id.savedbeacon_switch_broadcast);
                selectedOverlay = v.findViewById(R.id.savedbeacon_view_overlay);
                resilientState =(TextView) v.findViewById(R.id.savedbeacon_textview_resilient);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View clickedView) {
                        if (! isEditMode()) {
                            BeaconModel beaconModel = mBeaconStore.getBeaconAt(getAdapterPosition());
                            ActivityBeaconEdit.editBeacon(getActivity(), beaconModel.getId());
                        }
                        else {
                            toggleSelection(getAdapterPosition());
                        }
                    }
                });
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (! isEditMode()) {
                            enableEditMode();
                        }
                        if (_selectedItems.size() > 1) {
                            mItemTouchCallback.enableDrag(false);
                        }
                        toggleSelection(getAdapterPosition());
                        return true;
                    }
                });

                beaconToggle.setOnCheckedChangeListener(this);
            }

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                final int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    sLogger.debug("NO_POSITION in onCheckedChanged(), skipping");
                    return;
                }
                final BeaconModel beaconModel = mBeaconStore.getBeaconAt(adapterPosition);
                if (isChecked) {
                    if (BeaconSimulatorService.isBluetoothOn(getContext()) && BeaconSimulatorService.isBroadcastAvailable(getContext())) {
                        BeaconSimulatorService.startBroadcast(getContext(), beaconModel.getId(), true);
                    }
                    else if (BeaconSimulatorService.isBluetoothOn(getContext())) {
                        mHandler.postDelayed(new TimerTask() {
                            @Override
                            public void run() {
                                final ViewHolder holder = (ViewHolder)mRecyclerView.findViewHolderForAdapterPosition(adapterPosition);
                                if (holder == null) {
                                    sLogger.warn("View holder is null, skipping - #1");
                                    return;
                                }
                                holder.beaconToggle.setChecked(false);
                                notifyItemChanged(adapterPosition);
                            }
                        }, SWICH_REFRESH_DELAY);
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.simulator_no_multibroadcast_title)
                                .setMessage(R.string.simulator_no_multibroadcast_message)
                                .setPositiveButton(R.string.all_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                    else {
                        mHandler.postDelayed(new TimerTask() {
                            @Override
                            public void run() {
                                final ViewHolder holder = (ViewHolder)mRecyclerView.findViewHolderForAdapterPosition(adapterPosition);
                                if (holder == null) {
                                    sLogger.warn("View holder is null, skipping - #2");
                                    return;
                                }
                                holder.beaconToggle.setChecked(false);
                                notifyItemChanged(adapterPosition);
                            }
                        }, SWICH_REFRESH_DELAY);
                        DialogAskBluetooth dialog = new DialogAskBluetooth();
                        dialog.show(getActivity().getSupportFragmentManager(), dialog.getTag());
                    }
                }
                else {
                    BeaconSimulatorService.stopBroadcast(getContext(), beaconModel.getId(), true);
                }
            }
        }

        private final Set<Integer> _selectedItems = new TreeSet<>();

        // Create new views (invoked by the layout manager)
        @Override
        public SavedBeaconAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_saved_beacon, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final BeaconModel beaconModel = mBeaconStore.getBeaconAt(position);
            final BeaconType beaconType = beaconModel.getType();
            holder.beaconImage.setImageResource(beaconType.getImageResource());
            if (beaconModel.getName() != null && ! beaconModel.getName().isEmpty()) {
                holder.beaconName.setText(beaconModel.getName());
                holder.beaconName.setVisibility(View.VISIBLE);
            }
            else {
                holder.beaconName.setVisibility(View.GONE);
            }
            String typeMessage = beaconType.toString();
            String subTypeMessage = "";
            switch (beaconType) {
                case ibeacon:
                    IBeacon iBeacon = beaconModel.getIBeacon();
                    typeMessage = getString(
                            R.string.item_model_params_ibeacon,
                            iBeacon.getProximityUUID().toString());
                    subTypeMessage = getString(
                            R.string.item_model_params_ibeacon_sub,
                            iBeacon.getMajor(),
                            iBeacon.getMinor());
                    break;
                case eddystoneUID:
                    EddystoneUID eddystoneUid = beaconModel.getEddystoneUID();
                    typeMessage = getString(
                            R.string.item_model_params_eddystoneUID,
                            eddystoneUid.getNamespace());
                    subTypeMessage = getString(
                            R.string.item_model_params_eddystoneUID_sub,
                            eddystoneUid.getInstance());
                    break;
                case eddystoneURL:
                    EddystoneURL eddystoneUrl = beaconModel.getEddystoneURL();
                    typeMessage = getString(
                            R.string.item_model_params_eddystoneURL,
                            eddystoneUrl.getUrl());
                    break;
                case eddystoneTLM:
                    EddystoneTLM eddystoneTlm = beaconModel.getEddystoneTLM();
                    typeMessage = getString(
                            R.string.item_model_params_eddystoneTLM,
                            eddystoneTlm.getVoltage(), eddystoneTlm.getAdvertisingCount());
                    subTypeMessage = getString(
                            R.string.item_model_params_eddystoneTLM_sub,
                            eddystoneTlm.getTemperature(), eddystoneTlm.getUptime()/10.0);
                    break;
                case eddystoneEID:
                    EddystoneEID eddystoneEID = beaconModel.getEddystoneEID();
                    typeMessage = getString(
                            R.string.item_model_params_eddystoneEID,
                            eddystoneEID.getIdentityKey());
                    subTypeMessage = getString(
                            R.string.item_model_params_eddystoneEID_sub,
                            getResources().getStringArray(R.array.rotation_exponent)[eddystoneEID.getRotationPeriodExponent()], eddystoneEID.getCounterOffset());
                    break;
                case altbeacon:
                    AltBeacon altBeacon = beaconModel.getAltbeacon();
                    typeMessage = getString(
                            R.string.item_model_params_ibeacon,
                            altBeacon.getBeaconNamespace().toString());
                    subTypeMessage = getString(
                            R.string.item_model_params_ibeacon_sub,
                            altBeacon.getMajor(),
                            altBeacon.getMinor());
                    break;
                default:
            }
            holder.beaconType.setText(typeMessage);
            holder.beaconType.setVisibility(typeMessage.isEmpty() ? View.GONE : View.VISIBLE);
            holder.beaconSubType.setText(subTypeMessage);
            holder.beaconSubType.setVisibility(subTypeMessage.isEmpty() ? View.GONE : View.VISIBLE);
            if (mServiceControl != null) {
                holder.beaconToggle.setOnCheckedChangeListener(null); // otherwise the setChecked will trigger the listener
                holder.beaconToggle
                        .setChecked(mServiceControl.getBroadcastList().contains(beaconModel.getId()) );
                holder.beaconToggle.setOnCheckedChangeListener(holder); // reinstall the listener for user input
            }
            holder.selectedOverlay.setVisibility(_selectedItems.contains(position) ? View.VISIBLE : View.INVISIBLE);
            // Resilience status
            if (App.getInstance().getConfig().getBroadcastResilience() && mBeaconStore.activeBeacons().contains(beaconModel.getId().toString()) ) {
                holder.resilientState.setVisibility(View.VISIBLE);
            }
            else {
                holder.resilientState.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return mBeaconStore.size();
        }

        public void clearSelection() {
            //noinspection Convert2streamapi
            for(int i: _selectedItems) {
                notifyItemChanged(i);
            }
            _selectedItems.clear();
        }

        public void toggleSelection(int position) {
            if (_selectedItems.contains(position)) {
                _selectedItems.remove(position);
            } else {
                _selectedItems.add(position);
            }
            notifyItemChanged(position);
            if (_selectedItems.size() == 0) {
                finishEditMode();
            }
            else {
                mActionMode.setTitle(String.valueOf(_selectedItems.size()));
                mActionMode.invalidate();
            }
        }

        public void changeItemIndex(int fromPos, int toPos) {
            if (_selectedItems.contains(fromPos)) {
                _selectedItems.remove(fromPos);
                _selectedItems.add(toPos);
            }
            notifyItemMoved(fromPos, toPos);
            notifyItemChanged(fromPos);
            mBeaconStore.changeBeaconIndex(mBeaconStore.getBeaconAt(fromPos), toPos);
        }

        public void deleteSelected() {
            // 2 pass, one to collect beacons, the other to delete them
            // if I delete at a first pass, index of selected items move while being deleted
            List<BeaconModel> beacons = new ArrayList<>(_selectedItems.size());
            //noinspection Convert2streamapi
            for(int i: _selectedItems) {
                beacons.add(mBeaconStore.getBeaconAt(i));
            }
            for(BeaconModel beacon: beacons) {
                mBeaconStore.deleteBeacon(beacon);
            }
            _selectedItems.clear();
            mBeaconAdapter.notifyDataSetChanged();
        }
    }

}
