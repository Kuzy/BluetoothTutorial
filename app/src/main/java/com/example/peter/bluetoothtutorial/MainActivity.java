package com.example.peter.bluetoothtutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity
{
    private BluetoothAdapter _bluetoothAdapter;

    private ArrayList _listDiscoveredDevices;
    private ArrayAdapter _listAdapter;

    //support variables
    private boolean _broadcastReceiverEnable = false;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize the bluetooth adapter
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //prepare list for displaying devices found
        ListView __lstDevices = (ListView)findViewById(R.id.listView);
        _listDiscoveredDevices = new ArrayList();
        _listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, _listDiscoveredDevices);
        __lstDevices.setAdapter(_listAdapter);
        __lstDevices.setOnCreateContextMenuListener(this);
    }//end onCreate

    //----------------------------------------------------------------------------------------------

    /*
    *   Turn on Bluetooth
    */
    public void on(View view)
    {
        if(!_bluetoothAdapter.isEnabled())
        {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned On", Toast.LENGTH_LONG).show();
        }//end if
        else
        {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }//end else
    }//end on function

    //----------------------------------------------------------------------------------------------

    /*
    *   Turn off Bluetooth
    */
    public void off(View v)
    {
        if(_bluetoothAdapter.isEnabled())
        {
            _bluetoothAdapter.disable();
            Toast.makeText(getApplication(), "Turned off", Toast.LENGTH_LONG).show();
        }//end if
        else
        {
            Toast.makeText(getApplication(), "Already off", Toast.LENGTH_LONG).show();
        }//end else
    }//end off function

    //----------------------------------------------------------------------------------------------

    /*
    *   Make device visible
    */
    public void visible(View v)
    {
        if(_bluetoothAdapter.isEnabled())
        {
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(getVisible, 0);
        }//end if
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled first!", Toast.LENGTH_LONG).show();
        }//end else
    }//end function visible

    //----------------------------------------------------------------------------------------------

    /*
    *   lists all paired bluetooth devices
    */
    public void list(View v)
    {
        if(_bluetoothAdapter.isEnabled())
        {
            _listDiscoveredDevices.clear();
            _listAdapter.notifyDataSetChanged();

            Toast.makeText(getApplicationContext(), "Listing paired devices...", Toast.LENGTH_LONG).show();
            Set<BluetoothDevice> __pairedDevices = _bluetoothAdapter.getBondedDevices();

            for (BluetoothDevice __bt : __pairedDevices) {
                _listDiscoveredDevices.add(__bt.getName());
            }//end for loop

            _listAdapter.notifyDataSetChanged();
        }//end if
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled first!", Toast.LENGTH_LONG).show();
        }//end else
    }//end function list

    //----------------------------------------------------------------------------------------------

    /*
    *   handle devices that are discovered
    */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //when discovery finds a device
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //get the bluetooth device object from the intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                _listDiscoveredDevices.add(device.getName() + "\n" + device.getAddress());
                _listAdapter.notifyDataSetChanged();
            }//end if
        }
    };//end BroadcastReceiver

    //----------------------------------------------------------------------------------------------

    /*
    *   register Broadcast Receiver
    */
    public void registerBroadcastReceiver()
    {
        //Register the BroadcastReceiver
        IntentFilter __filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, __filter);        //must unregister during onDestroy
        _broadcastReceiverEnable = true;
    }//end function startBroadcastReceiver

    //----------------------------------------------------------------------------------------------

    /*
    *   start discovery for nearby devices
    */
    public void startDiscovery(View v)
    {
        if(_bluetoothAdapter.isEnabled())
        {
            //check that the broadcast receiver is started
            if(!_broadcastReceiverEnable)
            {
                //broadcast receiver is not registered so we register it first
                registerBroadcastReceiver();
            }//end if

            //clear the list
            _listDiscoveredDevices.clear();
            _listAdapter.notifyDataSetChanged();

            Toast.makeText(getApplicationContext(), "Searching for devices...", Toast.LENGTH_LONG).show();

            //start searching for bluetooth devices
            _bluetoothAdapter.startDiscovery();
        }//end if
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled first!", Toast.LENGTH_LONG).show();
        }//end else
    }//end function startDiscovery

    //----------------------------------------------------------------------------------------------

    /*
    *   stop discovery for nearby devices
     */
    public void stopDiscovery()
    {
        //unregister broadcast receiver if enabled
        if(_broadcastReceiverEnable)
        {
            unregisterReceiver(mReceiver);
            _broadcastReceiverEnable = false;
        }//end if
    }//end function stopDiscovery

    //----------------------------------------------------------------------------------------------

    /*
    *  ensuring resources are closed before app closes
    */
    @Override
    protected void onDestroy()
    {
        stopDiscovery();

        //ensure that the bluetooth adapter stopped searching for devices
        //safe to use even when discovery is disabled
        _bluetoothAdapter.cancelDiscovery();

        super.onDestroy();
    }//end function onDestroy

    //----------------------------------------------------------------------------------------------

    /*
    *   function to create a menu when a long press is detected on the list view
    */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"Pair");
    }//end function onCreateContextMenu

    //----------------------------------------------------------------------------------------------

    /*
    *   action to be performed when an option is selected
    */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;

        switch (item.getItemId())
        {
            case 0:
                stopDiscovery();
                Toast.makeText(getApplicationContext(), "pairing selected", Toast.LENGTH_LONG).show();
                break;
        }//end switch

        return false;
    }//end function onContextItemSelected
}//end Main Activity
