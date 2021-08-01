package gr.georkouk.theguardaians.dao;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import gr.georkouk.theguardaians.database.MyDB;
import gr.georkouk.theguardaians.models.Message;


public class DaoMessage {

    public static final int NEXT_MESSAGE = 0;
    public static final int PREVIOUS_MESSAGE = 1;

    public static int saveMqttMessage(Message message){
        ContentValues cv = new ContentValues();

        cv.put("topic", message.getTopic());
        cv.put("roomNumber", message.getRoomNumber());
        cv.put("cameraId", message.getCameraId());
        cv.put("objectId", message.getObjectId());
        cv.put("objectName", message.getObjectName());
        cv.put("status", message.getStatus());
        cv.put("timestamp", message.getTimestamp());
        cv.put("filename", message.getFilename());

        MyDB db = MyDB.getInstance().open();
        long id = db.insert(cv, "mqttMessage");
        MyDB.getInstance().close();

        return (int) id;
    }

    public static List<Message> getMqttMessages(){
        List<Message> messages = new ArrayList<>();
        String sql = "select * from mqttMessage where seen = 0 order by mqttMessage desc";

        MyDB db = MyDB.getInstance().open();

        Cursor cursor = db.runSqlCursor(sql);
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Message message = new Message();
                message.setId(cursor.getInt(cursor.getColumnIndex("mqttMessage")));
                message.setTopic(cursor.getString(cursor.getColumnIndex("topic")));
                message.setRoomNumber(cursor.getString(cursor.getColumnIndex("roomNumber")));
                message.setCameraId(cursor.getString(cursor.getColumnIndex("cameraId")));
                message.setObjectId(cursor.getString(cursor.getColumnIndex("objectId")));
                message.setObjectName(cursor.getString(cursor.getColumnIndex("objectName")));
                message.setStatus(cursor.getString(cursor.getColumnIndex("status")));
                message.setTimestamp(cursor.getString(cursor.getColumnIndex("timestamp")));
                message.setFilename(cursor.getString(cursor.getColumnIndex("filename")));
                message.setSeen(cursor.getInt(cursor.getColumnIndex("seen")) == 1);

                messages.add(message);
            }
        }

        MyDB.getInstance().close();

        return messages;
    }

    public static Message getMqttMessage(int id){
        Message message = new Message();

        String sql = "select * from mqttMessage where mqttMessage = " + id;

        MyDB db = MyDB.getInstance().open();

        Cursor cursor = db.runSqlCursor(sql);
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                message.setId(cursor.getInt(cursor.getColumnIndex("mqttMessage")));
                message.setTopic(cursor.getString(cursor.getColumnIndex("topic")));
                message.setRoomNumber(cursor.getString(cursor.getColumnIndex("roomNumber")));
                message.setCameraId(cursor.getString(cursor.getColumnIndex("cameraId")));
                message.setObjectId(cursor.getString(cursor.getColumnIndex("objectId")));
                message.setObjectName(cursor.getString(cursor.getColumnIndex("objectName")));
                message.setStatus(cursor.getString(cursor.getColumnIndex("status")));
                message.setTimestamp(cursor.getString(cursor.getColumnIndex("timestamp")));
                message.setFilename(cursor.getString(cursor.getColumnIndex("filename")));
                message.setSeen(cursor.getInt(cursor.getColumnIndex("seen")) == 1);
            }
        }

        MyDB.getInstance().close();

        return message;
    }

    public static Message getPreviousOrNextMqttMessage(int id, int type){
        Message message = new Message();

        String sql;
        if(type == NEXT_MESSAGE){
            sql = "select * from mqttMessage " +
                    " where seen = 0 and mqttMessage > " + id +
                    " order by mqttMessage limit 1";
        }
        else{
            sql = "select * from mqttMessage " +
                    " where seen = 0 and mqttMessage < " + id +
                    " order by mqttMessage desc limit 1";
        }

        MyDB db = MyDB.getInstance().open();

        Cursor cursor = db.runSqlCursor(sql);
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                message.setId(cursor.getInt(cursor.getColumnIndex("mqttMessage")));
                message.setTopic(cursor.getString(cursor.getColumnIndex("topic")));
                message.setRoomNumber(cursor.getString(cursor.getColumnIndex("roomNumber")));
                message.setCameraId(cursor.getString(cursor.getColumnIndex("cameraId")));
                message.setObjectId(cursor.getString(cursor.getColumnIndex("objectId")));
                message.setObjectName(cursor.getString(cursor.getColumnIndex("objectName")));
                message.setStatus(cursor.getString(cursor.getColumnIndex("status")));
                message.setTimestamp(cursor.getString(cursor.getColumnIndex("timestamp")));
                message.setFilename(cursor.getString(cursor.getColumnIndex("filename")));
                message.setSeen(cursor.getInt(cursor.getColumnIndex("seen")) == 1);
            }
        }

        MyDB.getInstance().close();

        return message;
    }

    public static void setMessageSeen(int id){
        ContentValues cv = new ContentValues();
        cv.put("seen", 1);

        MyDB db = MyDB.getInstance().open();
        db.update(cv, "mqttMessage", "mqttMessage = " + id);
        MyDB.getInstance().close();
    }

}
