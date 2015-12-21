package my.s1.app;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.LruCache;
import android.view.Display;
import android.view.WindowManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.PersistentCookieStore;
import my.s1.app.util.MyDiskCache;
import my.s1.app.util.MyHttpClient;

public class MyApp extends Application {
    public static MyApp instance;
    private static PersistentCookieStore cookieStore;
    public static RequestQueue myQueue;
    public static MyDiskCache myDiskCache;
    public static ImageLoader myImageLoader;
    public static LruCache<String, Bitmap> myMemoryLruCache;
    public static int screenWidth;
    public static int screenHeight;

    @Override
    public void onCreate() {
        instance = this;
        cookieStore = new PersistentCookieStore(instance);
        MyHttpClient.client.setCookieStore(cookieStore);
        MyHttpClient.client.setTimeout(6000);
        MyHttpClient.client.setMaxRetriesAndTimeout(2, 2000);
        myQueue = Volley.newRequestQueue(instance);
        myDiskCache = new MyDiskCache();
        myImageLoader = new ImageLoader(myQueue, myDiskCache);
        int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 3);
        myMemoryLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x - 80;
        screenHeight = size.y - 80;
    }

    public static void clearCookie() {
        cookieStore.clear();
    }

}
