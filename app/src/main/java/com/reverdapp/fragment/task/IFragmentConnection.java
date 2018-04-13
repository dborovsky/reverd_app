package com.reverdapp.fragment.task;

import android.os.Bundle;

import com.reverdapp.R;

/**
 * Created by wojci on 7/24/15.
 */

// Interface used to connect a fragment and a async task.
// Call getAsyncTaskOptions() from the task to obtain the options passed to it (use the IFC_* constants).
// When done call handlePostExecute to handle any navigation the fragment should do after the task finishes.
public interface IFragmentConnection
{
    static final String IFC_BASE_ID = "FragmentConnection-";
    static final String IFC_FULL_NUMBER_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_FULL_NUMBER_ID);
    static final String IFC_NUMBER_WITHOUT_PREFIX_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_NUMBER_WITHOUT_PREFIX_ID);

    // Numerical country code. Example: 45 for DK.
    static final String IFC_COUNTRY_NUM_CODE_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_COUNTRY_NUM_CODE_ID);
    // Country name. Example; Denmark.
    static final String IFC_COUNTRY_NAME_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_COUNTRY_NAME_ID);

    static final String IFC_COUNTRY_ISO3166_1_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_COUNTRY_ISO3166_1_ID);

    static final String IFC_NAME_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_NAME_ID);
    static final String IFC_NOTE_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_NOTE_ID);
    static final String IFC_NUMBER_LIST = IFC_BASE_ID + Integer.toString(R.id.IFC_NUMBER_LIST);

    static final String IFC_LISTID_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_LISTID_ID);

    static final String IFC_ADD_TO_COMM_BL_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_ADD_TO_COMM_BL_ID);
    static final String IFC_ADD_TO_COMM_WL_ID = IFC_BASE_ID + Integer.toString(R.id.IFC_ADD_TO_COMM_WL_ID);

    // In app purchase:
    static final String IFC_IAP_TERM = IFC_BASE_ID + Integer.toString(R.id.IFC_IAP_TERM);
    static final String IFC_IAP_RECOURRING = IFC_BASE_ID + Integer.toString(R.id.IFC_IAP_RECOURRING);

    // Get the optons passed to a async task.
    Bundle getAsyncTaskOptions();

    static final int UNUSED_NAVIGATE_ID = -1;

    // Handle navigation after completing a task.
    // The arguments decides what destination to use.
    void handlePostExecute(final int destination);
}
