package my.s1.app.models;

public class MessageEvent {
    public final int total;
    public final int done;

    public MessageEvent(int total, int done) {
        this.total = total;
        this.done = done;
    }
}
