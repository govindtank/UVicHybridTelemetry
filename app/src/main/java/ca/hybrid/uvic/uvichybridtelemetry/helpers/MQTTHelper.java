package ca.hybrid.uvic.uvichybridtelemetry.helpers;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import android.util.Log;
import android.content.Context;

public class MQTTHelper {
    //set basics
    public MqttAndroidClient mqttAndroidClient;
    //default server we used for testing
    public String server_address = "tcp://test.mosquitto.org";
    final String clientId = "ExampleAndroidClient";
    //topic we are listening to through MQTT
    final String subscriptionTopic = "hybrid/#";
    //User/pass weren't aren't being used in our current usage case.
    final String username = "";
    final String password = "";

    //helper method, receives incoming context and server_address (from prefs) from DangerZone
    public void MqttHelper(Context context, String server_address){
        mqttAndroidClient = new MqttAndroidClient(context, server_address, clientId);
        //starts callback process to log connection changes, and incoming data
        //and ultimately call the connect function.
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("MQTT", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("MQTT", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    //method to connect
    private void connect(){
        //sets MQTT options
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        //User/Pass weren't needed for our configuration.
        //mqttConnectOptions.setUserName(username);
        //mqttConnectOptions.setPassword(password.toCharArray());

        //tries to connect, if successful sets options and subscribes to topic
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("MQTT", "Failed to connect to: " + server_address + exception.toString());
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    //method to subscribe to new topic.

    //Could potentially be used to switch which topics you are listening to through
    //dropdown element like the server switcher. Would need to create an unsubscribe
    //to topic function to get that fully working however.
    //Better idea would be to create a popup box from an option in drop down to view
    //current topics, from that box you could have an + button to add, and a remove button
    //beside each currently subscribed topic.

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("MQTT","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("MQTT", "Subscribe fail!");
                }
            });
        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }
}

