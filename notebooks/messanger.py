import os
import sys
import time
import socket
import json
import cv2
import time
import random
import yaml
from base64 import b64encode
from datetime import datetime

import logging as log
import paho.mqtt.client as mqtt
# import paho.mqtt.publish as publish

import utils


# def read_yaml(file_path):
#     with open(file_path, "r") as f:
#         return yaml.safe_load(f)

creds = utils.read_yaml("credentials.yml")
camera_config = utils.read_yaml("camera_config.yml")

# MQTT server environment variables
MQTT_HOST =creds['broker']
MQTT_PORT = creds['port']
MQTT_KEEPALIVE_INTERVAL = creds['keep_alive']
MQTT_USER = creds['user']
MQTT_PW = creds['password']
client_id = camera_config['camera_id']
QOS = creds['qos'] # quality of service

def create_timestamp():
    now = datetime.now()
    date_time = now.strftime("%Y%m%d_%H%M%S")
    #print(date_time)
    return date_time	

# callback function for MQTT
def on_connect(client, userdata, flags, rc):
    if rc==0:
        print("connected OK Returned code=",rc)
    else:
        print("Bad connection Returned code=",rc)

def on_publish(client, userdata, mid):
    #print(" > published message: {}".format(mid))
    pass

def connect_mqtt():
    ### TODO: Connect to the MQTT client ###
    client = mqtt.Client(client_id)
    client.username_pw_set(MQTT_USER, MQTT_PW)
    client.on_connect=on_connect  # bind call back function
    client.on_publish = on_publish
    client.connect(MQTT_HOST, MQTT_PORT)#, MQTT_KEEPALIVE_INT ERVAL)
    return client

def publish_status(status, client):
    #global client
    message ={
        'room_no': camera_config['room_no'],
        'camera_id': camera_config['camera_id'],
        'object_id': camera_config['object_id'],
        'object_name': camera_config['object_name'],
        'status': status
    }
    message = json.dumps(message, indent=4)
    #print(f"Publishing message: {message}")
    client.publish("danger", message)

    return message



def publish_image():
    f=open(image_name, "rb") 
    fileContent = f.read()
    #print(fileContent)
    byteArr = bytearray(fileContent)
    #publish.single('danger', byteArr, hostname=MQTT_HOST)
    client.publish(camera_config['camera_id'], byteArr)

    return fileContent



def publish_image_with_metadata(client, fileContent, status):
    now = datetime.now()
    #if frame.all() == None:

    #else:
    #     fileContent = frame
    # Base64 encode
    b64 = b64encode(fileContent)
    #byteArr = bytearray(fileContent)
    #publish.single('danger', byteArr, hostname=MQTT_HOST)
    # JSON-encode
    timestamp = create_timestamp()
    message = { 
        #"image" : bytearray(fileContent), # does not work because bytearray is not serializable
        "image": b64.decode("utf-8"),
        "filename": str(camera_config['camera_id'])+ "_" + str(now.strftime("%Y%m%d_%H%M%S")) + ".jpg",
        "timestamp": str(now.strftime("%H:%M:%S %d.%m.%Y")),
        'room_no': camera_config['room_no'],
        'camera_id': camera_config['camera_id'],
        'object_id': camera_config['object_id'],
        'object_name': camera_config['object_name'],
        'status': status
    }
    messageJSON = json.dumps(message)
    #client.publish(camera_config['camera_id'], byteArr)
    client.publish(camera_config['camera_id'], messageJSON)


    return fileContent
    