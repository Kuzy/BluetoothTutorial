package com.example.peter.bluetoothtutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity
{
    private BluetoothAdapter _bluetoothAdapter;

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
        _bluetoothAdapter.disable();
        Toast.makeText(getApplication(), "Turned off", Toast.LENGTH_LONG).show();
    }//end off function

    //----------------------------------------------------------------------------------------------

    /*
    *   Make device visible
    */
    public void visible(View v)
    {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }//end function visible

    //----------------------------------------------------------------------------------------------

    /*
    *   lists all paired bluetooth devices
    */
    public void list(View v)
    {
        ListView __lstDevices = (ListView)findViewById(R.id.listView);
        Set<BluetoothDevice> __pairedDevices = _bluetoothAdapter.getBondedDevices();
        ArrayList __list = new ArrayList<>();

        for(BluetoothDevice __bt : __pairedDevices)
        {
            __list.add(__bt.getName());
        }//end for loop

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, __list);
        __lstDevices.setAdapter(adapter);
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
                Toast.makeText(getApplicationContext(), "Device discovered: " + device.getName() + "\n" + device.getAddress(), Toast.LENGTH_LONG).show();
                //discoveredDevices.add(device.getName() + "\n" + device.getAddress());
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
        //check that the broadcast receiver is started
        if(!_broadcastReceiverEnable)
        {
            //broadcast receiver is not registered so we register it first
            registerBroadcastReceiver();
        }//end if

        //start searching for bluetooth devices
        _bluetoothAdapter.startDiscovery();
    }//end function startDiscovery

    //----------------------------------------------------------------------------------------------

    /*
    *  ensuring resources are closed before app closes
    */
    @Override
    protected void onDestroy()
    {
        //unregister broadcast receiver if enabled
        if(_broadcastReceiverEnable)
        {
            unregisterReceiver(mReceiver);
            _broadcastReceiverEnable = false;
        }//end if

        //ensure that the bluetooth adapter stopped searching for devices
        _bluetoothAdapter.cancelDiscovery();

        super.onDestroy();
    }//end function onDestroy

    //----------------------------------------------------------------------------------------------
}//end Main Activity
