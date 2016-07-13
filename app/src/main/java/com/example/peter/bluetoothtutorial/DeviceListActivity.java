package com.example.peter.bluetoothtutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity
{
    //Constants to represent instruction
    private final int SEARCH = 1, PAIRED = 2, CONNECTED = 3;

    //bluetooth adapter to handle requests
    private BluetoothAdapter _bluetoothAdapter;

    //collection of bluetooth device objects
    private ArrayList<BluetoothDevice> _bluetoothDevices;

    //for displaying the list of devices to the user
    private ArrayList<String> _listDiscoveredDevices;
    private ArrayAdapter _listAdapter;

    //support variables
    private boolean _broadcastReceiverEnable = false;
    private int _currentInstruction = 0;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //Setting the display length and width to be a percentage of the total screen
        DisplayMetrics __displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(__displayMetrics);

        int __width = __displayMetrics.widthPixels;
        int __height = __displayMetrics.heightPixels;

        getWindow().setLayout((int) (__width * 0.8), (int) (__height * 0.8));

        //get an instance of the bluetooth adapter
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //initialize ArrayList for holding bluetooth objects
        _bluetoothDevices = new ArrayList<>();


        //possibility of getting null pointer exception
        try
        {
            //prepare list for displaying devices found
            ListView __lstDevices = (ListView)findViewById(R.id.listView);
            _listDiscoveredDevices = new ArrayList<>();
            _listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, _listDiscoveredDevices);
            assert __lstDevices != null;
            __lstDevices.setAdapter(_listAdapter);
            __lstDevices.setOnCreateContextMenuListener(this);
        }//end try
        catch (NullPointerException npe)
        {
            npe.printStackTrace();
        }//end catch

        //get instruction passed on from main activity
        _currentInstruction = getIntent().getExtras().getInt("instruction");

        //depending on the instruction we execute a different command
        switch (_currentInstruction)
        {
            case SEARCH:
                startDiscovery();
                break;
            case PAIRED:
                list();
                break;
            case CONNECTED:
                showConnections();
                break;
        }//end switch
    }//end function onCreate

    //----------------------------------------------------------------------------------------------

    /*
    *   lists all paired bluetooth devices
    */
    public void list()
    {
        //clear ArrayLists
        _listDiscoveredDevices.clear();
        _listAdapter.notifyDataSetChanged();
        _bluetoothDevices.clear();

        Toast.makeText(getApplicationContext(), "Listing paired devices...", Toast.LENGTH_LONG).show();
        Set<BluetoothDevice> __pairedDevices = _bluetoothAdapter.getBondedDevices();

        //iterate through all the Bluetooth devices paired with
        for (BluetoothDevice __bt : __pairedDevices) {
            _listDiscoveredDevices.add(__bt.getName());
            _bluetoothDevices.add(__bt);
        }//end for loop

        _listAdapter.notifyDataSetChanged();
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
                _bluetoothDevices.add(device);
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
    public void startDiscovery()
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
        _bluetoothDevices.clear();

        Toast.makeText(getApplicationContext(), "Searching for devices...", Toast.LENGTH_LONG).show();

        //start searching for bluetooth devices
        _bluetoothAdapter.startDiscovery();
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

        //ensure that the bluetooth adapter stopped searching for devices
        //safe to use even when discovery is disabled
        if(_bluetoothAdapter.isDiscovering())
        {
            _bluetoothAdapter.cancelDiscovery();
        }//end if
    }//end function stopDiscovery

    //----------------------------------------------------------------------------------------------

    /*
    *   Method to show the current devices that are connected
    */
    public void showConnections()
    {
        try
        {
            ArrayList<BluetoothDevice> devices = (ArrayList<BluetoothDevice>) getIntent().getExtras().get("devices");

            for(BluetoothDevice device:devices)
            {
                _listDiscoveredDevices.add(device.getName() + "\n" + device.getAddress());
                _bluetoothDevices.add(device);
            }//end for loop

            _listAdapter.notifyDataSetChanged();
        }//end try
        catch (NullPointerException npe)
        {
            Toast.makeText(getApplicationContext(), "Null Pointer Exception: " + npe, Toast.LENGTH_LONG).show();
        }//end catch
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "Exception: " + e, Toast.LENGTH_LONG).show();
        }//end catch
    }//end function showConnections

    //----------------------------------------------------------------------------------------------

    /*
    *  ensuring resources are closed before activity closes
    */
    @Override
    protected void onDestroy()
    {
        _currentInstruction = 0;
        _bluetoothAdapter = null;
        _bluetoothDevices.clear();
        _listAdapter.clear();
        _listDiscoveredDevices.clear();
        stopDiscovery();
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

        switch(_currentInstruction)
        {
            case CONNECTED:
                menu.add(0,1,1, "Disconnect");
                break;
            default:
                menu.add(0,0,0,"Connect");
        }//end switch
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
                Toast.makeText(getApplicationContext(), "connecting to " + _bluetoothDevices.get(position).getName(), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, new Intent().putExtra("device", _bluetoothDevices.get(position)));
                finish();
                break;
            case 1:
                Toast.makeText(getApplicationContext(), "disconnecting from " + _bluetoothDevices.get(position).getName(), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, new Intent().putExtra("device", _bluetoothDevices.get(position)));
                finish();
                break;
        }//end switch

        return false;
    }//end function onContextItemSelected

    //----------------------------------------------------------------------------------------------
}//end DeviceListActivity
