package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matias Grioni on 5/15/15.
 */
public class NotificationPickerPreference extends DialogPreference {
    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        // The value to persist of the preference while some instance is changed
        String value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value);
        }
    }

    private static final String DEFAULT_VALUE = "1 Hour";

    private final List<String> INTERVALS = getIntervals();

    private EditText backCount;
    private Spinner intervalType;

    private String preference = "";

    public NotificationPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Setup the layout of the Preference for picking what value to show in the Notification.
        setDialogLayoutResource(R.layout.dialog_notif_picker);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        backCount = (EditText) view.findViewById(R.id.dialog_notif_back_count);
        intervalType = (Spinner) view.findViewById(R.id.dialog_notif_card_type);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, INTERVALS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalType.setAdapter(adapter);

        updateViews(preference);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // If there is a value that is already saved, then populate the dialog with the current values
        // otherwise, populate with the default values, 1 hour.
        if(restorePersistedValue) {
            preference = this.getPersistedString(DEFAULT_VALUE);
        } else {
            preference = defaultValue.toString();
            persistString(preference);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // Positive result meaning that ok was clicked, so save the inputted information
        if(positiveResult) {
            // Since only one value can be saved per preference object, store the backcount and
            // interval type together as one string
            persistString(getValue());
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        if(isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check if the state was saved properly in onSaveInstanceState
        if(state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set the views of the dialog for the preference appropriately.
        updateViews(myState.value);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    private void updateViews(String value) {
        // Set the number preference
        int split = value.indexOf(" ");
        String number = value.substring(0, split);
        backCount.setText(number);

        // Now set the spinner to the current interval
        String intervalStr = value.substring(split + 1);
        intervalType.setSelection(INTERVALS.indexOf(intervalStr));
    }

    private String getValue() {
        return backCount.getText().toString() +  " " + intervalType.getSelectedItem().toString();
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
}
