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
 * @author - Matias Grioni
 * @created - 12/25/14
 *
 * An adapter for a double columned ListView. The left column is the index of
 * the item in the provided list to adapt. The right column is the item.
 */
public class IndexedAdapter extends ArrayAdapter<Integer> {
    private LayoutInflater inflater;
    private List<Integer> values;

    private int textViewResourceId;

    /**
     *
     * @param context
     * @param values
     */
    public IndexedAdapter(Context context, int textViewResourceId, List<Integer> values) {
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
}
