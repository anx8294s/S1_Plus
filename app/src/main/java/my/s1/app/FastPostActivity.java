package my.s1.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import my.s1.app.util.MyHttpClient;
import my.s1.app.util.ParseHtml;
import org.apache.http.Header;

import java.util.HashMap;


public class FastPostActivity extends AppCompatActivity {
    @Bind(R.id.post_body) EditText editText;
    @Bind(R.id.post_done) Button button;
    private String postUrl;
    private HashMap<String, String> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast_post);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        loadReplyForm(url);
    }

    private void loadReplyForm(String url) {
        MyHttpClient.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MyHttpClient.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                params = ParseHtml.parseReplyForm(responseString);
                if (params.isEmpty()) {
                    Toast.makeText(MyApp.instance, "请登录您的账号再进行回复", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                postUrl = params.get("url");
                params.remove("url");
                button.setVisibility(View.VISIBLE);
            }
        });
    }

    public void postFastReply(View view) {
        if (TextUtils.isEmpty(editText.getText().toString())) {
            editText.setError("请填写回复内容");
            return;
        }
        RequestParams requestParams = new RequestParams(params);
        requestParams.put("message", editText.getText().toString());
        MyHttpClient.post(postUrl, requestParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MyHttpClient.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String msg = ParseHtml.parseLoginResult(responseString);
                if (msg.startsWith("非常感谢")) {
                    Toast.makeText(MyApp.instance, "发帖成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    editText.setText(msg);
                }
            }
        });
    }
}
