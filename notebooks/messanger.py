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

creds = read_yaml("credentials.yml")
camera_config = read_yaml("camera_config.yml")

# MQTT server environment variables
#HOSTNAME = socket.gethostname()
#IPADDRESS = socket.gethostbyname(HOSTNAME)
MQTT_HOST = "homeassistant"
MQTT_PORT = 1883
MQTT_KEEPALIVE_INTERVAL = 60
MQTT_USER = creds['user']
MQTT_PW = creds['password']
client_id = camera_config['camera_id']
QOS = 1 # quality of service

# callback function for MQTT
def on_connect(client, userdata, flags, rc):
    if rc==0:
        print("connected OK Returned code=",rc)
    else:
        print("Bad connection Returned code=",rc)

def on_publish(client, userdata, mid):
    print(" > published message: {}".format(mid))

def connect_mqtt():
    ### TODO: Connect to the MQTT client ###
    client = mqtt.Client(client_id)
    client.username_pw_set(MQTT_USER, MQTT_PW)
    client.on_connect=on_connect  # bind call back function
    client.on_publish = on_publish
    client.connect(MQTT_HOST, MQTT_PORT)#, MQTT_KEEPALIVE_INT ERVAL)
    return client

def publish_status(status):
    global client
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

image_name = 'portrait.jpg'

def publish_image():
    f=open(image_name, "rb") 
    fileContent = f.read()
    print(fileContent)
    byteArr = bytearray(fileContent)
    #publish.single('danger', byteArr, hostname=MQTT_HOST)
    client.publish(camera_config['camera_id'], byteArr)

    return fileContent


if __name__ == '__main__':
    client = connect_mqtt()
    print(client)
    client.loop_start()

    time.sleep(5)
    status = 'OK'
    #publish_status(status)
    publish_image()
    time.sleep(5)
    client.loop_stop()
    client.disconnect()
    