package roy.NXT_Control;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.InputStream;
import java.io.OutputStream;

import roy.NXT_Control.BTConnection.BTDialog;

public class ConnectionFragment extends Fragment{

    private ImageView bluetoothIcon;
    private TextView status;
    private TextView device;
    private SeekBar batteryLevel;
    private TextView batteryAmount;
    private ToggleButton connectButton;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice robot;
    private BluetoothSocket socket;
    static final int PICK_BLUETOOTH_DEVICE = 1;

    private InputStream is = null;
    private OutputStream os = null;

    FragCommunicator fc;

    static ConnectionFragment newInstance(int num) {
        ConnectionFragment f = new ConnectionFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_connection,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        fc = (FragCommunicator)getActivity();

        bluetoothIcon = (ImageView) v.findViewById(R.id.bt_icon);

        status = (TextView) v.findViewById(R.id.connection_status);
        device = (TextView) v.findViewById(R.id.tv_deviceConnected);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!btAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(intent, 1000);
        }

        //Connect Device Button
        connectButton = (ToggleButton) v.findViewById(R.id.tbtn_connect);
        connectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toggles the Button through on and off
                if (connectButton.isChecked()) {
                    Intent lv_deviceConnect = new Intent(getContext(),BTDialog.class);
                    startActivityForResult(lv_deviceConnect, PICK_BLUETOOTH_DEVICE);
                }
                else {
                    try{
                        socket.close();
                        is.close();
                        os.close();
                        status.setText("Disconnected");
                        device.setText("No Device");
                        connectButton.setChecked(false);
                        bluetoothIcon.setImageResource(R.mipmap.bt_icon_black);
                    }
                    catch(Exception e){
                        Toast.makeText(getContext(),"Error when Disconnecting - " + e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Battery Level
        batteryLevel = (SeekBar)v.findViewById(R.id.sb_batteryLevel);
        batteryAmount = (TextView)v.findViewById(R.id.tv_batteryAmount);
        batteryLevel.setEnabled(false);
    }

    //Gets result from selecting a device
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                //Picked a device to connect to
                robot = data.getExtras().getParcelable("device");
                try {
                    socket = robot.createRfcommSocketToServiceRecord(java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    socket.connect();
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    fc.sendBTDeviceDetails(btAdapter,robot,socket,is,os);
                    //Successful Connection
                    bluetoothIcon.setImageResource(R.mipmap.bt_icon_blue);
                    device.setText(robot.getName());
                    status.setText("Connected");

                } catch (Exception e) {
                    //Connection failed
                    Toast.makeText(getContext(), "Error: Failed to connect to " + robot.getName(), Toast.LENGTH_SHORT).show();
                    connectButton.setChecked(false); //Reset button to connect
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //User decided to cancel
                connectButton.setChecked(false);
            }
        }
    }

    //Gets battery level from robot
    private void batteryLevel(){
        try{
            byte [] buffer = new byte[15];

            buffer[0] = (byte) (15-2);
            buffer[1] = 0;
            buffer[2] = 0;
            buffer[3] = 0x0B;   //gets battery level
            buffer[4] = 0;
            buffer[5] = 0;
            buffer[6] = 0;
            buffer[7] = 0;
            buffer[8] = 0;
            buffer[9] = 0;
            buffer[10] = 0;
            buffer[11] = 0;
            buffer[12] = 0;
            buffer[13] = 0;
            buffer[14] = 0;

            Log.i("tag","Battery Level " + buffer[3]);
        }
        catch(Exception e){

        }
    }
}