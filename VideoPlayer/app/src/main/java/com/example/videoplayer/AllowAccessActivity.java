package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Util;

public class AllowAccessActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION = 101;
    public static final int REQUEST_PERMISSION_SETTING = 12;
    private static final int STORAGE_PERMISSION_ABOVE10 = 123;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_access);


        if (Util.SDK_INT < Build.VERSION_CODES.R) {
            if(ContextCompat.checkSelfPermission(AllowAccessActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(AllowAccessActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            } else {
                startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                finish();
            }
        } else {
            if (Environment.isExternalStorageManager()) {
                startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                finish();
            } else {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, STORAGE_PERMISSION_ABOVE10);
                } catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, STORAGE_PERMISSION_ABOVE10);
                }
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == STORAGE_PERMISSION) {
            for(int i = 0; i<permissions.length; i++) {
                String per = permissions[i];
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                }else{
                    boolean showRationale = shouldShowRequestPermissionRationale(per);
                    if(!showRationale) {
                        Toast.makeText(this, "This App will not work without permissions..", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("App Permission")
                                .setMessage("For playing videos you must allow this app to access video files on your device\n\n"
                                        + "Now follow the below steps:\n\n"
                                        + "-Open settings from below button\n"
                                        + "-Click on Permissions\n"
                                        + "-Allow access for the storage")
                                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                    }
                                }).create().show();

                    } else {
                        ActivityCompat.requestPermissions(AllowAccessActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSION_ABOVE10) {
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }
    }
}