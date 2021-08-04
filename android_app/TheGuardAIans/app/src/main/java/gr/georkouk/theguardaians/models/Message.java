package gr.georkouk.theguardaians.models;

import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("room_no")
    private String roomNumber;

    @SerializedName("camera_id")
    private String cameraId;

    @SerializedName("object_id")
    private String objectId;

    @SerializedName("object_name")
    private String objectName;

    private String status;

    private String image;

    private String timestamp;

    private String topic;

    private String filename;

    private int id;

    private boolean seen;


    public Message() {
        this.id = 0;
        this.roomNumber = "";
        this.cameraId = "";
        this.objectId = "";
        this.objectName = "";
        this.status = "";
        this.image = "";
        this.timestamp = "";
        this.topic = "";
        this.filename = "";
        this.seen = false;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

}
