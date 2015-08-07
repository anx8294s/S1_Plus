package my.s1.app.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import my.s1.app.models.SubForumItem;

import java.util.List;

public class SubListAdapter extends ArrayAdapter<SubForumItem> {

    private final int resource;

    public SubListAdapter(Context context, int resource, List<SubForumItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
        }
        TextView textView = (TextView) convertView;
        SubForumItem item = getItem(position);
        String html = item.title + "&nbsp;&nbsp;<font color=\"#FF1493\">" + item.num + "</font>"
                + "&nbsp;&nbsp;<font color=\"#0099FF\">" + item.postBy + "</font>";
        textView.setText(Html.fromHtml(html));
        return convertView;
    }
}
