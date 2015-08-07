package my.s1.app.models;

public class Topic {
    public String name;
    public String body;
    public String time;
    public String floor;
    public String reply;

    public Topic(String name, String body, String time, String floor, String reply) {
        this.name = name;
        this.body = body;
        this.time = time;
        this.floor = floor;
        this.reply = reply;
    }
}
