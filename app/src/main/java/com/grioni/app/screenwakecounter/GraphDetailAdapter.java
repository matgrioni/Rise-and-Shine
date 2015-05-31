package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Matias Grioni on 12/25/14.
 */
public class GraphDetailAdapter extends ArrayAdapter<Integer> {
    private LayoutInflater inflater;
    private List<Integer> values;

    private int textViewResourceId;

    /**
     *
     * @param context
     * @param values
     */
    public GraphDetailAdapter(Context context, int textViewResourceId, List<Integer> values) {
        super(context, textViewResourceId, values);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.values = values;
        this.textViewResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if(rowView == null)
            rowView = inflater.inflate(textViewResourceId, parent, false);

        TextView index = (TextView) rowView.findViewById(R.id.row_data_index);
        TextView count = (TextView) rowView.findViewById(R.id.row_data_count);

        index.setText(Integer.toString(position + 1));

        int curCount = values.get(position);
        count.setText(Integer.toString(curCount));

        return rowView;
    }

    /**
     *
     * @param values
     */
    public void setData(List<Integer> values) {
        this.values = values;
        clear();
        addAll(values);
        notifyDataSetChanged();
    }

    /*
    private void setIndex(TimeCard card, TextView view, int position) {
        Calendar c = Calendar.getInstance();
        if(card.interval == TimeInterval.Hour) {
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Subtract the time elapsed of the current hour, if it's less than 0, that means the
            // hour needs to be adjusted to be one less, while the minutes are now on the opposite
            // side of the 60 if that makes any sense.
            minute -= minutesElapsed;
            if(minute < 0) {
                hour--;
                minute = 60 + minute;
            }

            view.setText(hour - (values.size() - 1 - position) + ":" + minute);
        } else {
            if(card.backCount == 1) {
                card.backCount = convertSingleton(card.interval);

                if(card.interval == TimeInterval.Day) {
                    card.interval = TimeInterval.Hour;
                } else if(card.interval != TimeInterval.Hour) {
                    card.interval = TimeInterval.Day;
                }
            }

        }
    }*/
}
