package com.grioni.app.screenwakecounter;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matias Grioni
 * @created 5/16/16
 *
 * Android has a bug when using the {@code Toolbar} and the contextual action
 * bar. The {@code MenuItem}s in the Toolbar are still clickable underneath the
 * contextual action bar. This is accounted for by keeping a reference to
 * {@code MenuItem}s in the Fragment and removing them and adding them on
 * entering and leaving of action mode.
 *
 * When using this {@code Fragment} call super#onCreateOptionsMenu at the end of
 * the {@code onCreateOptionsMenu}.
 */
public class ActionModeFragment extends Fragment {
    private List<MenuItem> items;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = new ArrayList<>();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        for(int i = 0; i < menu.size(); i++)
            items.add(menu.getItem(i));
    }

    /**
     * Hide the menu for this {@code Fragment}.
     */
    protected void hideMenu() {
        toggleMenu(false);
    }

    /**
     * Show the menu for this {@code Fragment}.
     */
    protected void showMenu() {
        toggleMenu(true);
    }

    /**
     * Helper method to set the visibility of all the
     * {@code MenuItem}s of this {@code Fragment}.
     *
     * @param visibility Flag for the visibility of all the {@code MenuItem}s.
     */
    private void toggleMenu(boolean visibility) {
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            item.setVisible(visibility);
        }
    }
}
