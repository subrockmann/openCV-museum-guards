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

def read_yaml(file_path):
    with open(file_path, "r") as f:
        return yaml.safe_load(f)

creds = read_yaml("credentials.yml")

# MQTT server environment variables
HOSTNAME = socket.gethostname()
IPADDRESS = socket.gethostbyname(HOSTNAME)
MQTT_HOST = "homeassistant"
MQTT_PORT = 1883
MQTT_KEEPALIVE_INTERVAL = 60
MQTT_USER = creds['user']
MQTT_PW = creds['password']
client_id = "OAK-1"

# callback function for MQTT
def on_connect(client, userdata, flags, rc):
    if rc==0:
        print("connected OK Returned code=",rc)
    else:
        print("Bad connection Returned code=",rc)


def connect_mqtt():
    ### TODO: Connect to the MQTT client ###
    client = mqtt.Client(client_id)
    client.username_pw_set(MQTT_USER, MQTT_PW)
    client.on_connect=on_connect  # bind call back function
    client.connect(MQTT_HOST, MQTT_PORT)#, MQTT_KEEPALIVE_INT ERVAL)
    return client

if __name__ == '__main__':
    client = connect_mqtt()
    print(client)
    client.loop_start()

    time.sleep(5)
    client.publish("OAK","OFF")
    time.sleep(5)
    client.loop_stop()
    client.disconnect()
    #client.publish("house/main-light","OFF")