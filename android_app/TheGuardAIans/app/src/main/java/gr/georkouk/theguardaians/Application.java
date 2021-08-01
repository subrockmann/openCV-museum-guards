package gr.georkouk.theguardaians;

import android.content.Context;
import androidx.multidex.MultiDexApplication;


public class Application extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        Application.context = getApplicationContext();
    }

    public static Context getContext(){
        return Application.context;
    }

}
