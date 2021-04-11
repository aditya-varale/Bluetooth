package com.example.bluetoothapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class ImageBluetoothActivity extends AppCompatActivity {

    Button btn_create,btn_join,btn_send,btn_send_image;
    ListView list_view;
    TextView tv_message,tv_status;
    EditText et_message;
    ImageView imageView;

    private BluetoothAdapter myBluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private static final int REQUEST_ENABLE_BT = 101;

    //handler variable
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;
    static final int STATE_IMAGE_RECEIVED=6;

    private static final String APP_NAME = "BluetoothActivity";
    private static final UUID MY_UUID=UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");
    SendReceive sendReceive;
    SendReceiveForImage sendReceiveForImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_bluetooth);


        findViewByIds();
        listenres();
        startBluetoothConnection();
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,arrayList);
        list_view.setAdapter(arrayAdapter);
    }



    private void listenres() {

        btn_send_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.happyicon);
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,50,stream);
                byte[] imageBytes=stream.toByteArray();

                int subArraySize=400;

                sendReceiveForImage.writeData(String.valueOf(imageBytes.length).getBytes());

                for(int i=0;i<imageBytes.length;i+=subArraySize){
                    byte[] tempArray;
                    tempArray= Arrays.copyOfRange(imageBytes,i,Math.min(imageBytes.length,i+subArraySize));
                    sendReceiveForImage.writeData(tempArray);
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string= String.valueOf(et_message.getText());
                sendReceive.write(string.getBytes());
            }
        });

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass=new ClientClass(bluetoothDeviceArrayList.get(i));
                clientClass.start();

                tv_status.setText("Connecting");
            }
        });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                arrayList.clear();
                Set<BluetoothDevice> devices = myBluetoothAdapter.getBondedDevices();
                if(devices.size() > 0)
                {
                    for (BluetoothDevice device : devices) {
                        arrayList.add(device.getName());
                        bluetoothDeviceArrayList.add(device);
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void startBluetoothConnection() {

        if(myBluetoothAdapter != null)
            if(myBluetoothAdapter.isEnabled())
            {
                Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetoothIntent,REQUEST_ENABLE_BT);
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT)
        {
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

    Handler myHandler =  new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    tv_status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    tv_status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    tv_status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    tv_status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    tv_message.setText(tempMsg);
                    break;

                case STATE_IMAGE_RECEIVED:
                    byte[] readBuff1= (byte[]) msg.obj;
                    Bitmap bitmap=BitmapFactory.decodeByteArray(readBuff1,0,msg.arg1);

                    imageView.setImageBitmap(bitmap);
                    break;
            }
            return true;
        }
    });

    private void findViewByIds() {
        btn_create = findViewById(R.id.btn_create);
        btn_join = findViewById(R.id.btn_join);
        btn_send = findViewById(R.id.btn_send);
        list_view = findViewById(R.id.list_view);
        tv_message = findViewById(R.id.tv_message);
        tv_status = findViewById(R.id.tv_status);
        et_message = findViewById(R.id.et_message);
        btn_send_image = findViewById(R.id.btn_send_image);
        imageView = findViewById(R.id.imageView);
    }

    private class ServerClass extends Thread{
        private BluetoothServerSocket bluetoothServerSocket;

        public ServerClass()
        {
            try {
                bluetoothServerSocket =myBluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BluetoothSocket bluetoothSocket =null;

            while (bluetoothSocket==null)
            {

                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    myHandler.sendMessage(message);
                    bluetoothSocket = bluetoothServerSocket.accept();

                }catch (Exception e)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    myHandler.sendMessage(message);

                }

                if(bluetoothSocket != null)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    myHandler.sendMessage(message);

                    /*sendReceive=new SendReceive(bluetoothSocket);
                    sendReceive.start();*/

                    sendReceiveForImage =  new SendReceiveForImage(bluetoothSocket);
                    sendReceiveForImage.start();

                }
            }

        }
    }

    public class ClientClass extends Thread{

        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice _device)
        {

            device = _device;
            try {
                socket =device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                socket.connect();
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                myHandler.sendMessage(message);

                /*sendReceive=new SendReceive(socket);
                sendReceive.start();*/
                sendReceiveForImage = new SendReceiveForImage(socket);
                sendReceiveForImage.start();



            }catch (Exception e)
            {
                Message message=Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                myHandler.sendMessage(message);
            }

        }
    }

    public class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket _bluetoothSocket)
        {
            bluetoothSocket = _bluetoothSocket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try {
                    bytes=inputStream.read(buffer);
                    myHandler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class SendReceiveForImage extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceiveForImage (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn= bluetoothSocket.getInputStream();
                tempOut= bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=  tempIn;
            outputStream= tempOut;
        }

        public void run()
        {

            byte[] buffer = null;
            int numberOfBytes = 0;
            int index=0;
            boolean flag = true;

            while(true)
            {
                if(flag)
                {
                    try {
                        byte[] temp = new byte[inputStream.available()];
                        if(inputStream.read(temp)>0)
                        {
                            numberOfBytes=Integer.parseInt(new String(temp,"UTF-8"));
                            buffer=new byte[numberOfBytes];
                            flag=false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        byte[] data=new byte[inputStream.available()];
                        int numbers=inputStream.read(data);

                        System.arraycopy(data,0,buffer,index,numbers);
                        index=index+numbers;

                        if(index == numberOfBytes)
                        {
                            myHandler.obtainMessage(STATE_IMAGE_RECEIVED,numberOfBytes,-1,buffer).sendToTarget();
                            flag = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        public void writeData(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}