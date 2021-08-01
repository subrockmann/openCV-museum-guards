package gr.georkouk.theguardaians;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;


public class NotificationActivity extends AppCompatActivity {

    public static int id = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

        if(getIntent().getExtras() != null){
            id = getIntent().getExtras().getInt("id", 0);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(id);
        }
    }

}
