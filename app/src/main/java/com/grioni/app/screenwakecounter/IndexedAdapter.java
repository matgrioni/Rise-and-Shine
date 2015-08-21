package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * @author - Matias Grioni
 * @created - 12/25/14
 *
 * An adapter for a double columned ListView. The left column is the index of
 * the item in the provided list to adapt. The right column is the item. The
 * resourceId for the row view should have two TextViews with the index TextView
 * having id of row_data_index, and the other with id of row_data_count.
 *
 * Note that when the index is displayed in the appropriate TextView, it will
 * be the index+1 so that the first data point in the list has an index of 1.
 *
 * Second note, when the list item is shown in the appropriate TextView, the
 * toString method will be called on it.
 */
public class IndexedAdapter<T> extends ArrayAdapter<T> {
    private LayoutInflater inflater;
    private int textViewResourceId;

    /**
     * Create a new IndexedAdapter using the provided Context, resource id for
     * a View, and data points.
     *
     * @param context - The Context object to use for this adapter.
     * @param values - The values to adapt.
     */
    public IndexedAdapter(Context context, int textViewResourceId, List<T> values) {
        super(context, textViewResourceId, values);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        T item = getItem(position);
        count.setText(item.toString());

        return rowView;
    }

    /**
     * Clears the prior data and sets it to the provided list.
     *
     * @param values - The new values for the Adapter to show.
     */
    public void setData(List<T> values) {
        clear();
        addAll(values);
        notifyDataSetChanged();
    }


}
