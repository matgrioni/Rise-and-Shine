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
 * @author - Matias Grioni
 * @created - 1/5/15
 *
 * Fragment that is used to allow the user to input a new TimeCard. The Fragment
 * will automatically add the TimeCard to TimeCardsManager assuming valid input.
 * Can only be used as a child Fragment and the parent Fragment must implement
 * the OnTimeCardAddedListener.
 */
public class AddCardDialogFragment extends DialogFragment {
    /**
     * @author - Matias Grioni
     * @created - 8/12/15
     *
     * Interface to allow for fragment communication. The parent fragment will
     * implement this interface so that when the TimeCard is added the parent
     * fragment can update itself as necessary. In this case adding the new
     * TimeCard to the card list.
     */
    public interface OnCardAddedListener {
        /**
         * Callback for when the user creates a TimeCard using this dialog.
         *
         * @param card - The card that was added.
         */
        public void onCardAdded(TimeCard card);
    }

    private TimeCardsManager cardsManager;

    private OnCardAddedListener cardAddedListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        cardsManager = TimeCardsManager.getInstance(activity);

        try {
            cardAddedListener = (OnCardAddedListener) getParentFragment();
        } catch (ClassCastException ex) {
            throw new ClassCastException(getParentFragment().toString() +
                " must implement OnCardAddedListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        final boolean collapsed = preferences.
                getBoolean("pref_card_collapsed", true);

        // Get the LayoutInflater and inflate the dialog view and get the
        // needed child views.
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_add_card, null);
        final EditText backCountText = (EditText) dialogView.findViewById(R.id.dialog_back_count);
        final Spinner cardTypeSpinner =
                (Spinner) dialogView.findViewById(R.id.dialog_card_type);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, getIntervals());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cardTypeSpinner.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
        .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String backCountStr = backCountText.getText().toString();
                String spinnerItem = cardTypeSpinner.getSelectedItem().toString();
                TimeInterval interval = TimeInterval.valueOf(spinnerItem);

                // If the number input was not a positive integer then notify
                // the user of the input error. The number may still cause an
                // overflow error so surround the parsing in a try-catch, to
                // make sure not number too large is parsed.
                if (isPositiveNumber(backCountStr)) {
                    // If the number is able to be parsed, a TimeCard is created
                    // using the data and the points generated from the database.
                    try {
                        // Try to parse the input and get the data from this input.
                        int backCount = Integer.parseInt(backCountStr);
                        TimeCard card = new TimeCard(interval, backCount, collapsed);

                        // Add the card to the model, and notify the listener the
                        // card was added.
                        cardsManager.addCard(card);
                        card = cardsManager.query(cardsManager.getCards().size() - 1);
                        cardAddedListener.onCardAdded(card);
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                        Toast.makeText(getActivity(), backCountStr + " is too large",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), backCountStr + "is not a valid positive number",
                            Toast.LENGTH_SHORT).show();
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
     * Get the list of TimeIntervals as their String representations. This way
     * they can be used in an ArrayAdapter.
     *
     * @return - A list of the names of the TimeIntervals.
     */
    private List<String> getIntervals() {
        List<String> intervals = new ArrayList<String>();

        for(TimeInterval interval : TimeInterval.values())
            intervals.add(interval.toString());

        return intervals;
    }

    /**
     * Checks if the provided String is a positive number.
     *
     * @param str - The String to check if it is a number.
     * @return - True if str is a positive number and false otherwise.
     */
    private boolean isPositiveNumber(String str) {
        return str.matches("\\d+");
    }
}
