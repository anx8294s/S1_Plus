package my.s1.app.util;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import my.s1.app.MyApp;
import my.s1.app.models.MainForumItem;
import my.s1.app.models.SubForumItem;
import my.s1.app.models.Topic;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class ParseHtml {
    private static PriorityQueue<MainForumItem> priorityQueue = new PriorityQueue<MainForumItem>(
            20, new Comparator<MainForumItem>() {
        @Override
        public int compare(MainForumItem item1, MainForumItem item2) {
            return Integer.parseInt(item2.num) - Integer.parseInt(item1.num);
        }
    }
    );
    private static final String BASE_URI = "http://bbs.saraba1st.com/2b/";

    public static ArrayList<MainForumItem> parseMainForum(String response) {
        ArrayList<MainForumItem> arrayList = new ArrayList<MainForumItem>();
        Document document = Jsoup.parse(response);
        Element contents = document.getElementById("category_1");
        Elements links = contents.getElementsByTag("h2");
        contents = document.getElementById("category_117");
        links.addAll(contents.getElementsByTag("dt"));
        for (Element link : links) {
            String url = BASE_URI + link.child(0).attr("href");
            String forumName = link.child(0).text();
            String num = (link.children().size() > 1) ? link.child(1).text().replaceAll("[()]", "") : "0";
            MainForumItem item = new MainForumItem(url, forumName, num);
            priorityQueue.add(item);
        }
        while (!priorityQueue.isEmpty()) {
            arrayList.add(priorityQueue.poll());
        }
        return arrayList;
    }

    public static ArrayList<SubForumItem> parseSubForum(String response) {
        ArrayList<SubForumItem> arrayList = new ArrayList<SubForumItem>();
        Document document = Jsoup.parse(response);
        Element contents = document.getElementById("threadlisttableid");
        Elements topics = contents.getElementsByTag("tbody");
        for (Element topic : topics) {
            if (topic.id().startsWith("normalthread")) {
                topic = topic.child(0);
                Element e = topic.child(1).select("a[onclick]").first();
                String url = BASE_URI + e.attr("href");
                String title = e.text();
                String postBy = topic.child(2).child(0).child(0).text();
                String num = topic.child(3).child(0).text();
                SubForumItem item = new SubForumItem(url, title, postBy, num);
                arrayList.add(item);
            }
        }
        return arrayList;
    }

    public static ArrayList<Topic> parseTopic(String response) {
        ArrayList<Topic> arrayList = new ArrayList<Topic>();
        HashSet<String> imgUrls = new HashSet<String>();
        Document document = Jsoup.parse(response);
        Element contents = document.getElementById("postlist");
        Elements topics = contents.getElementsByClass("plhin");
        for (Element topic : topics) {
            Element name = topic.getElementsByClass("xw1").first();
            Element body = topic.getElementsByClass("t_f").first();
            Element time = topic.select("em[id^=authorposton]").first();
            Element floor = topic.select("a[id^=postnum]").first();
            Element fastRe = topic.select("a[class=fastre]").first();
            String reply = BASE_URI + fastRe.attr("href");
            if (body == null) {
                body = topic.getElementsByClass("locked").first();
            }
            Elements images = body.getElementsByTag("img");
            for (Element img : images) {
                if (img.hasAttr("src") && img.attr("src").startsWith("static")) {
                    img.attr("src", BASE_URI + img.attr("src"));
                } else if (img.hasAttr("file")) {
                    if (img.attr("file").startsWith("static")) {
                        img.attr("file", BASE_URI + img.attr("file"));
                    }
                    img.attr("src", img.attr("file"));
                }
                imgUrls.add(img.attr("src"));
            }
            Topic topicItem = new Topic(name.text(), body.html(), time.text(), floor.text(), reply);
            arrayList.add(topicItem);
        }
        doWithImages(imgUrls);
        if (!arrayList.isEmpty() && arrayList.get(0).floor.equals("楼主")) {
            arrayList.get(0).floor = "1#";
        }
        return arrayList;
    }

    private static void doWithImages(final HashSet<String> imgUrls) {
        if (imgUrls.isEmpty()) {
            MyApp.topicActivity.checkTaskDone(0, 0);
        }
        final HashSet<String> taskCount = new HashSet<String>();
        for (final String url : imgUrls) {
            MyApp.myImageLoader.get(url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (MyApp.myDiskCache.getBitmap(url) != null) {
                        taskCount.add(url);
                    }
                    if (MyApp.topicActivity != null) {
                        MyApp.topicActivity.checkTaskDone(imgUrls.size(), taskCount.size());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    taskCount.add(url);
                    if (MyApp.topicActivity != null) {
                        MyApp.topicActivity.checkTaskDone(imgUrls.size(), taskCount.size());
                    }
                }
            }, MyApp.screenWidth, MyApp.screenHeight);
        }
    }

    public static HashMap<String, String> parseLoginForm(String response) {
        HashMap<String, String> params = new HashMap<String, String>();
        Document document = Jsoup.parse(response);
        Element form = document.select("form[id^=loginform]").first();
        params.put("url", BASE_URI + form.attr("action"));
        Elements inputs = form.getElementsByTag("input");
        for (Element input : inputs) {
            params.put(input.attr("name"), input.attr("value"));
        }
        return params;
    }

    public static String parseLoginResult(String response) {
        Document document = Jsoup.parse(response);
        Element message = document.getElementById("messagetext");
        return message.text();
    }

    public static ArrayList<SubForumItem> parseFavorite(String response) {
        ArrayList<SubForumItem> arrayList = new ArrayList<SubForumItem>();
        Document document = Jsoup.parse(response);
        Element favoriteList = document.getElementById("favorite_ul");
        if (favoriteList == null) {
            Element msg = document.getElementById("messagetext");
            if (msg == null) {
                msg = document.getElementsByClass("emp").first();
            }
            SubForumItem item = new SubForumItem("favorite", "", "", msg.text());
            arrayList.add(item);
            return arrayList;
        }
        Elements favorites = favoriteList.getElementsByTag("li");
        for (Element favorite : favorites) {
            Element a = favorite.getElementsByTag("a").last();
            Element span = favorite.getElementsByTag("span").last();
            String url = BASE_URI + a.attr("href");
            SubForumItem item = new SubForumItem(url, a.text(), span.text(), "");
            arrayList.add(item);
        }
        return arrayList;
    }

    public static HashMap<String, String> parseAddFavoriteForm(String response) {
        HashMap<String, String> params = new HashMap<String, String>();
        Document document = Jsoup.parse(response);
        Element msg = document.getElementById("messagetext");
        if (msg != null) {
            params.put("msg", msg.text());
            return params;
        }
        Element form = document.select("form[id^=favoriteform]").first();
        params.put("url", BASE_URI + form.attr("action"));
        Elements inputs = form.getElementsByTag("input");
        for (Element input : inputs) {
            params.put(input.attr("name"), input.attr("value"));
        }
        params.put("description", "");
        params.put("favoritesubmit_btn", "true");
        return params;
    }

    public static ArrayList<MainForumItem> parseSubChildren(MainForumItem subForum, String response) {
        ArrayList<MainForumItem> arrayList = new ArrayList<MainForumItem>();
        arrayList.add(subForum);
        Document document = Jsoup.parse(response);
        Element contents = document.getElementsByClass("fl_tb").first();
        if (contents == null) {
            return arrayList;
        }
        Elements links = contents.getElementsByTag("dt");
        for (Element link : links) {
            String url = BASE_URI + link.child(0).attr("href");
            String forumName = link.child(0).text();
            MainForumItem item = new MainForumItem(url, forumName, "");
            arrayList.add(item);
        }
        return arrayList;
    }

    public static HashMap<String, String> parseReplyForm(String response) {
        HashMap<String, String> params = new HashMap<String, String>();
        Document document = Jsoup.parse(response);
        Element form = document.select("form[id=postform]").first();
        if (form == null) {
            return params;
        }
        params.put("url", BASE_URI + form.attr("action"));
        Elements inputs = form.getElementsByTag("input");
        for (Element input : inputs) {
            params.put(input.attr("name"), input.attr("value"));
        }
        return params;
    }
}
