package my.s1.app.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import my.s1.app.models.MainForumItem;

import java.util.List;

public class ManiListAdapter extends ArrayAdapter<MainForumItem> {

    private final int resource;

    public ManiListAdapter(Context context, int resource, List<MainForumItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
        }
        TextView textView = (TextView) convertView;
        MainForumItem item = getItem(position);
        String html = item.forumName + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"#FF1493\">" + item.num + "</font>";
        textView.setText(Html.fromHtml(html));
        return convertView;
    }
}
