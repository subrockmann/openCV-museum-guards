package gr.georkouk.theguardaians;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.io.File;
import java.io.FileOutputStream;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gr.georkouk.theguardaians.adapter.MessageListAdapter;
import gr.georkouk.theguardaians.dao.DaoMessage;
import gr.georkouk.theguardaians.database.MyDB;
import gr.georkouk.theguardaians.models.Message;
import gr.georkouk.theguardaians.models.Settings;


@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_SPLASH_PERMISSIONS_CODE = 998;
    private static final String CHANNEL_ID = "999999";

    @BindView(R.id.fabConnect) FloatingActionButton fabConnect;
    @BindView(R.id.rvMessages) RecyclerView rvMessages;


    private MqttAndroidClient mqttClient;
    private Settings settings;
    private MessageListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MyDB.initializeInstance(getApplicationContext());
        MyDB.getInstance().open();

        this.settings = Utils.readSettings(this);
        createNotificationChannel();

        initializeView();
    }

    @Override
    public void onResume() {
        if(!Utils.areAllPermissionsGranded(this)){
            startActivityForResult(
                    new Intent(this, SplashPermissionsActivity.class),
                    ACTIVITY_SPLASH_PERMISSIONS_CODE
            );
        }

        this.adapter.swapData(
                DaoMessage.getMqttMessages()
        );

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.adapter.swapData(
                DaoMessage.getMqttMessages()
        );
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        openSettings();

        return super.onOptionsItemSelected(item);
    }

    private void openSettings(){
        startActivity(
                new Intent(this, ActivitySettings.class)
        );
    }

    @OnClick(R.id.fabConnect)
    public void fabConnectClick(View view){
        this.settings = Utils.readSettings(this);

        if(fabConnect.getBackgroundTintList()
                .equals(ColorStateList.valueOf(getResources().getColor(R.color.red_700)))){

            connectToServer();
        }
        else{
            disconnectFromServer();
        }
    }

    private void connectToServer(){
        mqttClient = new MqttAndroidClient(this, settings.getServerURI(), settings.getClientID());
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e("AAAAAAAAA===>", "connection lost");
                Utils.showToast(MainActivity.this, "Connection lost.");
                fabConnect.setBackgroundTintList(
                        ColorStateList.valueOf(getResources().getColor(R.color.red_700))
                );

                fabConnect.setImageResource(R.drawable.ic_baseline_link_off_36);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e("AAAAAAAAA===>", "messageArrived " + message.toString());

                if(Utils.isJSONValid(message.toString())) {
                    Gson gson = new Gson();
                    Message receivedMessage = gson.fromJson(message.toString(), Message.class);

                    if(receivedMessage == null
                            || TextUtils.isEmpty(receivedMessage.getRoomNumber())){
                        return;
                    }

                    receivedMessage.setTopic(topic);
                    receivedMessage.setId(
                            DaoMessage.saveMqttMessage(receivedMessage)
                    );

                    saveImageFile(receivedMessage);

                    sendNotification(receivedMessage);

                    adapter.swapData(
                            DaoMessage.getMqttMessages()
                    );
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.e("AAAAAAAAA===>", "deliveryComplete");
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setAutomaticReconnect(true);
//        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(settings.getUsername());
        mqttConnectOptions.setPassword(settings.getPassword().toCharArray());

        try {
            mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("AAAAAAAAA===>", "onSuccess connect");
                    Utils.showToast(MainActivity.this, "Connected.");

                    fabConnect.setBackgroundTintList(
                            ColorStateList.valueOf(getResources().getColor(R.color.lightGreen_700)));

                    fabConnect.setImageResource(R.drawable.ic_baseline_link_36);

                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("AAAAAAAAA===>", "onFailure connect");
                    Utils.showToast(MainActivity.this, "Connection problem.");
                    fabConnect.setBackgroundTintList(
                            ColorStateList.valueOf(getResources().getColor(R.color.red_700)));

                    fabConnect.setImageResource(R.drawable.ic_baseline_link_off_36);

                    exception.printStackTrace();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnectFromServer(){
        if(this.mqttClient != null){
            try {
                this.mqttClient.disconnect();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void subscribeToTopic(){
        try {
            mqttClient.subscribe(settings.getTopic(), 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("AAAAAAAAA===>", "onSuccess subscribe");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("AAAAAAAAA===>", "onFailure subscribe");
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "TheGuardians";
        String description = "Test";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendNotification(Message message){
        Intent notifyIntent = new Intent(this, NotificationActivity.class);

        Bundle bundle = new Bundle(1);
        bundle.putInt("id", message.getId());
        notifyIntent.putExtras(bundle);

        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        String tmp =
                "Room: " + message.getRoomNumber() + "\n" +
                "Object: " + message.getObjectName();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_museum_36)
                        .setContentTitle(message.getStatus())
                        .setContentText(tmp)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(notifyPendingIntent);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(MainActivity.this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(message.getId(), builder.build());
    }

    private void initializeView(){
        this.rvMessages.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        );

        this.adapter = new MessageListAdapter(this);
        this.rvMessages.setAdapter(adapter);

        this.adapter.setOnRowClickListener(message -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            Bundle bundle = new Bundle(1);
            bundle.putInt("id", message.getId());
            intent.putExtras(bundle);

            startActivityForResult(intent, 999);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveImageFile(Message message){
        if(TextUtils.isEmpty(message.getImage()) || TextUtils.isEmpty(message.getFilename())){
            return;
        }

        String imagesRootFolder = Environment.getExternalStorageDirectory() + "/theGuardAIans";
        File imagesFolder = new File(imagesRootFolder);

        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        File image = null;
        try{
            image = new File(imagesFolder, message.getFilename());
        }
        catch(Exception ignore){
        }

        byte[] decodedString = Base64.decode(message.getImage(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        FileOutputStream out = null;
        try {
            if(bitmap != null){
                out = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null){
                    out.flush();
                    out.close();
                }

                if(bitmap != null) {
                    bitmap.recycle();
                }
            }
            catch (Exception ignore) {
            }
        }
    }

}