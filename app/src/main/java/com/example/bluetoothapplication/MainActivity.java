package com.example.bluetoothapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 101;
    BluetoothAdapter myBluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
    private ListView list_view;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
    }

    private void findViewById() {
        list_view = findViewById(R.id.list_view);
    }

    private void CheckBluetoothAdapter() {

        if(myBluetoothAdapter != null)
        {
            if(!myBluetoothAdapter.isEnabled())
            {
                //enable bluetooth
                Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetoothIntent, REQUEST_ENABLE_BT);
            }
        }
        else
        {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {

            if(resultCode == RESULT_OK)
            {
                Toast.makeText(this, "Bluetooth Enabled", Toast.LENGTH_LONG).show();
            }else
            {
                if(resultCode == RESULT_CANCELED)
                {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void ButtonBuletoothOn(View view) {
        CheckBluetoothAdapter();
    }

    public void ButtonBuletoothOff(View view) {
        if(myBluetoothAdapter != null)
        {
            if(myBluetoothAdapter.isEnabled())
            {
                //disble bluetooth
                myBluetoothAdapter.disable();
            }
        }
        else
        {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_LONG).show();
        }
    }

    public void GetAllBluetoothList(View view) {
        Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices();
        String[] strings = new String[bt.size()];
        int index = 0;
        if(bt.size() > 0)
        {
            for (BluetoothDevice device : bt) {
                strings[index]=device.getName();
                arrayList.add(device.getName());
                index++;
            }

            arrayAdapter =  new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
            list_view.setAdapter(arrayAdapter);

        }
    }

    public void ScanBluetoothList(View view) {
        myBluetoothAdapter.startDiscovery();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver,intentFilter);
    }

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayList.add(device.getName());
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };
}