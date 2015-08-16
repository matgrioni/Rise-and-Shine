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
 * The Fragment that is opened when when a TimeCard item in the parent Fragment
 * is clicked. It is to be used as a child Fragment and the parent Fragment must
 * implement OnCardDeletedListener. This Fragment will delete cards when the menu
 * option is chosen from TimeCardsManager and then call the callback.
 */
public class GraphDetailFragment extends Fragment {
    /**
     * @author - Matias Grioni
     * @created - 8/13/15
     *
     * Interface to be implemented by the parent Fragment so that when a TimeCard
     * is deleted in this Fragment the parent can orchestrate the Fragments
     * accordingly.
     */
    public interface OnCardDeletedListener {
        /**
         * Callback when a user deletes the TimeCard this GraphDetailFragment
         * represents.
         *
         * @param position - The former position of the TimeCard in the
         *                 TimeCardsManager TimeCards list.
         * @param card - The card that was deleted.
         */
        public void onCardDeleted(int position, TimeCard card);
    }

    private OnCardDeletedListener cardDeletedListener;

    private TimeCardsManager cardsManager;

    private ActionBar actionBar;

    private GraphView graph;
    private TextView indexHeader;
    private ListView dataList;

    private GraphDetailAdapter graphDetailAdapter;
    private TimeCard card;
    private String axis;

    // The position in the TimeCardsManager list of the TimeCard to be detailed.
    private int position;
    private List<Integer> points;
    private int count;

    /**
     * Instantiates a new instance of this fragment type, using data from the
     * TimeCard to display the graph and data points.
     *
     * @param position - The position of this TimeCard in the global list of
     *                 cards.
     * @return - The new instance of the GraphDetailFragment using the provided
     *         params.
     */
    public static GraphDetailFragment newInstance(int position) {
        GraphDetailFragment graphDetails = new GraphDetailFragment();

        Bundle arguments = new Bundle();
        arguments.putInt("position", position);
        graphDetails.setArguments(arguments);

        return graphDetails;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            cardDeletedListener = (OnCardDeletedListener) getParentFragment();
        } catch (ClassCastException ex) {
            throw new ClassCastException(getParentFragment().toString() +
                " must implement OnCardDeletedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getGraphTitle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardsManager = TimeCardsManager.getInstance(getActivity());

        // Setup the displayed values from the provided TimeCard.
        Bundle arguments = getArguments();
        position = arguments.getInt("position");
        card = cardsManager.getCard(position);

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

        points = TimeCardUtils.getPoints(card);
        count = TimeCardUtils.getCount(card);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View detailView = inflater.inflate(R.layout.fragment_graph_details, container, false);

        graph = (GraphView) detailView.findViewById(R.id.fragment_details_graph);
        graph.setAxis(axis);
        graph.setData(points);

        indexHeader = (TextView) detailView.findViewById(R.id.column_index_header);
        indexHeader.setText(axis);

        // Create the double columned list adapter to display the index inline
        // with the data point.
        dataList = (ListView) detailView.findViewById(R.id.graph_points);
        dataList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        graphDetailAdapter = new GraphDetailAdapter(getActivity(),
                R.layout.row_graph_detail, points);
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
                cardsManager.remove(position);
                cardDeletedListener.onCardDeleted(position, card);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the data this Fragment displays from the TimeCardManager.
     */
    public void update() {
        this.card = cardsManager.getCard(position);
        this.points = this.card.cache.points;
        this.count = this.card.cache.count;

        graphDetailAdapter.setData(points);
        graph.setData(points);

        // Redraw the graph after updating its data
        graph.postInvalidate();

        actionBar.setTitle(getGraphTitle());
    }

    /**
     * The position of the TimeCard this Fragment is detailing in the global
     * list of TimeCards.
     *
     * @return - The position of the TimeCard of this Fragment.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Writes the data points of the TimeCard in this Fragment to an appropriately
     * named data file, in the screewake/data folder of the sd card.
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
     * Converts the values of the TimeCard into a string where the data point
     * index is on the left followed by a tab, and the value for that index.
     *
     * @return - The CSV style formatted string for writing to the file.
     */
    private String valuesToCSV() {
        String fileBuffer = axis + "\t" + "Views";

        for(int i = 0; i < points.size(); i++)
            fileBuffer += (i + 1) + "\t" + points.get(i) + "\n";

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
     * Creates the appropriate title for this Fragment. If the TimeCard goes
     * back more than one TimeUnit then, the title should be Last x
     * months/weeks/days... If it only goes back one TimeUnit then it should
     * simply be Last month/week...
     *
     * @return - The ActionBar title for this Fragment given its TimeCard.
     */
    private String getGraphTitle() {
        String title = "";
        if(card.backCount > 1)
            title = "Last " + card.backCount + " " + card.interval.name().toLowerCase() + "s";
        else
            title = "Last " + card.interval.name().toLowerCase();
        title += ": " + count;

        return title;
    }
}
