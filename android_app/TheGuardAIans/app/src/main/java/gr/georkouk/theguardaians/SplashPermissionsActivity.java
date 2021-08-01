package gr.georkouk.theguardaians;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;
import gr.georkouk.theguardaians.utils.PermissionUtil;


public class SplashPermissionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        checkAllPermissions();
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean checkAllPermissions(){

        final int permStorageStatus = PermissionUtil.checkPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );

        if(permStorageStatus == PermissionUtil.PERMISSION_GRANDED){

            this.setResult(RESULT_OK);
            this.finish();

            return true;
        }

        if(permStorageStatus == PermissionUtil.PERMISSION_DENIED){

            String message = "Please allow the application to access the storage so it can read/write the images from the notifications to work properly";

            new AlertDialog.Builder(this)
                    .setTitle("Permissions")
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            List<String> perms = new ArrayList<>();
                            perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                            String[] mStringArray = new String[perms.size()];
                            mStringArray = perms.toArray(mStringArray);

                            ActivityCompat.requestPermissions(
                                    SplashPermissionsActivity.this,
                                    mStringArray,
                                    999);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SplashPermissionsActivity.this.setResult(RESULT_CANCELED);
                            SplashPermissionsActivity.this.finish();
                        }
                    })
                    .show();
        }
        else{
            showManualPermissionsDialog();
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 999) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                this.setResult(RESULT_OK);
                this.finish();
            }
            else {
                // permission denied
                PermissionUtil.openPermissionSettings(this);
            }
        }
    }

    private void showManualPermissionsDialog(){
        String tmpMessage = "Please allow the application to access the storage so it can read/write the images from the notifications to work properly";

        new AlertDialog.Builder(this)
                .setTitle("Permissions")
                .setMessage(tmpMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PermissionUtil.openPermissionSettings(SplashPermissionsActivity.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SplashPermissionsActivity.this.setResult(RESULT_CANCELED);
                        SplashPermissionsActivity.this.finish();
                    }
                })
                .show();
    }

}
