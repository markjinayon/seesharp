package com.parentalcontrol.seesharp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parentalcontrol.seesharp.R;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ReportListAdapter extends ArrayAdapter<String> {

    static class ViewHolder {
        public TextView title, message, timestamp;
    }

    public ReportListAdapter(Context context, ArrayList<String> reports) {
        super(context, R.layout.item_list_reports, reports);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_reports, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.title_reportItem);
            viewHolder.message = convertView.findViewById(R.id.message_reportItem);
            viewHolder.timestamp = convertView.findViewById(R.id.timestamp_reportItem);

            convertView.setTag(viewHolder);
        }

        String data = getItem(position);
        String[] newData = data.split("=");
        String[] content = newData[1].split("::");
        ZonedDateTime zd = ZonedDateTime.parse(newData[0]);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.title.setText(content[0]);
        viewHolder.message.setText(content[1]);
        viewHolder.timestamp.setText(zd.getHour()+":"+zd.getMinute());

        return convertView;
    }
}
