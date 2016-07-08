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
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView)findViewById(R.id.listView);
    }

    //----------------------------------------------------------------------------------------------

    /*
    *   Turn on Bluetooth
    */
    public void on(View view)
    {
        if(!BA.isEnabled())
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
        BA.disable();
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
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList<>();

        for(BluetoothDevice bt : pairedDevices)
        {
            list.add(bt.getName());
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
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
            }
        }
    };

    /*
    *   start searching for new devices
    */
    public void startBroadcastReceiver(View v)
    {
        //Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);        //must unregister during onDestroy
        BA.startDiscovery();
    }//end method startBroadcastReceiver

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        BA.cancelDiscovery();
    }
}//end Main Activity
