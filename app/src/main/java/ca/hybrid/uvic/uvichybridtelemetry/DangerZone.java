package ca.hybrid.uvic.uvichybridtelemetry;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;
import android.content.SharedPreferences;
import ca.hybrid.uvic.uvichybridtelemetry.helpers.MQTTHelper;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import android.util.Log;
import pl.pawelkleczkowski.customgauge.CustomGauge;
import android.graphics.Color;

public class DangerZone extends AppCompatActivity {

    //place holder spots where incoming data will be stored
    //then used to set various UI elements, or passed to different classes.
    CustomGauge coolant_gauge;
    CustomGauge afr_gauge;
    CustomGauge throttle_gauge;
    CustomGauge fuel_gauge;
    CustomGauge charge_gauge;
    CustomGauge voltage_gauge;
    TextView coolant_text;
    TextView afr_text;
    TextView throttle_text;
    TextView fuel_text;
    TextView charge_text;
    TextView voltage_text;

    public String server_address;

    public static final String MY_PREFS_NAME = "user_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic initialization.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger_zone);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Context context = getApplicationContext();

        //Set paths for incoming data.
        coolant_gauge = findViewById(R.id.coolantData);
        coolant_text = findViewById(R.id.coolantText);
        afr_gauge = findViewById(R.id.afrData);
        afr_text = findViewById(R.id.afrText);
        throttle_gauge = findViewById(R.id.tpsData);
        throttle_text = findViewById(R.id.tpsText);
        fuel_gauge = findViewById(R.id.fuelData);
        fuel_text = findViewById(R.id.fuelText);
        charge_gauge = findViewById(R.id.chargeData);
        charge_text = findViewById(R.id.chargeText);
        voltage_gauge = findViewById(R.id.voltageData);
        voltage_text = findViewById(R.id.voltageText);

        // sets prefs location
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString("server", null);
        //if statement displays popup on startup for current server from prefs.
        if (restoredText != null) {
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, "Server: " + restoredText, duration).show();
        }

        //grabs server address from prefs
        server_address = prefs.getString("server", "tcp://test.mosquitto.org");
        //starts connection
        startMqtt();
    }

    //starts mqtt
    private void startMqtt(){
        //creates the MQTT helper object
        MQTTHelper helper = new MQTTHelper();
        //passes the MQTT helper object the application context and server address.
        helper.MqttHelper(getApplicationContext(), "tcp://"+server_address);

        helper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                //output message with topic to log
                String payload = mqttMessage.toString();
                float data = Float.parseFloat(payload.substring(payload.lastIndexOf(':') + 1));
                Log.w("Debug", topic + ": " + data);

                //checks topic from incoming message, then outputs it to the corresponding GUI element
                if(topic.equals("hybrid/engine/temperature")) {
                    String engine = data + " F";
                    //sets gauges
                    coolant_gauge.setValue((int)data);
                    coolant_text.setText(engine);
                    if (data>187) {
                        coolant_gauge.setPointEndColor(Color.parseColor("#ff0000"));
                        coolant_gauge.setPointStartColor(Color.parseColor("#ff0000"));
                    }
                    if(data<186){
                        coolant_gauge.setPointEndColor(Color.parseColor("#0000ff"));
                        coolant_gauge.setPointStartColor(Color.parseColor("#0000ff"));
                    }
                }
                if(topic.equals("hybrid/engine/TPS")) {
                    String tps = String.format("%.0f", data) + "%";
                    throttle_gauge.setValue((int)data);
                    throttle_text.setText(tps);
                }
                if(topic.equals("hybrid/engine/AFR")) {
                    afr_text.setText(String.valueOf(String.format("%.1f", data)));
                    afr_gauge.setValue((int)data);
                    if (data<11) {
                        afr_gauge.setPointStartColor(Color.parseColor("#ffff00"));
                        afr_gauge.setPointEndColor(Color.parseColor("#ffff00"));
                    }
                    if (data>14.9) {
                        afr_gauge.setPointStartColor(Color.parseColor("#ff00000"));
                        afr_gauge.setPointEndColor(Color.parseColor("#ff0000"));
                    }
                    if (data>11.1 && data<14.8) {
                        afr_gauge.setPointStartColor(Color.parseColor("#0000ff"));
                        afr_gauge.setPointEndColor(Color.parseColor("#0000ff"));
                    }
                }
                if(topic.equals("hybrid/dash/fuel")) {
                    String fuel = String.format("%.0f", data) + "%";
                    fuel_text.setText(fuel);
                    fuel_gauge.setValue((int)data);
                    if (data<20) {
                        fuel_gauge.setPointStartColor(Color.parseColor("#ffff00"));
                        fuel_gauge.setPointEndColor(Color.parseColor("#ffff00"));
                    }
                    if (data>21) {
                        fuel_gauge.setPointStartColor(Color.parseColor("#0000ff"));
                        fuel_gauge.setPointEndColor(Color.parseColor("#0000ff"));
                    }
                }
                if(topic.equals("hybrid/dash/charge")) {
                    String charge = String.format("%.0f", data) + "%";
                    charge_text.setText(charge);
                    charge_gauge.setValue((int)data);
                }
                if(topic.equals("hybrid/dash/GLVoltage")) {
                    String voltage = String.format("%.1f", data) + "V";
                    voltage_gauge.setValue((int)data);
                    voltage_text.setText(voltage);
                    if (data>14 || data<10) {
                        voltage_gauge.setPointStartColor(Color.parseColor("#ff00000"));
                        voltage_gauge.setPointEndColor(Color.parseColor("#ff0000"));
                    }
                    if (data>10.1 && data<13.9) {
                        voltage_gauge.setPointStartColor(Color.parseColor("#0000ff"));
                        voltage_gauge.setPointEndColor(Color.parseColor("#0000ff"));
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_danger_zone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.select_server) {
            // get prompts.xml view
            LayoutInflater layoutInflater = LayoutInflater.from(DangerZone.this);
            View promptView = layoutInflater.inflate(R.layout.select_server, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DangerZone.this);
            alertDialogBuilder.setView(promptView);

            final EditText editText = promptView.findViewById(R.id.server_address);
            // setup a dialog window
            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //creates message saying the server is set to "input"
                            int duration = Toast.LENGTH_SHORT;
                            Context context = getApplicationContext();
                            Toast.makeText(context, "Server set to: " + editText.getText(), duration).show();
                            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                            //places the new server address in prefs
                            editor.putString("server", editText.getText().toString());
                            editor.apply();
                            //sets public server_address element to newly passed server value
                            //then restarts MQTT using new server value
                            server_address=editText.getText().toString();
                            startMqtt();
                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create an alert dialog
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
