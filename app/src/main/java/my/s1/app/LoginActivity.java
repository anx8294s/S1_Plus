package my.s1.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import my.s1.app.util.MyHttpClient;
import my.s1.app.util.ParseHtml;
import org.apache.http.Header;

import java.util.HashMap;


public class LoginActivity extends AppCompatActivity {

    @Bind(R.id.user_name) EditText username;
    @Bind(R.id.password) EditText password;
    @Bind(R.id.question_id) Spinner questionId;
    @Bind(R.id.answer) EditText answer;
    @Bind(R.id.login) Button login;
    @Bind(R.id.message) TextView message;
    private String postUrl;
    private HashMap<String, String> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        MyApp.clearCookie();
        getLoginForm();
    }

    private void getLoginForm() {
        String url = "http://bbs.saraba1st.com/2b/member.php?mod=logging&action=login";

        MyHttpClient.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MyHttpClient.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                params = ParseHtml.parseLoginForm(responseString);
                postUrl = params.get("url");
                params.remove("url");
                login.setVisibility(View.VISIBLE);
            }
        });
    }

    public void logTheUserIn(View view) {
        if (editTextIsEmpty(username) || editTextIsEmpty(password)) {
            return;
        } else if (questionId.getSelectedItemPosition() > 0 && editTextIsEmpty(answer)) {
            return;
        }
        RequestParams requestParams = new RequestParams(params);
        requestParams.put("loginfield", "username");
        requestParams.put("username", username.getText().toString());
        requestParams.put("password", password.getText().toString());
        requestParams.put("questionid", String.valueOf(questionId.getSelectedItemPosition()));
        requestParams.put("answer", answer.getText().toString());

        MyHttpClient.post(postUrl, requestParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MyHttpClient.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String msg = ParseHtml.parseLoginResult(responseString);
                if (msg.startsWith("如果") || msg.startsWith("欢迎")) {
                    onBackPressed();
                } else {
                    message.setText(msg);
                }
            }
        });
    }

    private boolean editTextIsEmpty(EditText text) {
        if (TextUtils.isEmpty(text.getText().toString())) {
            text.setError("请填写" + text.getHint());
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
