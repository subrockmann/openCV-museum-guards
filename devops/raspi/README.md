# Setting up Raspi Zero W for OAK-D

- Download the latest Rasbi lite image from the official homepage.
- Flash the image to a SD card
- Enable ssh: inside the "boot" folder create an empty file "ssh" without an extension
- Enable wifi: inside the "boot" folder create a file "wpa_supplicant.conf". Place the following code inside the file and replace the placeholders accordingly:

```
country=US
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1

network={
ssid="WIFI_SSID"
scan_ssid=1
psk="WIFI_PASSWORD"
key_mgmt=WPA-PSK
}
```
- Insert the microSD card int the Pi and power it via USB. After 30 - 90 seconds the Pi should be connected to the network.

Login in to Raspi with user: pi and password: raspberry  

## Updating the sytem
```
sudo apt-get update 
sudo apt-get upgrade -y
sudo pat-get dist-upgrade -y

```
## Installing software packages
sudo apt-get install python3-pip

Upgrading pip:
python3 
-m pip install -U pip

Installing opencv:
sudo apt install libqtgui4
sudo apt install libqt4-test
sudo pip3 install opencv-python

Install MQTT:
sudo pip3 install paho-mqtt

Install depthai:
sudo pip3 install depthai

Install GPIO libraries:
sudo pip3 install adafruit-blinka

Configure I2C:
sudo apt-get install -y pyton-smbus
sudo apt-get install -y i2c-tools

sudo raspi-config
3 - Interface Options -> P5 I2C

### Check for all the connected I2C devices:
sudo i2cdetect -y 1


## General configuration
sudo raspi-config


enable camera
enable autologin
set keyboard and language options

6-advanced options -> A1 Expand Filesystem
5-localization option -> L2 timezone
  
## Get the IP adress of your Raspi
In the rapi terminal enter:
hostname -I



