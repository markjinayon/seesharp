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

public class PredictionListAdapter extends ArrayAdapter<String> {
    static class ViewHolder {
        public TextView appName, text, prediction;
    }

    public PredictionListAdapter(Context context, ArrayList<String> predictions) {
        super(context, R.layout.item_list_ai_child, predictions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_ai_child, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.appName = convertView.findViewById(R.id.appName_aiAssistant);
            viewHolder.text = convertView.findViewById(R.id.text_aiAssistant);
            viewHolder.prediction = convertView.findViewById(R.id.predicition_aiAssistant);

            convertView.setTag(viewHolder);
        }

        String item = getItem(position);
        String[] data = item.split("=");
        String[] content = data[1].split("::");

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.appName.setText(content[0]);
        viewHolder.prediction.setText(content[1]);
        viewHolder.text.setText(data[0]);

        return convertView;
    }
}
