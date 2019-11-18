package com.example.noob.textdetector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

    public static void saveBitmap(Bitmap capturedBitmap, Context mContext, String name){
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOutputStream = null;
        File filedir = new File(path + "/Captures/");
        if (!filedir.exists()) {
            filedir.mkdirs();
        }
        File file = new File(filedir, name);
        try {
            fOutputStream = new FileOutputStream(file);

            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();

            MediaStore.Images.Media.insertImage(mContext.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Save Failed", Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Save Failed", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public static JSONArray loadJSONFromAsset(String name, Context mContext) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open(name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        JSONArray retObject = null;
        try {
            retObject = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retObject;
    }


    public static float getPixelsFromDp(Context mContext, float dip){
        Resources r = mContext.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        return px;
    }
}
