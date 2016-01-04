package com.grioni.app.screenwakecounter;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import models.TimeCard;
import models.TimeCardCache;
import models.TimeInterval;
import utils.DataUtils;
import utils.Exporter;
import utils.LabelUtils;
import views.GraphView;

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
         * @param card - The card that was deleted.
         */
        void onCardDeleted(TimeCard card);
    }

    private TimeCardsManager cardsManager;
    private ScreenCountDatabase countDatabase;
    private Exporter exporter;

    private OnCardDeletedListener cardDeletedListener;

    private ActionBar actionBar;
    private TextView average;
    private TextView stdev;
    private GraphView graph;
    private String axis;
    private IndexedAdapter<Integer> graphDetailAdapter;

    private TimeCard card;
    private TimeCardCache cache;

    /**
     * Instantiates a new instance of this fragment type, using data from the
     * TimeCard to display the graph and data points.
     *
     * @param card The position of this TimeCard in the global list of cards.
     * @param cache The {@code TimeCardCache} associated with this.
     * @return The new instance of the GraphDetailFragment using the provided params.
     */
    public static GraphDetailFragment newInstance(TimeCard card, TimeCardCache cache) {
        GraphDetailFragment graphDetails = new GraphDetailFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable("card", card);
        arguments.putParcelable("cache", cache);
        graphDetails.setArguments(arguments);

        return graphDetails;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            cardDeletedListener = (OnCardDeletedListener) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(getParentFragment().toString() +
                " must implement OnCardDeletedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        try {
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        } catch (ClassCastException ex) {
            throw new ClassCastException(getActivity().toString() + " must " +
                " extend AppCompatActivity");
        }

        actionBar.setTitle(LabelUtils.last(card.interval, card.backCount) + cache.count);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardsManager = TimeCardsManager.getInstance(getActivity());
        countDatabase = ((InstanceApplication) getActivity().getApplicationContext()).getCountDatabase();
        exporter = new Exporter("screenwake/data");

        // Get the TimeCard given the position and the TimeCard should have
        // already been queried and up to date.
        Bundle arguments = getArguments();
        card = arguments.getParcelable("card");
        cache = arguments.getParcelable("cache");

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
        graph.setData(cache.data);

        average = (TextView) detailView.findViewById(R.id.graph_average);
        stdev = (TextView) detailView.findViewById(R.id.graph_stdev);

        updateStats();

        TextView indexHeader = (TextView) detailView.findViewById(R.id.column_index_header);
        indexHeader.setText(axis);

        // Create the double columned list adapter to display the index inline
        // with the data point.
        ListView pointsView = (ListView) detailView.findViewById(R.id.graph_points);
        pointsView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        graphDetailAdapter = new IndexedAdapter<>(getActivity(),
                R.layout.row_graph_detail, cache.data);
        pointsView.setAdapter(graphDetailAdapter);

        return detailView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.settings).setVisible(false);
        inflater.inflate(R.menu.graph_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_save:
                String toastMsg = "Unable to export file";

                if (exporter.isValid()) {
                    String filename = getFileName();
                    if (exporter.write(filename, valuesToCSV().getBytes()))
                        toastMsg = "Export file written to " + filename;
                }

                Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_delete:
                cardsManager.remove(card.id);
                cardDeletedListener.onCardDeleted(card);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the data this {@code Fragment} displays from the {@code TimeCardManager} and
     * {@code ScreenCountDatabase}.
     */
    public void update() {
        cache.data = countDatabase.getEntries(card.interval, card.backCount);
        cache.count = DataUtils.sum(cache.data);

        updateStats();

        // Redraw the graph after updating its data
        graphDetailAdapter.setData(cache.data);
        graph.setData(cache.data);
        graph.postInvalidate();

        actionBar.setTitle(LabelUtils.last(card.interval, card.backCount) + cache.count);
    }

    /**
     * Converts the values of the TimeCard into a string where the data point
     * index is on the left followed by a tab, and the value for that index.
     *
     * @return - The CSV style formatted string for writing to the file.
     */
    private String valuesToCSV() {
        String fileBuffer = axis + "\t" + "Views\n";

        for(int i = 0; i < cache.data.size(); i++)
            fileBuffer += (i + 1) + "\t" + cache.data.get(i) + "\n";

        return fileBuffer;
    }

    /**
     * Gets the appropriate filename for this GraphDetailFragment to export. Accounts for the fact
     * that there can be a file already with the desired name and includes a counter.
     *
     * If the Exporter object is not valid then the duplicate file scenario is not considered.
     *
     * @return - The next filename to be exported.
     */
    private String getFileName() {
        String next = card.interval.name() + card.backCount + ".csv";

        if (exporter.isValid()) {
            int index = 1;
            while (exporter.exists(next)) {
                next = card.interval.name() + card.backCount + "(" + index + ").csv";
                index++;
            }
        }

        return next;
    }

    /**
     * Updates the stats section of this {@code Fragment} with 2 decimal places of precision.
     */
    private void updateStats() {
        double averageComp = ((int) (DataUtils.average(cache.data) * 1000)) / 1000.d;
        double stdevComp = ((int) (DataUtils.stdev(cache.data) * 1000)) / 1000.d;

        average.setText(Double.toString(averageComp));
        stdev.setText(Double.toString(stdevComp));
    }
}
