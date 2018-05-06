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
import android.content.Context;


public class DangerZone extends AppCompatActivity {

    TextView dataReceived;
    TextView engineTempData;
    TextView AFR_Data;
    TextView throttle_Data;
    TextView fuel_Data;
    TextView charge_Data;

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
        engineTempData = findViewById(R.id.engine_temperature);
        AFR_Data = findViewById(R.id.AFR);
        throttle_Data = findViewById(R.id.TPS);
        fuel_Data = findViewById(R.id.fuel);
        charge_Data = findViewById(R.id.charge);

        startMqtt();


        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString("server", null);
        if (restoredText != null) {
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, "Server: " + restoredText, duration).show();
        }

    }

    //starts mqtt
    private void startMqtt(){
        MQTTHelper helper = new MQTTHelper();
        helper.MqttHelper(getApplicationContext());
        helper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                //output message with topic
                String payload = mqttMessage.toString();
                float data = Float.parseFloat(payload.substring(payload.lastIndexOf(':') + 1));
                Log.w("Debug", topic + ": " + data);

                //set incoming data to update in UI.
                if(topic.equals("hybrid/engine/temperature")) {
                    String engine = data + " F";
                    engineTempData.setText(engine);
                }
                if(topic.equals("hybrid/engine/TPS")) {
                    String tps = data + "%";
                    throttle_Data.setText(tps);
                }
                if(topic.equals("hybrid/engine/AFR")) {
                    AFR_Data.setText(String.valueOf(data));
                }
                if(topic.equals("hybrid/dash/fuel")) {
                    String fuel = data + "%";
                    fuel_Data.setText(fuel);
                }
                if(topic.equals("hybrid/dash/charge")) {
                    String charge = data + "%";
                    charge_Data.setText(charge);
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

                            int duration = Toast.LENGTH_SHORT;
                            Context context = getApplicationContext();
                            Toast.makeText(context, "Server set to: " + editText.getText(), duration).show();

                            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                            editor.putString("server", editText.getText().toString());
                            editor.apply();
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
