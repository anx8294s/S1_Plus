package my.s1.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import my.s1.app.MyApp;
import org.apache.http.Header;

public class MyHttpClient {
    public static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, AsyncHttpResponseHandler responseHandler) {
        client.get(url, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    public static void onFailure(int statusCode, Header[] headers, String responseString, Throwable e) {
        ConnectivityManager manager = (ConnectivityManager) MyApp.instance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            Toast.makeText(MyApp.instance, "请检查网络连接...", Toast.LENGTH_SHORT).show();
        } else if (responseString == null) {
            Toast.makeText(MyApp.instance, "连接超时,请尝试刷新...", Toast.LENGTH_SHORT).show();
        } else if (statusCode == 502) {
            Toast.makeText(MyApp.instance, "服务器姨妈中...", Toast.LENGTH_SHORT).show();
        }
    }

}
