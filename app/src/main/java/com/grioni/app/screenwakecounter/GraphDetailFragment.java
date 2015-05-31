package com.grioni.app.screenwakecounter;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matias Grioni on 12/25/14.
 */
public class GraphDetailFragment extends Fragment {

    private TimeCardsFragment.TimeCardDeleteListener cardDeleteListener;

    private ActionBar actionBar;

    private GraphView graph;
    private TextView indexHeader;
    private ListView dataList;

    private GraphDetailAdapter graphDetailAdapter;
    private TimeCard card;
    private List<Integer> values;
    private String label;

    private int position;

    public static GraphDetailFragment newInstance(TimeCard card, int position) {
        GraphDetailFragment graphDetails = new GraphDetailFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable("timecard", card);
        arguments.putInt("position", position);
        graphDetails.setArguments(arguments);

        return graphDetails;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {

            cardDeleteListener = (TimeCardsFragment.TimeCardDeleteListener) activity;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle arguments = getArguments();
        card = arguments.getParcelable("timecard");
        position = arguments.getInt("position");

        label = "Hour";
        if (card.backCount == 1) {
            if (card.interval != TimeInterval.Day)
                label = "Day";
        } else
            label = card.interval.name();
        values = new ArrayList<Integer>(card.points);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View detailView = inflater.inflate(R.layout.fragment_graph_details, container, false);

        graph = (GraphView) detailView.findViewById(R.id.fragment_details_graph);
        graph.setAxis(label);
        graph.setData(values);

        indexHeader = (TextView) detailView.findViewById(R.id.column_index_header);
        indexHeader.setText(label);

        dataList = (ListView) detailView.findViewById(R.id.graph_points);
        graphDetailAdapter = new GraphDetailAdapter(getActivity(), R.layout.row_graph_detail, values);
        dataList.setAdapter(graphDetailAdapter);

        return detailView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

        actionBar.setTitle(getGraphTitle());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.graph_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_save:
                if(isExternalStorageWritable()) {
                    File writtenFile = writeToFile();

                    String dispMessage = writtenFile.getPath() + " saved";
                    Toast.makeText(getActivity(), dispMessage, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_delete:
                cardDeleteListener.onCardDelete(position);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     */
    private File writeToFile() {
        File parent = new File(Environment.getExternalStorageDirectory(), "screenwake/data");
        if(!parent.exists())
            parent.mkdirs();

        String filename = card.interval.name() + card.backCount + ".csv";
        File file = new File(parent, filename);

        int index = 0;
        while(file.exists()) {
            filename = card.interval.name() + card.backCount + "(" + (++index) + ")" + ".csv";
            file = new File(parent, filename);
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(valuesToCSV().getBytes());
            outputStream.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }

        return file;
    }

    private String valuesToCSV() {
        String fileBuffer = label + "\t" + "Views";

        for(int i = 0; i < values.size(); i++)
            fileBuffer += (i + 1) + "\t" + values.get(i) + "\n";

        return fileBuffer;
    }

    /**
     *
     */
    public void update(TimeCard card) {
        values = card.points;
        graphDetailAdapter.setData(values);
        graph.setData(values);

        graph.postInvalidate();

        actionBar.setTitle(getGraphTitle());
    }

    /**
     *
     * @return
     */
    public int getPosition() {
        return position;
    }

    /**
     *
     * @return
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))
            return true;
        return false;
    }

    private String getGraphTitle() {
        String title = "Last " + card.backCount + " " + card.interval.name().toLowerCase();
        if(card.backCount > 1)
            title += "s";
        title += ": " + card.count;

        return title;
    }
}
