import paho.mqtt.client as mqtt #import the client1
import time

broker_address="localhost"
client = mqtt.Client()
client.connect(broker_address)

i = 0.0
going_down = False

while(True):

	client.publish("hybrid/engine/AFR", 10.0 + i/4.0)
	client.publish("hybrid/engine/TPS", 0 + i * 10.0)
	client.publish("hybrid/engine/temperature", 170.0 + i * 2.0)
	client.publish("hybrid/dash/charge", 0 + i * 10.0)
	client.publish("hybrid/dash/fuel", 100.0 - i * 10.0)
	client.publish("hybrid/dash/GLVoltage", 10.0 + i * 0.4)

	print("data set sent for iteration value: " + str(i))

	if(going_down == False):
		i += 0.001
	else:
		i -= 0.001

	if(i >= 10):
		going_down = True
	if(i <= 0):
		going_down = False

	time.sleep(0.05)
