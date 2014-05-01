package org.opensilk.filebrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


/**
 * Created by drew on 4/30/14.
 */
public class FileItemArrayAdapter extends ArrayAdapter<FileItem> {

    public FileItemArrayAdapter(Context context) {
        super(context, -1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        ViewHolder h;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.file_item, parent, false);
            h = new ViewHolder(v);
            v.setTag(h);
        } else {
            v = convertView;
            h = (ViewHolder) v.getTag();
        }
        FileItem item = getItem(position);
        h.icon.forType(item.getMediaType(), item.getMimeType());
        h.title.setText(item.getTitle());
        h.subTitle.setVisibility(View.GONE);
        return v;
    }

    static class ViewHolder {
//        @InjectView(R.id.file_icon)
        FileIcon icon;
//        @InjectView(R.id.file_title)
        TextView title;
//        @InjectView(R.id.file_sub_title)
        TextView subTitle;

        protected ViewHolder(View v) {
//            ButterKnife.inject(this, v);
            icon = (FileIcon) v.findViewById(R.id.file_icon);
            title = (TextView) v.findViewById(R.id.file_title);
            subTitle = (TextView) v.findViewById(R.id.file_sub_title);
        }
    }
}
