package com.grioni.app.screenwakecounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matias Grioni on 1/5/15.
 */
public class AddCardDialogFragment extends DialogFragment {

    private ScreenCountDatabase countDatabase;
    private AddCardDialogListener addCardListener;

    /**
     *
     */
    public interface AddCardDialogListener {
        /**
         *
         * @param card
         */
        public void onCardAdded(TimeCard card);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            addCardListener = (AddCardDialogListener) activity;
        } catch(ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement AddCardDialogListener");
        }

        countDatabase = ScreenCountDatabase.getInstance(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View dialogView = inflater.inflate(R.layout.dialog_add_card, null);
        final Spinner cardTypeSpinner = (Spinner) dialogView.findViewById(R.id.dialog_card_type);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, getIntervals());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cardTypeSpinner.setAdapter(adapter);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final boolean collapsed = preferences.getBoolean("pref_card_collapsed", true);

        final EditText backCountText = (EditText) dialogView.findViewById(R.id.dialog_back_count);

        builder.setView(dialogView)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String backCountStr = backCountText.getText().toString();
                        String spinnerItem = cardTypeSpinner.getSelectedItem().toString();
                        TimeInterval interval = TimeInterval.valueOf(spinnerItem);

                        try {
                            int backCount = Integer.parseInt(backCountStr);
                            List<Integer> points = countDatabase.getCounts(interval, backCount);

                            TimeCard card = new TimeCard(interval, backCount, sumPoints(points), points, collapsed);
                            addCardListener.onCardAdded(card);
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                            Toast.makeText(getActivity(), "'" + backCountStr + "" + " not a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getDialog().cancel();
                    }
                });

        return builder.create();
    }

    /**
     *
     * @return
     */
    private List<String> getIntervals() {
        List<String> intervals = new ArrayList<String>();

        for(int i = 0; i < TimeInterval.values().length; i++)
            intervals.add(TimeInterval.values()[i].toString());

        return intervals;
    }

    /**
     *
     * @param points
     * @return
     */
    private int sumPoints(List<Integer> points) {
        int sum = 0;
        for(int i = 0; i < points.size(); i++)
            sum += points.get(i);

        return sum;
    }
}
