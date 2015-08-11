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
 * @author - Matias Grioni
 * @created - 12/25/14
 *
 * The fragment that is opened when when a TimeCard item in the main app list is clicked. It has a
 * GraphView at the top, and the list of points below.
 * The menu items are save and delete.
 */
public class GraphDetailFragment extends Fragment {

    private TimeCardsFragment.TimeCardDeleteListener cardDeleteListener;

    private ActionBar actionBar;

    private GraphView graph;
    private TextView indexHeader;
    private ListView dataList;

    private GraphDetailAdapter graphDetailAdapter;
    private TimeCard card;
    private String axis;

    // The current TimeCard positions in the card list. This way the card can be deleted from the
    // fragment menu.
    private int position;

    /**
     * Instantiates a new instance of this fragment type, using data from the TimeCard to display
     * the graph and data points.
     *
     * @param card - The TimeCard that has the data points to display.
     * @param position - The position of this TimeCard in the global list of cards.
     * @return - The new instance of the GraphDetailFragment using the provided params.
     */
    public static GraphDetailFragment newInstance(TimeCard card, int position) {
        GraphDetailFragment graphDetails = new GraphDetailFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable("timecard", card);
        arguments.putInt("position", position);
        graphDetails.setArguments(arguments);

        return graphDetails;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getGraphTitle());
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

        // Setup the displayed values from the provided TimeCard.
        Bundle arguments = getArguments();
        card = arguments.getParcelable("timecard");
        position = arguments.getInt("position");

        // If this card only goes back one unit, then the axis has to break this TimeUnit into
        // smaller pieces. For example, Month -> Day, Week -> Day, Day -> Hour. Since minutes
        // are not tracked 1 hour stays 1 hour. Otherwise, if the TimeCard goes back multiple units
        // the x-axis is only those units. 5 weeks is 5 individual days, not 35 days, etc.
        axis = "";
        if (card.backCount == 1) {
            if (card.interval == TimeInterval.Hour || card.interval == TimeInterval.Day) {
                axis = "Hour";
            } else {
                axis = "Day";
            }
        } else {
            axis = card.interval.name();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View detailView = inflater.inflate(R.layout.fragment_graph_details, container, false);

        graph = (GraphView) detailView.findViewById(R.id.fragment_details_graph);
        graph.setAxis(axis);
        graph.setData(card.points);

        indexHeader = (TextView) detailView.findViewById(R.id.column_index_header);
        indexHeader.setText(axis);

        // Create the double columned list adapter to display the index inline with the data point.
        dataList = (ListView) detailView.findViewById(R.id.graph_points);
        dataList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        graphDetailAdapter = new GraphDetailAdapter(getActivity(), R.layout.row_graph_detail,
                card.points);
        dataList.setAdapter(graphDetailAdapter);

        return detailView;
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
                String toastMsg = "The file could not be saved";

                if(isExternalStorageWritable()) {
                    File writtenFile = writeToFile();
                    toastMsg = writtenFile.getPath() + " saved";
                }

                Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_delete:
                cardDeleteListener.onCardDelete(position);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the data this fragment displays to the data in the provided TimeCard.
     *
     * @param card - The card whose data to show.
     */
    public void update(TimeCard card) {
        graphDetailAdapter.setData(card.points);
        graph.setData(card.points);

        // Redraw the graph after updating its data
        graph.postInvalidate();

        actionBar.setTitle(getGraphTitle());
    }

    /**
     * The position of the TimeCard this Fragment is detailing in the global list of TimeCards.
     *
     * @return - The position of the TimeCard of this Fragment.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Writes the data points of the TimeCard in this Fragment to an appropriately named data file,
     * in the screewake/data folder of the sd card.
     *
     * @return - The File object of the written log file.
     */
    private File writeToFile() {
        // Get the parent directory to store the log files, and check if it exists. If not, make
        // the path.
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

    /**
     * Converts the values of the TimeCard into a string where the data point index is on the left
     * followed by a tab, and the value for that index.
     *
     * @return - The CSV style formatted string for writing to the file.
     */
    private String valuesToCSV() {
        String fileBuffer = axis + "\t" + "Views";

        for(int i = 0; i < card.points.size(); i++)
            fileBuffer += (i + 1) + "\t" + card.points.get(i) + "\n";

        return fileBuffer;
    }

    /**
     * Checks if the sd card is writable.
     *
     * @return - True if sd card is writable and False otherwise.
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))
            return true;
        return false;
    }

    /**
     * Creates the appropriate title for this Fragment. If the TimeCard goes back more than one
     * TimeUnit then, the title should be Last x months/weeks/days... If it only goes back one
     * TimeUnit then it should simply be Last month/week...
     *
     * @return - The ActionBar title for this Fragment given its TimeCard.
     */
    private String getGraphTitle() {
        String title = "";
        if(card.backCount > 1)
            title = "Last " + card.backCount + " " + card.interval.name().toLowerCase() + "s";
        else
            title = "Last " + card.interval.name().toLowerCase();
        title += ": " + card.count;

        return title;
    }
}
