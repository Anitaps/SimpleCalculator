package com.example.anita.mychatapp;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttToken;

import java.io.UnsupportedEncodingException;
import java.net.URI;

//Main activity and all declaration for variables
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView subText;
    public Button addbtn,minbtn,multbtn,divbtn;
    public EditText fnumber,secnumber;
    public TextView tvresult;
    public static MqttAndroidClient AnitaClient;
    MqttConnectOptions options;
    String clientId = MqttClient.generateClientId();

    @Override
    //where you create buttons and text boxes
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addbtn=(Button)findViewById(R.id.addbtn);
        multbtn=(Button)findViewById(R.id.multbtn);
        minbtn=(Button)findViewById(R.id.minbtn);
        divbtn=(Button)findViewById(R.id.divbtn);
        fnumber=(EditText)findViewById(R.id.etFirstNumber);
        secnumber=(EditText)findViewById(R.id.etSecondNumber);
        tvresult=(TextView)findViewById(R.id.tvResult);

        subText = (TextView) findViewById(R.id.subText);
        //adding actionlistener to the buttons
        addbtn.setOnClickListener(this);
        multbtn.setOnClickListener(this);
        minbtn.setOnClickListener(this);
        divbtn.setOnClickListener(this);
        //setting Mqtt client, obtaining client Id and connecting to the broker
        //transfering Client Id to Anitaclient
        MqttAndroidClient client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://prata.technocreatives.com:1883",
                        clientId);
        AnitaClient = client;
        //setting mqtt options(selecting version 3_1)
        options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

        //client gets connected to the broker
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this, "Connection Success!", Toast.LENGTH_LONG).show();
                    subscribe();
                }



                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "Failed to connect!", Toast.LENGTH_LONG).show();
                }
            });


        } catch (MqttException e) {
            e.printStackTrace();
        }

        //After connecting the client tries to get back information from the broker
        AnitaClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }
            //If there is some message on the broker the client gets it and then it is displayed on the subtext
            //and tvresult
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                subText.setText(new String(message.getPayload()));
                tvresult.setText(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

//Publish Method
    public void publish(String payload) {
        String topic = "Mylovely/calculator/AndroidMessage";
        byte[] encodedPayload = new byte[0];
        try {

            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(false);
            AnitaClient.publish(topic, message);

            Toast.makeText(MainActivity.this, "Msg published.", Toast.LENGTH_LONG).show();

        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
//subscribe Method
    private void subscribe() {
        String topic = "Mylovely/calculator/Timer";
        int qos = 1;  //subscribe
        try {
            IMqttToken subToken = AnitaClient.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
//not using this method anymore
    public void connect(View v) {
        try {
            IMqttToken token = AnitaClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this, "Connection Success!", Toast.LENGTH_LONG).show();
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "Failed to connect!", Toast.LENGTH_LONG).show();
                }
            });


        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
//not using this method anymore
    public void disconnect(View v){

        try {
            IMqttToken token = AnitaClient.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this, "Disconnected!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "Failed, still connected!", Toast.LENGTH_LONG).show();
                }
            });


        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
//Parsing information from the textfields so you get the number from the fnumber and secnumber, Andvar stores the expression
    public void onClick(View v) {
        String num1 = fnumber.getText().toString();
        String num2 = secnumber.getText().toString();
        String AndVar="";
        switch (v.getId()) {
            case R.id.addbtn:

                AndVar=num1+"+"+num2;

                break;
            case R.id.multbtn:
                AndVar=num1+"*"+num2;

                break;
            case R.id.minbtn:
                AndVar=num1+"-"+num2;

                break;
            case R.id.divbtn:
                AndVar=num1+"/"+num2;
                try {

                } catch (Exception e) {


                }
                break;
        }
// calling publish method and sending Andvar as payload containing the expression
        publish(AndVar);

    }
}

