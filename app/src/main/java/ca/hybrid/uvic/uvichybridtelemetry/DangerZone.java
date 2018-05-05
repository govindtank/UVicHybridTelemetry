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

    public static final String MY_PREFS_NAME = "user_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic initialization.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger_zone);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Context context = getApplicationContext();

        //MQTT Bullshit
        dataReceived = (TextView) findViewById(R.id.dataReceived);
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
                Log.w("Debug",mqttMessage.toString());
                dataReceived.setText(mqttMessage.toString());
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
