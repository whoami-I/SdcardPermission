package com.example.sdcardpermission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PermissionFragment extends Fragment {
    public static final String TAG = "PermissionFragment";

    public static final int SDCARD_PERMISSION_REQUEST_CODE = 9999;
    private static final String SDCARD_DOCUMENT_TREE_URI = "sdcard uri";

    private List<OnPermissionResultListener> mOnPermissionResultListeners = new ArrayList<>();
    private AlertDialog mOperationDialog;
    private Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public void requestSdcardPermission() {
        boolean canOperateSdcard = canOperateSdcardStorage(getActivity(), getSDCardName());
        if (canOperateSdcard) {
            String treeUri = getSdcardTreeUriPreferences(getActivity());
            if (TextUtils.isEmpty(treeUri)) {
                InternaleRequestdcardPermission();
                return;
            }
            for (OnPermissionResultListener l : mOnPermissionResultListeners) {
                l.onGranted(Uri.parse(treeUri));
            }
            return;
        }
        InternaleRequestdcardPermission();
    }

    /**
     * 是否有sd的写入权限
     *
     * @param context
     * @param sdCardName
     * @return
     */
    public static boolean canOperateSdcardStorage(Context context, String sdCardName) {
        boolean getUriTreePermission = false;
        List<UriPermission> listUP = context.getContentResolver().getPersistedUriPermissions();
        if (listUP != null && !listUP.isEmpty()) {
            for (int i = 0; i < listUP.size(); i++) {
                String uriPath = listUP.get(i).getUri().getPath();
                if (uriPath.endsWith("/tree/" + sdCardName + ":")) {
                    getUriTreePermission = true;
                    break;
                }
            }
        }
        return getUriTreePermission;
    }

    /**
     * get sdcard name
     *
     * @return sdcard name
     */
    public String getSDCardName() {
        StorageManager storageManager = (StorageManager) getActivity().getSystemService(Context.STORAGE_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            List<StorageVolume> storageVolumeList = storageManager.getStorageVolumes();
            for (StorageVolume storageVolume : storageVolumeList) {
                if (storageVolume.isRemovable()) {
                    return storageVolume.getUuid();
                }
            }
        }
        return null;
    }


    private void InternaleRequestdcardPermission() {
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N
                && android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            intent = createAccessIntent(getActivity());
            if (null != intent) {
                startActivityForResult(intent, SDCARD_PERMISSION_REQUEST_CODE);
            } else {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, SDCARD_PERMISSION_REQUEST_CODE);
            }
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, SDCARD_PERMISSION_REQUEST_CODE);
        }
    }

    private Intent createAccessIntent(Context context) {
        File file = new File(getSDCardName());
        StorageManager storageManager =
                (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume volume;
        if (null != storageManager) {
            volume = storageManager.getStorageVolume(file);
            if (null != volume) {
                return volume.createAccessIntent(null);
            }
        }
        return null;
    }


    public void addOnPermissionResultListener(OnPermissionResultListener l) {
        mOnPermissionResultListeners.add(l);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mOnPermissionResultListeners.clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SDCARD_PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri treeUri = data.getData();
            // save permission ,it is valid even reboot
            getActivity().getContentResolver().takePersistableUriPermission(treeUri, data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
            // save the uri， incase we can use it at other place
            saveSdcardTreeUriPreferences(getActivity(), treeUri.toString());
            Log.d(TAG, "get sdcard permission");
            for (OnPermissionResultListener l : mOnPermissionResultListeners) {
                l.onGranted(data.getData());
            }
        } else {
            for (OnPermissionResultListener l : mOnPermissionResultListeners) {
                l.onDenied();
            }
        }
    }

    public void saveSdcardTreeUriPreferences(Context context, String uri) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        preferences.edit().putString(SDCARD_DOCUMENT_TREE_URI, uri).apply();
    }

    public String getSdcardTreeUriPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return preferences.getString(SDCARD_DOCUMENT_TREE_URI, "");
    }

    public interface OnPermissionResultListener {
        void onGranted(Uri uri);

        void onDenied();
    }

}
