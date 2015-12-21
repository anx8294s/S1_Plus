package my.s1.app.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.android.volley.toolbox.ImageLoader;
import com.jakewharton.disklrucache.DiskLruCache;
import my.s1.app.MyApp;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyDiskCache implements ImageLoader.ImageCache {
    private final static Context context = MyApp.instance;
    private final static long MAX_SIZE = 30 * 1024 * 1024;
    private final static int IO_BUFFER_SIZE = 8 * 1024;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private DiskLruCache mDiskLruCache;

    public MyDiskCache() {
        try {
            mDiskLruCache = DiskLruCache.open(getDiskCacheDir(), getAppVersion(), 1, MAX_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        try {
            String key = hashKey(url);
            MyApp.myMemoryLruCache.put(key, bitmap);
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            OutputStream out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            bitmap.compress(mCompressFormat, 50, out);
            editor.commit();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bitmap getBitmap(String url) {
        Bitmap bitmap = null;
        try {
            String key = hashKey(url);
            bitmap = MyApp.myMemoryLruCache.get(key);
            if (bitmap != null) {
                return bitmap;
            }
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
            if (snapShot != null) {
                InputStream inputStream = snapShot.getInputStream(0);
                bitmap = BitmapFactory.decodeStream(inputStream);
                MyApp.myMemoryLruCache.put(key, bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private File getDiskCacheDir() {
        File cacheFile = context.getExternalCacheDir();
        String cachePath;
        if (cacheFile == null) {
            cachePath = context.getCacheDir().getPath();
        } else {
            cachePath = cacheFile.getAbsolutePath();
        }
        return new File(cachePath);
    }

    private int getAppVersion() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public String hashKey(String key) {
        key = key.replaceFirst("^#W\\d+#H\\d+#S\\d+", "");
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHex(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }


    private String bytesToHex(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public void flush() {
        try {
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
