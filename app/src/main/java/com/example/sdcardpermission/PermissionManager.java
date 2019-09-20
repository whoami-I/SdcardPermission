package com.example.sdcardpermission;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


public class PermissionManager {

    private static final String TAG = "PermissionManager";

    public static void requestSdcardPermission(AppCompatActivity activity, PermissionFragment.OnPermissionResultListener l) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment permissionFragment = fragmentManager.findFragmentByTag(PermissionFragment.TAG);
        PermissionFragment newFragment = null;
        //向当前activity植入fragment
        if (permissionFragment == null) {
            newFragment = new PermissionFragment();
        } else if (!(permissionFragment instanceof PermissionFragment)) {
            fragmentManager.beginTransaction().remove(permissionFragment).commitNow();
            newFragment = new PermissionFragment();
        } else {
            newFragment = (PermissionFragment) permissionFragment;
            fragmentManager.beginTransaction().remove(newFragment).commitNow();
        }
        newFragment.addOnPermissionResultListener(l);
        fragmentManager.beginTransaction().add(newFragment, PermissionFragment.TAG).commitNow();
        newFragment.requestSdcardPermission();
    }
}
