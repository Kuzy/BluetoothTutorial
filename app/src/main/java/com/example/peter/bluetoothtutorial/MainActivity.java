package com.example.peter.bluetoothtutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    private BluetoothAdapter _bluetoothAdapter;

    //collection of bluetooth device objects
    private ArrayList<BluetoothDevice> _bluetoothDevices;

    //for displaying the list of devices to the user
    private ArrayList<String> _listDiscoveredDevices;
    private ArrayAdapter _listAdapter;

    //support variables
    private boolean _broadcastReceiverEnable = false;

    //Handler to get data from other threads
    private Handler _handler = null;

    private final int MESSAGE_RECEIVED = 1;
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize the bluetooth adapter
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //initialize ArrayList for holding bluetooth objects
        _bluetoothDevices = new ArrayList();

        //prepare list for displaying devices found
        ListView __lstDevices = (ListView)findViewById(R.id.listView);
        _listDiscoveredDevices = new ArrayList();
        _listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, _listDiscoveredDevices);
        __lstDevices.setAdapter(_listAdapter);
        __lstDevices.setOnCreateContextMenuListener(this);

        _handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message __inputMessage)
            {
                switch (__inputMessage.what)
                {
                    case MESSAGE_RECEIVED:
                        Toast.makeText(getApplicationContext(), "" + __inputMessage.obj, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        super.handleMessage(__inputMessage);
                }//end switch
            }//end function handleMessage
        };//end Handler
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
            _bluetoothDevices.clear();

            Toast.makeText(getApplicationContext(), "Listing paired devices...", Toast.LENGTH_LONG).show();
            Set<BluetoothDevice> __pairedDevices = _bluetoothAdapter.getBondedDevices();

            for (BluetoothDevice __bt : __pairedDevices) {
                _listDiscoveredDevices.add(__bt.getName());
                _bluetoothDevices.add(__bt);
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
            _bluetoothDevices.clear();

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
                Toast.makeText(getApplicationContext(), "pairing selected", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "pairing with " + _bluetoothDevices.get(position).getName(), Toast.LENGTH_SHORT).show();
                ConnectThread connectThread = new ConnectThread(_bluetoothDevices.get(position));
                connectThread.start();
                break;
        }//end switch

        return false;
    }//end function onContextItemSelected

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    private class ConnectThread extends Thread
    {
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device)
        {
            //Use a temporary object that is later assigned to mmSocket,
            //because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            //get a BluetoothSocket to connect with the given BluetoothDevice
            try
            {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }//end try
            catch(IOException e)
            {

            }//end catch

            mmSocket = tmp;
        }//end constructor

        public void run()
        {
            //cancel discovery because it will slow the connection
            _bluetoothAdapter.cancelDiscovery();

            try
            {
                //Connect the device through the socket. This will block
                //until it succeeds or throws an exception
                mmSocket.connect();
            }//end try
            catch (IOException connectException)
            {
                //Unable to connect; close the socket and get out
                try
                {
                    Toast.makeText(getApplicationContext(), "Failed to connect!", Toast.LENGTH_SHORT).show();
                    mmSocket.close();
                }//end try
                catch (IOException closeException)
                {

                }//end catch

                return;
            }//end catch

            //manage connection in a separate thread
            ConnectedThread manage = new ConnectedThread(mmSocket);
            manage.start();
        }//end method run

        /* will cancel an in-progress connection and close the socket */
        public void cancel()
        {
            try
            {
                mmSocket.close();
            }//end try
            catch (IOException e)
            {

            }//end catch
        }//end method cancel
    }//end class ConnectThread

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket _socket;
        private final InputStream _inputStream;
        private final OutputStream _outputStream;

        public ConnectedThread(BluetoothSocket __socket)
        {
            _socket = __socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Get the input and output streams using temp objects because member streams are final
            try
            {
                tmpIn = __socket.getInputStream();
                tmpOut = __socket.getOutputStream();
            }//end try
            catch (IOException e)
            {

            }//end catch

            _inputStream = tmpIn;
            _outputStream = tmpOut;
        }//end constructor

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            //Keep listening to the InputStream until an exception occurs
            while(true)
            {
                try
                {
                    //Read from the InputStream
                    bytes = _inputStream.read(buffer);
                    //Send the obtained bytes to the UI activity
                    _handler.obtainMessage(MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                }
                catch(IOException e)
                {
                    break;
                }
            }
        }//end function run

        public void write(byte[] __bytes)
        {
            try
            {
                _outputStream.write(__bytes);
            }//end try
            catch (IOException e)
            {

            }//end catch
        }//end function write

        public void cancel()
        {
            try
            {
                _socket.close();
            }//end try
            catch (IOException e)
            {

            }//end catch
        }//end function cancel
    }//end class ConnectedThread
}//end Main Activity
