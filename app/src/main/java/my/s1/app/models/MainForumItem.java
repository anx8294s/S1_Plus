package my.s1.app.models;

public class MainForumItem {
    public String url;
    public String forumName;
    public String num;

    public MainForumItem(String url, String forumName, String num) {
        this.url = url;
        this.forumName = forumName;
        this.num = num;
    }

    @Override
    public String toString() {
        return this.forumName;
    }
}
