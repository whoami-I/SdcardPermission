package com.example.sdcardpermission;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bt_save_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //直接申请权限
                PermissionManager.requestSdcardPermission(MainActivity.this, new PermissionFragment.OnPermissionResultListener() {
                    @Override
                    public void onGranted(Uri uri) {
                        //....
                    }

                    @Override
                    public void onDenied() {
                        Toast.makeText(MainActivity.this, "reject sdcard access", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
