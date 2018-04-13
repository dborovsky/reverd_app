package com.reverdapp.fragment.task;

import android.os.Bundle;

import com.reverdapp.model.NumberListModel;

import java.util.ArrayList;

/**
 * Created by wojci on 8/5/15.
 */
public class FragmentConnectionHelper {

    public static int getNumberOfSelectedItems(final Bundle b, final String listId) {

        final ArrayList<NumberListModel> list = b.getParcelableArrayList(listId);

        int selected = 0;
        for (NumberListModel m: list) {
            if (m.isSelected()) {
                selected++;
            }
        }
        return selected;
    }
}
