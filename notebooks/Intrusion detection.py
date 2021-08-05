#!/usr/bin/env python3

# This code is an adaption of "https://github.com/luxonis/depthai-python/tree/main/examples/26_1_spatial_mobilenet.py"


from pathlib import Path
import argparse
import sys
import cv2
import depthai as dai
import numpy as np
import time
from PIL import Image
from base64 import b64encode
import base64


from messanger import connect_mqtt, publish_status, publish_image_with_metadata

'''
Spatial detection network demo.
    Performs inference on RGB camera and retrieves spatial location coordinates: x,y,z relative to the center of depth map.
'''

# MobilenetSSD label texts
labelMap = ["background", "aeroplane", "bicycle", "bird", "boat", "bottle", "bus", "car", "cat", "chair", "cow",
            "diningtable", "dog", "horse", "motorbike", "person", "pottedplant", "sheep", "sofa", "train", "tvmonitor"]


# args
parser = argparse.ArgumentParser()
parser.add_argument('-hl', '--headless', action='store_true', help="Run camera in headless mode - no output to screen")
parser.add_argument('-l', '--led', action='store_true', help="Enable led indicators for Raspberry Pi")
args = parser.parse_args()

if args.headless:
    headless = args.headless
else:
    headless = False

if args.led:
    # setup Raspberry Pi for use of indicator LEDs

    import RPi.GPIO as GPIO
    GREEN_LED = 17
    RED_LED = 27
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(17, GPIO.OUT)
    GPIO.setup(27, GPIO.OUT)
else:
    pass

syncNN = True
white = (255, 255, 255)
color = (255, 255, 255)
red = (0, 0, 255)
green = (0, 255,0)
z_threshold = 1500 # set this threshold to raise an alarm if a person is closer
IMAGE_FREQUENCY = 15 # send image ever x frames

# Get argument first
model_dir = Path.cwd().parent.joinpath('models')
model_filename = 'mobilenet-ssd_openvino_2021.2_6shave.blob'
nnBlobPath = str(model_dir.joinpath(model_filename))
#print(nnBlobPath)

if not Path(nnBlobPath).exists():
    print(nnBlobPath)
    import sys
    raise FileNotFoundError(f'Required file/s not found, please run "{sys.executable} install_requirements.py"')

# Start defining a pipeline
pipeline = dai.Pipeline()

# Define a source - color camera
colorCam = pipeline.createColorCamera()
spatialDetectionNetwork = pipeline.createMobileNetSpatialDetectionNetwork()
monoLeft = pipeline.createMonoCamera()
monoRight = pipeline.createMonoCamera()
stereo = pipeline.createStereoDepth()

xoutRgb = pipeline.createXLinkOut()
xoutNN = pipeline.createXLinkOut()
xoutBoundingBoxDepthMapping = pipeline.createXLinkOut()
xoutDepth = pipeline.createXLinkOut()

xoutRgb.setStreamName("rgb")
xoutNN.setStreamName("detections")
xoutBoundingBoxDepthMapping.setStreamName("boundingBoxDepthMapping")
xoutDepth.setStreamName("depth")


colorCam.setPreviewSize(300, 300)
colorCam.setResolution(dai.ColorCameraProperties.SensorResolution.THE_1080_P)
colorCam.setInterleaved(False)
colorCam.setColorOrder(dai.ColorCameraProperties.ColorOrder.BGR)

monoLeft.setResolution(dai.MonoCameraProperties.SensorResolution.THE_400_P)
monoLeft.setBoardSocket(dai.CameraBoardSocket.LEFT)
monoRight.setResolution(dai.MonoCameraProperties.SensorResolution.THE_400_P)
monoRight.setBoardSocket(dai.CameraBoardSocket.RIGHT)

# Setting node configs
stereo.setOutputDepth(True)
stereo.setConfidenceThreshold(255)

spatialDetectionNetwork.setBlobPath(nnBlobPath)
spatialDetectionNetwork.setConfidenceThreshold(0.5)
spatialDetectionNetwork.input.setBlocking(False)
spatialDetectionNetwork.setBoundingBoxScaleFactor(1)
spatialDetectionNetwork.setDepthLowerThreshold(100)
spatialDetectionNetwork.setDepthUpperThreshold(5000)

# Create outputs

monoLeft.out.link(stereo.left)
monoRight.out.link(stereo.right)

colorCam.preview.link(spatialDetectionNetwork.input)
if syncNN:
    spatialDetectionNetwork.passthrough.link(xoutRgb.input)
else:
    colorCam.preview.link(xoutRgb.input)

spatialDetectionNetwork.out.link(xoutNN.input)
spatialDetectionNetwork.boundingBoxMapping.link(xoutBoundingBoxDepthMapping.input)

stereo.depth.link(spatialDetectionNetwork.inputDepth)
spatialDetectionNetwork.passthroughDepth.link(xoutDepth.input)

# setup MQTT
client = connect_mqtt()
client.loop_start()

