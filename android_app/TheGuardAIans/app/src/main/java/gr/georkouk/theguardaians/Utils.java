package gr.georkouk.theguardaians;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import gr.georkouk.theguardaians.models.Settings;
import gr.georkouk.theguardaians.utils.PermissionUtil;


public class Utils {

    public static boolean isJSONValid(String jsonInString) {
        try {
            Gson gson = new Gson();
            gson.fromJson(jsonInString, Object.class);
            return true;
        }
        catch(Exception ex) {
            return false;
        }
    }

    public static Settings readSettings(Context context){
        Settings settings = new Settings();

        SharedPreferences settingsPref =
                context.getSharedPreferences("Settings", Context.MODE_PRIVATE);

        settings.setServerURI(settingsPref.getString("serverURI", ""));
        settings.setClientID(settingsPref.getString("clientID", ""));
        settings.setUsername(settingsPref.getString("username", ""));
        settings.setPassword(settingsPref.getString("password", ""));
        settings.setTopic(settingsPref.getString("topic", ""));

        return settings;
    }

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean areAllPermissionsGranded(Context context){
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return PermissionUtil.arePermissionsGranded(context, permissions);
    }

}
