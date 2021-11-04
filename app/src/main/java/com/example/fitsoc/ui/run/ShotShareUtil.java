package com.example.fitsoc.ui.run;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.fitsoc.BuildConfig;
import com.example.fitsoc.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;



public class ShotShareUtil {
    private static final String TAG = "";
    private static Intent intent1 = new Intent(Intent.ACTION_VIEW);

    public static void shotShare(Context context) {
        String path = screenShot(context);

        if (!StringUtil.isEmpty(path)) {
            ShareImage(context, path);
        }
    }

    private static String screenShot(Context context) {
        String imagePath = null;
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(R.id.underView);
        Bitmap bitmap = ScreenUtil.getBitmapFromView(rootView);

        if (bitmap != null) {
            try {
                // path of pictures
                imagePath = context.getExternalFilesDir(null) + "share.png";
                Toast.makeText(context, imagePath, Toast.LENGTH_SHORT).show();
                File file = new File(imagePath);
//                File file = new File(context.getFilesDir(),"share.png");
//                context.getFilesDir().
//                Toast.makeText(context, context.getFilesDir(), Toast.LENGTH_SHORT).show();
//                FileOutputStream fos = context.openFileOutput("share.png",Context.MODE_PRIVATE);

                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                return imagePath;
            } catch (Exception e) {
                Toast.makeText(context, "====screenshot:error====", Toast.LENGTH_SHORT).show();
//                LogUtil.e(ShotShareUtil.class, "====screenshot:error====" + e.getMessage());
            }
        }
        return null;
    }

    private static void ShareImage(Context context, String imagePath) {
        if (imagePath != null) {
            Intent intent = new Intent(Intent.ACTION_SEND); // set share attribute
            File file = new File(imagePath);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID +".fileProvider",file);
//                intent.setDataAndType(contentUri,"application/vnd.android.package-archive");
                intent.setDataAndType(contentUri,"image/*");// set type of share message
            }

//            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getPackageName()+".fileProvider",file));//the content shared
            intent.setType("image/*");//set type of share message
            Intent chooser = Intent.createChooser(intent, "Share screen shot");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
            }
        } else {
            Toast.makeText(context , "take screeshot first，then share", Toast.LENGTH_SHORT).show();
        }

    }

    public static class StringUtil {
        public static boolean isEmpty(String str) {
            if (str == null || "null".equals(str) || str.length() == 0) {
                return true;

            } else {
                return false;

            }

        }
    }
}