# Pipeline is defined, now we can connect to the device
with dai.Device(pipeline) as device:

    # Start pipeline
    device.startPipeline()

    # Output queues will be used to get the rgb frames and nn data from the outputs defined above
    previewQueue = device.getOutputQueue(name="rgb", maxSize=4, blocking=False)
    detectionNNQueue = device.getOutputQueue(name="detections", maxSize=4, blocking=False)
    xoutBoundingBoxDepthMapping = device.getOutputQueue(name="boundingBoxDepthMapping", maxSize=4, blocking=False)
    depthQueue = device.getOutputQueue(name="depth", maxSize=4, blocking=False)

    frame = None
    detections = []

    startTime = time.monotonic()
    counter = 0
    fps = 0
    intrusion_counter = 0 

    while True:
        inPreview = previewQueue.get()
        inNN = detectionNNQueue.get()
        depth = depthQueue.get()

        counter+=1
        current_time = time.monotonic()
        if (current_time - startTime) > 1 :
            fps = counter / (current_time - startTime)
            counter = 0
            startTime = current_time

        frame = inPreview.getCvFrame()
        depthFrame = depth.getFrame()

        depthFrameColor = cv2.normalize(depthFrame, None, 255, 0, cv2.NORM_INF, cv2.CV_8UC1)
        depthFrameColor = cv2.equalizeHist(depthFrameColor)
        depthFrameColor = cv2.applyColorMap(depthFrameColor, cv2.COLORMAP_HOT)
        detections = inNN.detections
        if len(detections) != 0:
            boundingBoxMapping = xoutBoundingBoxDepthMapping.get()
            roiDatas = boundingBoxMapping.getConfigData()

            for roiData in roiDatas:
                roi = roiData.roi
                roi = roi.denormalize(depthFrameColor.shape[1], depthFrameColor.shape[0])
                topLeft = roi.topLeft()
                bottomRight = roi.bottomRight()
                xmin = int(topLeft.x)
                ymin = int(topLeft.y)
                xmax = int(bottomRight.x)
                ymax = int(bottomRight.y)

                cv2.rectangle(depthFrameColor, (xmin, ymin), (xmax, ymax), color, cv2.FONT_HERSHEY_SCRIPT_SIMPLEX)
        else: 
            color = green
            intrusion_counter = -1  ## brute force, 


        # If the frame is available, draw bounding boxes on it and show the frame
        height = frame.shape[0]
        width  = frame.shape[1]
        for detection in detections:
            # Denormalize bounding box
            x1 = int(detection.xmin * width)
            x2 = int(detection.xmax * width)
            y1 = int(detection.ymin * height)
            y2 = int(detection.ymax * height)

            #print(f"Detection z: {detection.spatialCoordinates.z}")
            try:
                label = labelMap[detection.label]
            except:
                label = detection.label
            if label == 'person':
                if args.led:
                    GPIO.output(GREEN_LED,True)
                if detection.spatialCoordinates.z < z_threshold:
                    color = red
                    #print(f"Detection z: {detection.spatialCoordinates.z}")
                    intrusion_counter +=1
                else: 
                    color = green
                    intrusion_counter = -1  ## brute force, 

                #cv2.putText(frame, str(label), (x1 + 10, y1 + 20), cv2.FONT_HERSHEY_TRIPLEX, 0.5, color)
                cv2.putText(frame, "{:.2f}".format(detection.confidence*100), (x1 + 10, max(0,y1 - 10)), cv2.FONT_HERSHEY_TRIPLEX, 0.5, color)
                #cv2.putText(frame, f"X: {int(detection.spatialCoordinates.x)} mm", (x1 + 10, y1 + 50), cv2.FONT_HERSHEY_TRIPLEX, 0.5, color)
                #cv2.putText(frame, f"Y: {int(detection.spatialCoordinates.y)} mm", (x1 + 10, y1 + 65), cv2.FONT_HERSHEY_TRIPLEX, 0.5, color)
                cv2.putText(frame, f"Z: {int(detection.spatialCoordinates.z)} mm", (x1 + 10, max(0,y2 - 10)), cv2.FONT_HERSHEY_TRIPLEX, 0.5, color)
                

                cv2.rectangle(frame, (x1, y1), (x2, y2), color, cv2.FONT_HERSHEY_SIMPLEX)
            
            else:
                if args.led:
                    GPIO.output(GREEN_LED,False)

        cv2.putText(frame, "NN fps: {:.2f}".format(fps), (frame.shape[1] - 100,  12), cv2.FONT_HERSHEY_TRIPLEX, 0.4, white)
        
        if headless == False:
            cv2.imshow("depth", depthFrameColor)
            cv2.imshow("rgb", frame)

        if (intrusion_counter >5) and (intrusion_counter % IMAGE_FREQUENCY ==0):
            # send message over mqtt including the frame
            print(intrusion_counter)
            status = 'Intrusion'


            #client.publish(status)
            #publish_status(status, client)

            retval, buffer = cv2.imencode('.jpg', frame)
            #jpg_as_text = base64.b64encode(buffer)
            #im_pil = Image.fromarray(frame)
            publish_image_with_metadata(client, buffer, status)
            #pass

        if args.led:
            if intrusion_counter <5:
                GPIO.output(RED_LED,False)
            elif intrusion_counter >10:
                GPIO.output(RED_LED,True)

        if cv2.waitKey(1) == ord('q'):
            if args.led:
                GPIO.cleanup()
            break
