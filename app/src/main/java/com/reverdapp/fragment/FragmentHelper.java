package com.reverdapp.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.reverdapp.R;

/**
 * Created by wojci on 8/6/15.
 */
public final class FragmentHelper {
    public static void ReplaceFragment(FragmentManager mgr, Fragment replaceWithFragment)
    {
        FragmentTransaction fragmentTransaction = mgr.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out);

        fragmentTransaction.replace(R.id.content_frame, replaceWithFragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }
}
