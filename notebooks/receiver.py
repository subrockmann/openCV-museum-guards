import os
import sys
import time
import socket
import json
import cv2
import time
import random
import yaml

import logging as log
import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish

def read_yaml(file_path):
    with open(file_path, "r") as f:
        return yaml.safe_load(f)

creds = read_yaml("monitor_credentials.yml")
#camera_config = read_yaml("camera_config.yml")

MQTT_HOST = "homeassistant"
MQTT_PORT = 1883
MQTT_KEEPALIVE_INTERVAL = 60
MQTT_USER = creds['user']
MQTT_PW = creds['password']
client_id = creds['user']
QOS = 1 # quality of service

subscription_topics =['danger', 'OAK-1']


# # callback function for MQTT
# def on_connect(client, userdata, flags, rc):
#     if rc==0:
#         print("connected OK Returned code=",rc)
#     else:
#         print("Bad connection Returned code=",rc)

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    (result, mid) = client.subscribe('OAK-1', qos=1)
    print(mid)
    (result, mid) = client.subscribe('danger', qos=1)
    print(mid)

def on_message(client, userdata, msg):
    # more callbacks, etc
    # Create a file with write byte permission
    print(f"Received a message! {msg}")
    f = open('output.jpg', "wb")
    f.write(msg.payload)
    print("Image Received")
    f.close()

def connect_mqtt():
    ### TODO: Connect to the MQTT client ###
    client = mqtt.Client(client_id)
    client.username_pw_set(MQTT_USER, MQTT_PW)
    client.on_connect=on_connect  # bind call back function
    client.on_message=on_message
    client.connect(MQTT_HOST, MQTT_PORT)#, MQTT_KEEPALIVE_INT ERVAL)
    return client



if __name__ == '__main__':
    client = connect_mqtt()
    print(client)
    time.sleep(5)
    client.loop_forever()
    #client.loop_start()

    #time.sleep(5)
    #status = 'OK'
    #publish_status(status)
    #publish_image()
    #time.sleep(5)
    #client.loop_stop()
    #client.disconnect()