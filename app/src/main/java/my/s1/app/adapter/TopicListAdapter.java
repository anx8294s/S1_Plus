package my.s1.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import my.s1.app.MyApp;
import my.s1.app.R;
import my.s1.app.models.Topic;

import java.util.List;

public class TopicListAdapter extends ArrayAdapter<Topic> {

    private final int resource;
    private static final String LONG_SPACE = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    public TopicListAdapter(Context context, int resource, List<Topic> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.topic_title);
            holder.content = (TextView) convertView.findViewById(R.id.topic_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Topic item = getItem(position);
        String title = "<font color=\"#0099FF\">" + item.name + "</font>" + LONG_SPACE + LONG_SPACE
                + item.time + LONG_SPACE + "<font color=\"#FF1493\">" + item.floor + "</font>";
        holder.title.setText(Html.fromHtml(title));
        holder.content.setText(Html.fromHtml(item.body, new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String s) {
                String key = MyApp.myDiskCache.hashKey(s);
                Bitmap bitmap = MyApp.myMemoryCacheLruCache.get(key);
                Drawable drawable = null;
                if (bitmap != null) {
                    drawable = new BitmapDrawable(getContext().getResources(), bitmap);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                }
                return drawable;
            }
        }, null));
        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView content;
    }
}
