package com.example.peter.bluetoothtutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    private BluetoothAdapter _bluetoothAdapter;

    //Handler to get data from other threads
    private Handler _handler = null;

    //constant integer value to represent receiving a message from another device
    private final int MESSAGE_RECEIVED = 1;

    //device that is connected
    private ConnectThread _connectDevice = null;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize the bluetooth adapter
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //initialize handler and make it do something when another thread passes a message back
        _handler = new Handler(Looper.getMainLooper()){
            TextView lblMessage = (TextView)findViewById(R.id.lblMessagesReceived);
            ScrollView scrollView = (ScrollView)findViewById(R.id.scrollTextView);

            @Override
            public void handleMessage(Message __inputMessage)
            {
                switch (__inputMessage.what)
                {
                    case MESSAGE_RECEIVED:
                        Toast.makeText(getApplicationContext(), __inputMessage.obj.toString(), Toast.LENGTH_SHORT).show();
                        lblMessage.append(__inputMessage.obj.toString() + "\n");
                        scrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(View.FOCUS_DOWN);
                            }
                        });
                        break;
                    default:
                        //let the program handle any other messages passed
                        super.handleMessage(__inputMessage);
                }//end switch
            }//end function handleMessage
        };//end Handler
    }//end onCreate

    //----------------------------------------------------------------------------------------------

    /*
    *   Turn on Bluetooth
    */
    public void on()
    {
        //check if bluetooth adapter exists on the device

        if(_bluetoothAdapter != null)
        {
            //check if bluetooth adapter is turned on
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
        }//end if
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device!", Toast.LENGTH_LONG).show();
        }//end else
    }//end on function

    //----------------------------------------------------------------------------------------------

    /*
    *   Turn off Bluetooth
    */
    public void off()
    {
        //check if bluetooth adapter is turned on
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
    public void visible()
    {
        //check if bluetooth adapter is turned on
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

    /*Call this function to cancel connection with the device*/
    public void closeConnection(View v)
    {
        _connectDevice.cancel();
        _connectDevice = null;  //destroy reference to the device
    }//end function closeConnection

    //----------------------------------------------------------------------------------------------

    /*
    *   displays a menu list when the menu button is pressed on the device
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0,0,0,"Turn On Bluetooth");
        menu.add(0,1,1,"Turn Off Bluetooth");
        menu.add(0,2,2,"Search for devices");
        menu.add(0,3,3,"Display paired devices");
        menu.add(0,4,4,"Make Discoverable");
        return true;
    }//end function onCreateOptionsMenu

    //----------------------------------------------------------------------------------------------

    /*
    *   when an option is selected in the menu, this will process the action
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean result = false;

        //detect which option was selected if any
        switch(item.getItemId())
        {
            case 0:             //turn on bluetooth
                on();
                result = true;
                break;
            case 1:            //turn off bluetooth
                off();
                result = true;
                break;
            case 2:            //search for nearby devices
                //check if bluetooth adapter is turned on
                if(_bluetoothAdapter.isEnabled())
                {
                    Intent searchIntent = new Intent(this, DeviceListActivity.class);
                    searchIntent.putExtra("instruction", 1);
                    startActivityForResult(searchIntent, 1);
                    result = true;
                }//end if
                else
                {
                    Toast.makeText(getApplicationContext(), "Bluetooth must be enabled first!", Toast.LENGTH_LONG).show();
                }//end else

                break;
            case 3:            //list all paired devices
                //check if bluetooth adapter is turned on
                if(_bluetoothAdapter.isEnabled())
                {
                    Intent listIntent = new Intent(this, DeviceListActivity.class);
                    listIntent.putExtra("instruction", 2);
                    startActivityForResult(listIntent, 1);
                    result = true;
                    break;
                }//end if
                else
                {
                    Toast.makeText(getApplicationContext(), "Bluetooth must be enabled first!", Toast.LENGTH_LONG).show();
                }//end else
            case 4:           //make device visible
                visible();
                result = true;
                break;
        }//end switch

        return result;
    }//end function onOptionsItemSelected

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK)
        {
            //process the incoming request
            switch (requestCode)
            {
                case 1:
                    BluetoothDevice device = (BluetoothDevice) data.getExtras().get("device");
                    _connectDevice = new ConnectThread(device);
                    _connectDevice.start();
                    break;

            }//end switch
        }//end if
    }//end onActivityResult

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    /*
    *   Class that initiates the connection to the bluetooth device
    */
    private class ConnectThread extends Thread
    {
        //default Bluetooth UUID is being used
        private final UUID _UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        //defining connection variables
        private final BluetoothSocket _socket;

        //the object that will manage communication with the bluetooth device
        ConnectedThread _managedCommunication;

        public ConnectThread(BluetoothDevice device)
        {
            //Use a temporary object that is later assigned to mmSocket,
            //because mmSocket is final
            BluetoothSocket tmp = null;

            //get a BluetoothSocket to connect with the given BluetoothDevice
            try
            {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(_UUID);
            }//end try
            catch(IOException e)
            {
                e.printStackTrace();
            }//end catch

            _socket = tmp;
        }//end constructor

        //------------------------------------------------------------------------------------------

        public void run()
        {
            //cancel discovery because it will slow the connection
            _bluetoothAdapter.cancelDiscovery();

            try
            {
                //Connect the device through the socket. This will block
                //until it succeeds or throws an exception
                _socket.connect();
            }//end try
            catch (IOException connectException)
            {
                //Unable to connect; close the socket and get out
                try
                {
                    Toast.makeText(getApplicationContext(), "Failed to connect!", Toast.LENGTH_SHORT).show();
                    _socket.close();
                }//end try
                catch (IOException closeException)
                {
                    closeException.printStackTrace();
                }//end catch

                return;
            }//end catch

            //manage connection in a separate thread
            _managedCommunication = new ConnectedThread(_socket);
            _managedCommunication.start();
        }//end method run

        //------------------------------------------------------------------------------------------

        //uses this method to invoke write method for the connection
        public void send(String message)
        {
            _managedCommunication.write(message.getBytes());
        }//end method send

        //------------------------------------------------------------------------------------------

        // will cancel an in-progress connection and close the socket
        public void cancel()
        {
            try
            {
                _socket.close();
            }//end try
            catch (IOException e)
            {
                e.printStackTrace();
            }//end catch
        }//end method cancel
    }//end class ConnectThread

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    /*
    *   class that handles communication between bluetooth devices
    */
    private class ConnectedThread extends Thread
    {
        private final InputStream _inputStream;
        private final OutputStream _outputStream;

        public ConnectedThread(BluetoothSocket __socket)
        {
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
                e.printStackTrace();
            }//end catch

            _inputStream = tmpIn;
            _outputStream = tmpOut;
        }//end constructor

        //------------------------------------------------------------------------------------------

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

                    //converting array of bytes into string
                    String readMessage = new String(buffer, 0, bytes);

                    //Send the obtained bytes to the UI activity
                    _handler.obtainMessage(MESSAGE_RECEIVED, readMessage).sendToTarget();
                }//end try
                catch(IOException e)
                {
                    break;
                }//end catch
            }//end while loop
        }//end function run

        //------------------------------------------------------------------------------------------

        //writes bytes to the output stream
        public void write(byte[] __bytes)
        {
            try
            {
                _outputStream.write(__bytes);
            }//end try
            catch (IOException e)
            {
                e.printStackTrace();
            }//end catch
        }//end function write
    }//end class ConnectedThread
}//end Main Activity
