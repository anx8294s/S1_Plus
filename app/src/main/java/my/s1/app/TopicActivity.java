package my.s1.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import my.s1.app.adapter.TopicListAdapter;
import my.s1.app.models.Topic;
import my.s1.app.util.MyHttpClient;
import my.s1.app.util.ParseHtml;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;

public class TopicActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener,
        SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    private int scrollState = SCROLL_STATE_IDLE;
    private boolean isFromRefresh;
    private String currentPage;

    private View footer;
    private ListView listView;
    private TopicListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Topic> topics = new ArrayList<Topic>();
    private HashMap<String, ArrayList<Topic>> pageMap = new HashMap<String, ArrayList<Topic>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_layout);
        listView = (ListView) findViewById(R.id.listview);
        ViewGroup parent = (ViewGroup) findViewById(R.id.container);
        footer = LayoutInflater.from(this).inflate(R.layout.footer, parent, false);
        footer.setVisibility(View.GONE);
        listView.addFooterView(footer);
        listView.setFooterDividersEnabled(false);
        adapter = new TopicListAdapter(this, R.layout.topic_item, topics);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(this);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.topic_swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);
        listView.setOnScrollListener(this);
        Intent intent = getIntent();
        currentPage = intent.getStringExtra("url");
        loadTopic(currentPage);
        MyApp.instance.setTopicActivity(this);
    }

    private void loadTopic(String url) {
        ArrayList<Topic> pageItems = pageMap.get(url);
        if (pageItems == null || pageItems.size() < 30) {
            pageMap.put(url, null);
            loadTopicFromNet(url);
        } else {
            if (isFromRefresh) {
                topics.clear();
                isFromRefresh = false;
                swipeRefreshLayout.setRefreshing(false);
            }
            topics.addAll(pageItems);
            currentPage = url;
            adapter.notifyDataSetChanged();
        }

    }

    private void loadTopicFromNet(final String url) {
        swipeRefreshLayout.setRefreshing(true);

        MyHttpClient.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MyHttpClient.onFailure(statusCode, headers, responseString, throwable);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                ArrayList<Topic> list = ParseHtml.parseTopic(responseString);
                ArrayList<Topic> pageItems = pageMap.get(url);
                if (footer.getVisibility() == View.GONE) {
                    footer.setVisibility(View.VISIBLE);
                }
                if (isFromRefresh) {
                    topics.clear();
                    isFromRefresh = false;
                }
                if (list.size() == 30 && topics.size() > 0) {
                    if (list.get(list.size() - 1).floor.equals(topics.get(topics.size() - 1).floor)) {
                        list.clear();
                    }
                }
                if (pageItems == null || topics.isEmpty()) {
                    topics.addAll(list);
                } else {
                    for (int i = pageItems.size(); i < list.size(); i++) {
                        topics.add(list.get(i));
                    }
                }
                pageMap.put(url, list);
                currentPage = (list.size() > 0) ? url : currentPage;
                swipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void checkTaskDone(int urlCount, int taskCount) {
        if ((urlCount == taskCount || taskCount > 5) && scrollState == SCROLL_STATE_IDLE) {
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onDestroy() {
        MyApp.myQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
        MyApp.myDiskCache.flush();
        MyApp.instance.setTopicActivity(null);
        super.onDestroy();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (view == footer) {
            listView.setSelection(0);
        } else {
            Topic topic = topics.get(position);
            startFastPost(topic.reply);
        }
        return true;
    }

    @Override
    public void onRefresh() {
        if (topics.isEmpty()) {
            loadTopicFromNet(currentPage);
            return;
        }
        int position = (topics.size() > 29) ? 29 : topics.size() - 1;
        String floor = topics.get(position).floor.replace("#", "");
        int pageCount = (Integer.valueOf(floor) + 29) / 30;
        pageCount = (pageCount > 1) ? pageCount - 1 : 1;
        isFromRefresh = true;
        String url = currentPage.replaceFirst("\\d+(-\\d+\\.html)", String.valueOf(pageCount) + "$1");
        loadTopic(url);
    }


    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        this.scrollState = scrollState;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount == totalItemCount && !swipeRefreshLayout.isRefreshing()) {
            if (pageMap.get(currentPage).size() == 30) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL && footer.getBottom() - 80 < listView.getHeight()) {
                    String floor = topics.get(topics.size() - 1).floor.replace("#", "");
                    int pageCount = Integer.valueOf(floor) / 30 + 1;
                    String url = currentPage.replaceFirst("\\d+(-\\d+\\.html)", String.valueOf(pageCount) + "$1");
                    loadTopic(url);
                }
            } else if (scrollState == SCROLL_STATE_FLING && listView.getHeight() == footer.getBottom()) {
                loadTopicFromNet(currentPage);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (swipeRefreshLayout.isRefreshing()) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.add_favorite) {
            addFavorite();
            return true;
        } else if (id == R.id.fast_post && !topics.isEmpty()) {
            Topic topic = topics.get(0);
            String url = topic.reply.replaceFirst("(tid=\\d+).+", "$1");
            startFastPost(url);

        }
        return super.onOptionsItemSelected(item);
    }

    private void startFastPost(String url) {
        Intent intent = new Intent(this, FastPostActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void addFavorite() {
        String topicId = currentPage.split("-")[1];
        String url = "http://bbs.saraba1st.com/2b/home.php?mod=spacecp&ac=favorite&type=thread&id="
                + topicId + "&infloat=yes&handlekey=k_favorite";
        swipeRefreshLayout.setRefreshing(true);
        MyHttpClient.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MyHttpClient.onFailure(statusCode, headers, responseString, throwable);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                HashMap<String, String> params = ParseHtml.parseAddFavoriteForm(responseString);
                if (params.containsKey("msg")) {
                    Toast.makeText(MyApp.instance, params.get("msg"), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    String url = params.get("url");
                    params.remove("url");
                    RequestParams requestParams = new RequestParams(params);
                    MyHttpClient.post(url, requestParams, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            MyHttpClient.onFailure(statusCode, headers, responseString, throwable);
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            HashMap<String, String> params = ParseHtml.parseAddFavoriteForm(responseString);
                            if (params.containsKey("msg")) {
                                String msg = params.get("msg");
                                if (msg.startsWith("信息收藏成功")) {
                                    msg = "信息收藏成功";
                                }
                                Toast.makeText(MyApp.instance, msg, Toast.LENGTH_SHORT).show();
                            }
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }
}
