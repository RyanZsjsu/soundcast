package com.ahmilio.turtle.soundcast;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.content.Intent;
import android.app.Activity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter<String> listAdapter;
    Button connectNew;
    ListView listView;
    BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        if(btAdapter == null) //Check to see device has bluetooth
        {
            //Shows message for LONG period of time
            Toast.makeText(getApplicationContext(), "Device does not have Bluetooth",Toast.LENGTH_LONG).show();
            finish();
        }
        else{
            if(!btAdapter.isEnabled()){
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent,1 );
            }
        }

    }
    private void init()
    {
        connectNew =(Button)findViewById(R.id.bConnectNew);
        listView=(ListView)findViewById(R.id.listView);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        listView.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

    }
    protexted void onActivityResult(int requestCode, resultCode, data)
    {

    }
}
