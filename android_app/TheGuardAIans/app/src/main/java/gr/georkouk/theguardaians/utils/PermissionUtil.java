package gr.georkouk.theguardaians.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import java.util.List;


@SuppressWarnings("WeakerAccess")
public class PermissionUtil {

    public static final int PERMISSION_GRANDED = 1000;
    public static final int PERMISSION_DENIED = 1001;
    public static final int PERMISSION_DISABLED = 1002;


    @TargetApi(Build.VERSION_CODES.M)
    public static int checkPermission(Activity activity, String permission){
        /*
         * If permission is not granted
         * */
        if (shouldAskPermission(activity, permission)){
            /*
             * If permission denied previously
             * */
            if (activity.shouldShowRequestPermissionRationale(permission)) {
                return PERMISSION_DENIED;
            }
            else {
                /*
                 * Permission denied or first time requested
                 * */
                if (isFirstTimeAskingPermission(activity, permission)) {
                    setFirstTimeAskingPermission(activity, permission);

                    return PERMISSION_DENIED;
                }
                else {
                    /*
                     * Handle the feature without permission or ask user to manually allow permission
                     * */
                    return PERMISSION_DISABLED;
                }

            }

        }
        else {
            return PERMISSION_GRANDED;
        }

    }

    public static boolean arePermissionsGranded(Context context, List<String> permissions){
        boolean granded = true;

        for(String permission : permissions){
            if(shouldAskPermission(context, permission)){
                granded = false;

                break;
            }
        }

        return granded;
    }

    public static void openPermissionSettings(Context context){
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        context.startActivity(intent);
    }

    public static boolean shouldAskPermission(Context context, String permission){
        if (shouldAskPermission()) {
            int permissionResult = ActivityCompat.checkSelfPermission(context, permission);

            return permissionResult != PackageManager.PERMISSION_GRANTED;
        }

        return false;
    }

    private static boolean shouldAskPermission() {
        return true;
    }

    private static void setFirstTimeAskingPermission(Context context, String permission){
        SharedPreferences.Editor settings =
               context.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit();

        settings.putBoolean(permission, false);
        settings.apply();
    }

    private static boolean isFirstTimeAskingPermission(Context context, String permission){
        SharedPreferences settingsPref =
                context.getSharedPreferences("Settings", Context.MODE_PRIVATE);

        return settingsPref.getBoolean(permission, true);
    }

}
