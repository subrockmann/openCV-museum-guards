import os
import sys
import time
import socket
import json
import cv2
import time
import random
from base64 import b64decode, b64encode
#import yaml
from pathlib import Path


import logging as log
import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish

import utils

creds = utils.read_yaml("monitor_credentials.yml")
data_folder = Path.cwd().parent.joinpath('data') 
print(f"data_folder: {data_folder}")
#os.path.join(os.path.dirname(os.getcwd()),'data')

MQTT_HOST =creds['broker']
MQTT_PORT = creds['port']
MQTT_KEEPALIVE_INTERVAL = creds['keep_alive']
MQTT_USER = creds['user']
MQTT_PW = creds['password']
client_id = creds['user']
QOS = creds['qos'] # quality of service

subscription_topics =['danger', 'OAK-1']

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    global subscription_topics
    print("Connected with result code "+str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    (result, mid) = client.subscribe('OAK-1', qos=1)
    print(mid)
    (result, mid) = client.subscribe('danger', qos=1)
    print(mid)

def extract_save_image(payload_json):
    #if payload_json['image'] and payload_json['filename']:
    if 'image' in payload_json.keys():
        print("Received an image!")
        filename = payload_json['filename']# + ".jpg"
        filename = data_folder.joinpath(filename)
        image = b64decode(payload_json['image'])
        #print 
        f = open(filename, "wb")
        f.write(image)
        f.close()


def on_message(client, userdata, msg):
    # more callbacks, etc
    # Create a file with write byte permission
    #print(f"Received a message!")
    message = json.loads(msg.payload)

    payload = msg.payload.decode("utf-8")
    payload_json = json.loads(payload)

    extract_save_image(payload_json)
    
    return payload_json # do callback functions need a return value?

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